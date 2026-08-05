package api.dto;

import java.math.BigDecimal;

import api.validation.ValorPositivoValido;

public record LimiteRequestDTO(
        @ValorPositivoValido BigDecimal limite) {
}
