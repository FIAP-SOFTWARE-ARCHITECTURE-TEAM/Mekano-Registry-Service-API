package com.fiap.mekano.registry.application.service;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.model.Veiculo;
import com.fiap.mekano.registry.domain.port.in.CreateVeiculoCommand;
import com.fiap.mekano.registry.domain.port.in.UpdateVeiculoCommand;
import com.fiap.mekano.registry.domain.port.out.ClienteRepositoryPort;
import com.fiap.mekano.registry.domain.port.out.VeiculoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    private static final String PLACA = "ABC1D23";

    @Mock
    private VeiculoRepositoryPort veiculoRepository;

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    @Test
    void deveCriarVeiculoQuandoClienteAtivoEPlacaUnica() {
        UUID clienteUuid = UUID.randomUUID();
        when(clienteRepository.findById(clienteUuid)).thenReturn(Optional.of(cliente(clienteUuid, true)));
        when(veiculoRepository.existsByPlaca(PLACA)).thenReturn(false);
        when(veiculoRepository.create(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo criado = veiculoService.execute(
                new CreateVeiculoCommand(clienteUuid, PLACA, "Volkswagen", "Gol", 2020));

        assertThat(criado.getClienteUuid()).isEqualTo(clienteUuid);
        assertThat(criado.getPlaca().getValue()).isEqualTo(PLACA);
        assertThat(criado.getIsActive()).isTrue();
        verify(veiculoRepository).create(any(Veiculo.class));
    }

    @Test
    void deveRetornar404QuandoClienteNaoExiste() {
        UUID clienteUuid = UUID.randomUUID();
        when(clienteRepository.findById(clienteUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.execute(
                new CreateVeiculoCommand(clienteUuid, PLACA, "Volkswagen", "Gol", 2020)))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(404));

        verify(veiculoRepository, never()).create(any(Veiculo.class));
    }

    @Test
    void deveRetornar409QuandoClienteInativo() {
        UUID clienteUuid = UUID.randomUUID();
        when(clienteRepository.findById(clienteUuid)).thenReturn(Optional.of(cliente(clienteUuid, false)));

        assertThatThrownBy(() -> veiculoService.execute(
                new CreateVeiculoCommand(clienteUuid, PLACA, "Volkswagen", "Gol", 2020)))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(409));

        verify(veiculoRepository, never()).create(any(Veiculo.class));
    }

    @Test
    void deveRetornar409QuandoPlacaJaCadastrada() {
        UUID clienteUuid = UUID.randomUUID();
        when(clienteRepository.findById(clienteUuid)).thenReturn(Optional.of(cliente(clienteUuid, true)));
        when(veiculoRepository.existsByPlaca(PLACA)).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.execute(
                new CreateVeiculoCommand(clienteUuid, PLACA, "Volkswagen", "Gol", 2020)))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(409));

        verify(veiculoRepository, never()).create(any(Veiculo.class));
    }

    @Test
    void deveAtualizarMarcaModeloEAnoMantendoPlacaECliente() {
        Veiculo existente = veiculo();
        when(veiculoRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(veiculoRepository.update(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo atualizado = veiculoService.update(existente.getId(),
                new UpdateVeiculoCommand("Fiat", "Uno", 2015));

        assertThat(atualizado.getMarca()).isEqualTo("Fiat");
        assertThat(atualizado.getModelo()).isEqualTo("Uno");
        assertThat(atualizado.getAno()).isEqualTo(2015);
        assertThat(atualizado.getPlaca().getValue()).isEqualTo(PLACA);
        assertThat(atualizado.getClienteUuid()).isEqualTo(existente.getClienteUuid());
    }

    @Test
    void deveManterDadosNaoInformadosNaAtualizacao() {
        Veiculo existente = veiculo();
        when(veiculoRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(veiculoRepository.update(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo atualizado = veiculoService.update(existente.getId(),
                new UpdateVeiculoCommand("  ", null, null));

        assertThat(atualizado.getMarca()).isEqualTo(existente.getMarca());
        assertThat(atualizado.getModelo()).isEqualTo(existente.getModelo());
        assertThat(atualizado.getAno()).isEqualTo(existente.getAno());
    }

    @Test
    void deveRetornar404AoAtualizarVeiculoInexistente() {
        UUID id = UUID.randomUUID();
        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.update(id, new UpdateVeiculoCommand("Fiat", "Uno", 2015)))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(404));
    }

    @Test
    void deveRetornar404AoBuscarVeiculoInexistente() {
        UUID id = UUID.randomUUID();
        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.findById(id))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(404));
    }

    @Test
    void deveDelegarListagemEContagem() {
        UUID clienteUuid = UUID.randomUUID();
        when(veiculoRepository.findAll(0, 20, "placa,asc", clienteUuid, "ABC", true)).thenReturn(List.of());
        when(veiculoRepository.countAll(clienteUuid, "ABC", true)).thenReturn(0L);

        assertThat(veiculoService.findAll(0, 20, "placa,asc", clienteUuid, "ABC", true)).isEmpty();
        assertThat(veiculoService.countAll(clienteUuid, "ABC", true)).isZero();
    }

    @Test
    void deveDelegarSoftDeleteEReativacao() {
        Veiculo inativo = Veiculo.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), PLACA, "Volkswagen", "Gol", 2020,
                LocalDateTime.now(), false);
        when(veiculoRepository.findById(inativo.getId())).thenReturn(Optional.of(inativo));
        when(veiculoRepository.existsByPlaca(PLACA)).thenReturn(false);

        veiculoService.delete(inativo.getId());
        veiculoService.reactivate(inativo.getId());

        verify(veiculoRepository).markAsDeleted(inativo.getId());
        verify(veiculoRepository).reactivate(inativo.getId());
    }

    @Test
    void naoDeveReativarVeiculoQuandoPlacaJaPertenceAOutroAtivo() {
        Veiculo inativo = Veiculo.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), PLACA, "Volkswagen", "Gol", 2020,
                LocalDateTime.now(), false);
        when(veiculoRepository.findById(inativo.getId())).thenReturn(Optional.of(inativo));
        when(veiculoRepository.existsByPlaca(PLACA)).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.reactivate(inativo.getId()))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(409));

        verify(veiculoRepository, never()).reactivate(inativo.getId());
    }

    private static Cliente cliente(UUID uuid, boolean ativo) {
        return Cliente.reconstitute(
                uuid, "Maria", "52998224725", "maria@example.com", null,
                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000",
                LocalDateTime.now(), ativo);
    }

    private static Veiculo veiculo() {
        return Veiculo.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), PLACA, "Volkswagen", "Gol", 2020,
                LocalDateTime.now(), true);
    }
}
