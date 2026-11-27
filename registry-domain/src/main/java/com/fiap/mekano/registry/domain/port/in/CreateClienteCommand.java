package com.fiap.mekano.registry.domain.port.in;

/**
 * Comando de criacao de cliente. CPF nao pode ser alterado depois da criacao.
 */
public record CreateClienteCommand(
        String nome,
        String cpf,
        String email,
        String telefone,
        EnderecoCommand endereco
) {
}
