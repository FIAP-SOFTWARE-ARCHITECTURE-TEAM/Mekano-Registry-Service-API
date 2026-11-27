package com.fiap.mekano.registry.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request de atualizacao de veiculo (placa imutavel)")
public class UpdateVeiculoRequest {

    @Size(max = 100)
    @Schema(example = "Volkswagen")
    private String marca;

    @Size(max = 100)
    @Schema(example = "Gol")
    private String modelo;

    @Min(value = 1900, message = "Ano invalido")
    @Max(value = 2100, message = "Ano invalido")
    @Schema(example = "2020")
    private Integer ano;
}
