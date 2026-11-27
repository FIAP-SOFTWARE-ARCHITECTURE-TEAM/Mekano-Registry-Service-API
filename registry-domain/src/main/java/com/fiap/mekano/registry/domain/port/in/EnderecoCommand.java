package com.fiap.mekano.registry.domain.port.in;

/**
 * Dados de endereco usados nos comandos de cliente.
 */
public record EnderecoCommand(
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep
) {
}
