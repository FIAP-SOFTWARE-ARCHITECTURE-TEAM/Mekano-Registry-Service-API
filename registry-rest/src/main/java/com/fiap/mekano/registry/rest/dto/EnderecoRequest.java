package com.fiap.mekano.registry.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Endereco do cliente")
public class EnderecoRequest {

    @NotBlank(message = "Logradouro e obrigatorio")
    @Size(max = 255)
    @Schema(required = true, example = "Rua Exemplo")
    private String logradouro;

    @Size(max = 20)
    @Schema(example = "100")
    private String numero;

    @Size(max = 100)
    @Schema(example = "Apto 12")
    private String complemento;

    @Size(max = 100)
    @Schema(example = "Centro")
    private String bairro;

    @NotBlank(message = "Cidade e obrigatoria")
    @Size(max = 100)
    @Schema(required = true, example = "Sao Paulo")
    private String cidade;

    @NotBlank(message = "UF e obrigatoria")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF deve ter exatamente 2 letras")
    @Schema(required = true, example = "SP")
    private String uf;

    @NotBlank(message = "CEP e obrigatorio")
    @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP deve ter 8 digitos")
    @Schema(required = true, example = "01001000")
    private String cep;
}
