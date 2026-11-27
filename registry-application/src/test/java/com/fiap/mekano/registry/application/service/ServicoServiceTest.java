package com.fiap.mekano.registry.application.service;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.model.Servico;
import com.fiap.mekano.registry.domain.port.in.CreateServicoCommand;
import com.fiap.mekano.registry.domain.port.out.ServicoRepositoryPort;
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
class ServicoServiceTest {

    @Mock
    ServicoRepositoryPort repository;

    @InjectMocks
    ServicoService service;

    @Test
    void deveCriarServicoQuandoNomeNaoExiste() {
        when(repository.existsByNome("Troca de oleo")).thenReturn(false);
        when(repository.create(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Servico criado = service.create(new CreateServicoCommand(
                " Troca de oleo ", "Troca de filtro", new BigDecimal("150.00")));

        assertThat(criado.getNome()).isEqualTo("Troca de oleo");
        assertThat(criado.getIsActive()).isTrue();
    }

    @Test
    void deveRetornar409QuandoNomeJaExiste() {
        when(repository.existsByNome("Troca de oleo")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateServicoCommand(
                "Troca de oleo", null, new BigDecimal("150.00"))))
                .isInstanceOfSatisfying(AppException.class, exception -> assertThat(exception.getStatus()).isEqualTo(409));
        verify(repository, never()).create(any(Servico.class));
    }

    @Test
    void naoDeveReativarServicoComNomeEmUso() {
        UUID id = UUID.randomUUID();
        Servico inativo = Servico.reconstitute(
                id, "Troca de oleo", null, new BigDecimal("150.00"), LocalDateTime.now(), false);
        when(repository.findById(id)).thenReturn(Optional.of(inativo));
        when(repository.existsByNomeAndIdNot("Troca de oleo", id)).thenReturn(true);

        assertThatThrownBy(() -> service.reactivate(id))
                .isInstanceOfSatisfying(AppException.class, exception -> assertThat(exception.getStatus()).isEqualTo(409));
        verify(repository, never()).reactivate(id);
    }
}
