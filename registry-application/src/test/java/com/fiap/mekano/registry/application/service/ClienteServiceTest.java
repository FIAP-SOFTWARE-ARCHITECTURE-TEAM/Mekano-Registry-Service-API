package com.fiap.mekano.registry.application.service;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.model.Cliente;
import com.fiap.mekano.registry.domain.port.in.CreateClienteCommand;
import com.fiap.mekano.registry.domain.port.in.EnderecoCommand;
import com.fiap.mekano.registry.domain.port.in.UpdateClienteCommand;
import com.fiap.mekano.registry.domain.port.out.ClienteRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class ClienteServiceTest {

    private static final String CPF = "52998224725";
    private static final EnderecoCommand ENDERECO =
            new EnderecoCommand("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000");

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void deveCriarClienteQuandoCpfNaoExiste() {
        when(clienteRepository.existsByCpf(CPF)).thenReturn(false);
        when(clienteRepository.create(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente criado = clienteService.execute(
                new CreateClienteCommand("Maria", CPF, "maria@example.com", "11999999999", ENDERECO));

        assertThat(criado.getNome()).isEqualTo("Maria");
        assertThat(criado.getCpf().getValue()).isEqualTo(CPF);
        assertThat(criado.getIsActive()).isTrue();
        verify(clienteRepository).create(any(Cliente.class));
    }

    @Test
    void deveRejeitarCriacaoComCpfJaCadastrado() {
        when(clienteRepository.existsByCpf(CPF)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.execute(
                new CreateClienteCommand("Maria", CPF, "maria@example.com", null, ENDERECO)))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(409));

        verify(clienteRepository, never()).create(any(Cliente.class));
    }

    @Test
    void deveAtualizarClienteExistente() {
        Cliente existente = novoCliente("Maria");
        when(clienteRepository.findById(existente.getId())).thenReturn(Optional.of(existente));
        when(clienteRepository.update(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente atualizado = clienteService.update(existente.getId(), new UpdateClienteCommand(
                "Joao", "joao@example.com", "11988887777",
                new EnderecoCommand("Rua B", "20", null, "Centro", "Campinas", "SP", "13010000")));

        assertThat(atualizado.getNome()).isEqualTo("Joao");
        assertThat(atualizado.getEmail().getValue()).isEqualTo("joao@example.com");
        assertThat(atualizado.getCpf().getValue()).isEqualTo(CPF);
    }

    @Test
    void deveRetornar404AoAtualizarClienteInexistente() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.update(id, new UpdateClienteCommand(
                "Joao", "joao@example.com", null, ENDERECO)))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(404));
    }

    @Test
    void deveRetornar404AoBuscarClienteInexistente() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findById(id))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(404));
    }

    @Test
    void deveDelegarListagemEContagem() {
        when(clienteRepository.findAll(0, 20, "nome,asc", "Maria", true)).thenReturn(List.of());
        when(clienteRepository.countAll("Maria", true)).thenReturn(0L);

        assertThat(clienteService.findAll(0, 20, "nome,asc", "Maria", true)).isEmpty();
        assertThat(clienteService.countAll("Maria", true)).isZero();
    }

    @Test
    void deveDelegarSoftDeleteEReativacao() {
        Cliente inativo = Cliente.reconstitute(
                UUID.randomUUID(), "Maria", CPF, "maria@example.com", null,
                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000",
                java.time.LocalDateTime.now(), Boolean.FALSE);
        when(clienteRepository.findById(inativo.getId())).thenReturn(Optional.of(inativo));
        when(clienteRepository.existsByCpf(CPF)).thenReturn(false);

        clienteService.delete(inativo.getId());
        clienteService.reactivate(inativo.getId());

        verify(clienteRepository).markAsDeleted(inativo.getId());
        verify(clienteRepository).reactivate(inativo.getId());
    }

    @Test
    void naoDeveReativarClienteQuandoCpfJaPertenceAOutroAtivo() {
        Cliente inativo = Cliente.reconstitute(
                UUID.randomUUID(), "Maria", CPF, "maria@example.com", null,
                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000",
                java.time.LocalDateTime.now(), Boolean.FALSE);
        when(clienteRepository.findById(inativo.getId())).thenReturn(Optional.of(inativo));
        when(clienteRepository.existsByCpf(CPF)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.reactivate(inativo.getId()))
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getStatus()).isEqualTo(409));

        verify(clienteRepository, never()).reactivate(inativo.getId());
    }

    private static Cliente novoCliente(String nome) {
        return Cliente.create(
                nome, CPF, "maria@example.com", "11999999999",
                "Rua A", "10", null, "Centro", "Sao Paulo", "SP", "01001000");
    }
}
