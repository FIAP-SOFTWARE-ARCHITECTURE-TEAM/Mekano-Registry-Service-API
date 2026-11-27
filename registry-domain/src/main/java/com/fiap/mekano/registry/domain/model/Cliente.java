package com.fiap.mekano.registry.domain.model;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import com.fiap.mekano.registry.domain.valueobject.Cpf;
import com.fiap.mekano.registry.domain.valueobject.Email;
import com.fiap.mekano.registry.domain.valueobject.Endereco;
import com.fiap.mekano.registry.domain.valueobject.Telefone;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de dominio Cliente - POJO puro, sem anotacoes JPA.
 *
 * <p>Criacao apenas via {@link #create} ou {@link #reconstitute}. O CPF e
 * imutavel apos a criacao; os demais dados cadastrais podem ser atualizados.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class Cliente {

    private final UUID id;
    private String nome;
    private final Cpf cpf;
    private Email email;
    private Telefone telefone;
    private Endereco endereco;
    private final LocalDateTime createdAt;
    private final Boolean isActive;

    public static Cliente create(
            String nome,
            String cpfValue,
            String emailValue,
            String telefoneValue,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf,
            String cep) {

        validarNome(nome);

        return Cliente.builder()
                .id(UUID.randomUUID())
                .nome(nome.strip())
                .cpf(new Cpf(cpfValue))
                .email(new Email(emailValue))
                .telefone(toTelefone(telefoneValue))
                .endereco(new Endereco(logradouro, numero, complemento, bairro, cidade, uf, cep))
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    public static Cliente reconstitute(
            UUID id,
            String nome,
            String cpfValue,
            String emailValue,
            String telefoneValue,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf,
            String cep,
            LocalDateTime createdAt,
            Boolean isActive) {

        return Cliente.builder()
                .id(id)
                .nome(nome)
                .cpf(new Cpf(cpfValue))
                .email(new Email(emailValue))
                .telefone(toTelefone(telefoneValue))
                .endereco(new Endereco(logradouro, numero, complemento, bairro, cidade, uf, cep))
                .createdAt(createdAt)
                .isActive(isActive == null ? Boolean.TRUE : isActive)
                .build();
    }

    public void atualizar(
            String nome,
            String emailValue,
            String telefoneValue,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf,
            String cep) {

        validarNome(nome);
        this.nome = nome.strip();
        this.email = new Email(emailValue);
        this.telefone = toTelefone(telefoneValue);
        this.endereco = new Endereco(logradouro, numero, complemento, bairro, cidade, uf, cep);
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new AppException(400, Messages.get("cliente.nome.required"));
        }
    }

    private static Telefone toTelefone(String value) {
        return value != null && !value.isBlank() ? new Telefone(value) : null;
    }
}
