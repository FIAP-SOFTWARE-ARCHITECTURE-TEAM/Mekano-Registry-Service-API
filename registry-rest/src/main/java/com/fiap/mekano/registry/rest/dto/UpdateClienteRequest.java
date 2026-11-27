package com.fiap.mekano.registry.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request de atualizacao de cliente (CPF imutavel)")
public class UpdateClienteRequest {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 255)
    @Schema(required = true, example = "Cliente Exemplo")
    private String nome;

    @NotBlank(message = "Email e obrigatorio")
    @Email(message = "Email deve ter formato valido")
    @Size(max = 255)
    @Schema(required = true, example = "cliente.exemplo@mekano.com.br")
    private String email;

    @Size(max = 20)
    @Schema(example = "11999999999")
    private String telefone;

    @NotNull(message = "Endereco e obrigatorio")
    @Valid
    @Schema(required = true)
    private EnderecoRequest endereco;
}
