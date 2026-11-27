package com.fiap.mekano.registry.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request de criacao de veiculo")
public class CreateVeiculoRequest {

    @NotNull(message = "Cliente e obrigatorio")
    @Schema(required = true, example = "11111111-1111-1111-1111-111111111111")
    private UUID clienteUuid;

    @NotBlank(message = "Placa e obrigatoria")
    @Size(max = 8)
    @Schema(required = true, example = "ABC1D23")
    private String placa;

    @NotBlank(message = "Marca e obrigatoria")
    @Size(max = 100)
    @Schema(required = true, example = "Volkswagen")
    private String marca;

    @NotBlank(message = "Modelo e obrigatorio")
    @Size(max = 100)
    @Schema(required = true, example = "Gol")
    private String modelo;

    @NotNull(message = "Ano e obrigatorio")
    @Min(value = 1900, message = "Ano invalido")
    @Max(value = 2100, message = "Ano invalido")
    @Schema(required = true, example = "2020")
    private Integer ano;
}
