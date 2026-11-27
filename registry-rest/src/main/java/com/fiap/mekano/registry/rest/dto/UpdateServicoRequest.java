package com.fiap.mekano.registry.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Dados para atualizacao de um servico")
public class UpdateServicoRequest {

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 255)
    @Schema(required = true, example = "Troca de oleo")
    private String nome;

    @Size(max = 255)
    @Schema(example = "Troca de oleo e filtro")
    private String descricao;

    @NotNull(message = "Valor unitario e obrigatorio")
    @DecimalMin(value = "0.00", inclusive = false, message = "Valor unitario deve ser maior que zero")
    @Digits(integer = 10, fraction = 2)
    @Schema(required = true, example = "150.00")
    private BigDecimal valorUnitario;
}
