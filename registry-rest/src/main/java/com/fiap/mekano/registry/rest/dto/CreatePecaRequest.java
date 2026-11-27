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
@Schema(description = "Dados para cadastro de uma peca do catalogo")
public class CreatePecaRequest {

    @NotBlank(message = "Codigo e obrigatorio")
    @Size(max = 50)
    @Schema(required = true, example = "PEA-001")
    private String codigo;

    @NotBlank(message = "Descricao e obrigatoria")
    @Size(max = 255)
    @Schema(required = true, example = "Filtro de oleo")
    private String descricao;

    @NotNull(message = "Valor unitario e obrigatorio")
    @DecimalMin(value = "0.00", inclusive = false, message = "Valor unitario deve ser maior que zero")
    @Digits(integer = 10, fraction = 2)
    @Schema(required = true, example = "45.90")
    private BigDecimal valorUnitario;
}
