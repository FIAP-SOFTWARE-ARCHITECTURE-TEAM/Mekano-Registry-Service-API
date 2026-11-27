package com.fiap.mekano.registry.application.service;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.model.Peca;
import com.fiap.mekano.registry.domain.port.in.CreatePecaCommand;
import com.fiap.mekano.registry.domain.port.out.PecaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PecaServiceTest {

    @Mock
    PecaRepositoryPort repository;

    @InjectMocks
    PecaService service;

    @Test
    void deveCriarPecaQuandoCodigoNaoExiste() {
        when(repository.existsByCodigo("PEA-001")).thenReturn(false);
        when(repository.create(any(Peca.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Peca criada = service.create(new CreatePecaCommand(" PEA-001 ", "Filtro", new BigDecimal("45.90")));

        assertThat(criada.getCodigo()).isEqualTo("PEA-001");
        assertThat(criada.getIsActive()).isTrue();
        verify(repository).create(any(Peca.class));
    }

    @Test
    void deveRetornar409QuandoCodigoJaExiste() {
        when(repository.existsByCodigo("PEA-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new CreatePecaCommand("PEA-001", "Filtro", new BigDecimal("45.90"))))
                .isInstanceOfSatisfying(AppException.class, exception -> assertThat(exception.getStatus()).isEqualTo(409));
        verify(repository, never()).create(any(Peca.class));
    }

    @Test
    void naoDeveReativarPecaComCodigoEmUso() {
        UUID id = UUID.randomUUID();
        Peca inativa = Peca.reconstitute(
                id, "PEA-001", "Filtro", new BigDecimal("45.90"), LocalDateTime.now(), false);
        when(repository.findById(id)).thenReturn(Optional.of(inativa));
        when(repository.existsByCodigoAndIdNot("PEA-001", id)).thenReturn(true);

        assertThatThrownBy(() -> service.reactivate(id))
                .isInstanceOfSatisfying(AppException.class, exception -> assertThat(exception.getStatus()).isEqualTo(409));
        verify(repository, never()).reactivate(id);
    }
}
