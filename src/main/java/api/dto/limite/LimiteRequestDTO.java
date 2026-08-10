package api.dto.limite;

import java.math.BigDecimal;

import api.validation.ValorPositivoValido;

public record LimiteRequestDTO(
        @ValorPositivoValido BigDecimal limite) {
}
