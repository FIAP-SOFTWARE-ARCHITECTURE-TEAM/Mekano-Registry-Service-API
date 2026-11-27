package com.fiap.mekano.registry.domain.valueobject;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Locale;

/**
 * Value Object de endereco brasileiro.
 *
 * <ul>
 *   <li>Logradouro e cidade sao obrigatorios.</li>
 *   <li>UF normalizada para uppercase com 2 caracteres.</li>
 *   <li>CEP normalizado para 8 digitos.</li>
 *   <li>Numero, complemento e bairro sao opcionais.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode
@ToString
public final class Endereco {

    private final String logradouro;
    private final String numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;
    private final String uf;
    private final String cep;

    public Endereco(String logradouro, String numero, String complemento, String bairro,
                    String cidade, String uf, String cep) {
        if (logradouro == null || logradouro.isBlank()) {
            throw new AppException(400, Messages.get("endereco.logradouro.required"));
        }
        if (cidade == null || cidade.isBlank()) {
            throw new AppException(400, Messages.get("endereco.cidade.required"));
        }
        if (uf == null) {
            throw new AppException(400, Messages.get("endereco.uf.invalid"));
        }
        String normalizedUf = uf.strip().toUpperCase(Locale.ROOT);
        if (!normalizedUf.matches("^[A-Z]{2}$")) {
            throw new AppException(400, Messages.get("endereco.uf.invalid"));
        }
        if (cep == null) {
            throw new AppException(400, Messages.get("endereco.cep.invalid"));
        }
        String normalizedCep = cep.strip().replaceAll("\\D", "");
        if (normalizedCep.length() != 8) {
            throw new AppException(400, Messages.get("endereco.cep.invalid"));
        }
        this.logradouro = logradouro.strip();
        this.numero = trimToNull(numero);
        this.complemento = trimToNull(complemento);
        this.bairro = trimToNull(bairro);
        this.cidade = cidade.strip();
        this.uf = normalizedUf;
        this.cep = normalizedCep;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
