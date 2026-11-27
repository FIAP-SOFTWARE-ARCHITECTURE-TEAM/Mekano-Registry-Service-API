package com.fiap.mekano.registry.domain.port.in;

/**
 * Comando de atualizacao de cliente. CPF nao faz parte do comando.
 */
public record UpdateClienteCommand(
        String nome,
        String email,
        String telefone,
        EnderecoCommand endereco
) {
}
