package api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import api.enums.StatusConta;
import api.enums.TipoConta;

public record ContaResponseDTO(
    Long id,
    String titular,
    String email,
    String numeroConta,
    BigDecimal saldo,
    TipoConta tipoConta,
    StatusConta status,
    LocalDateTime dataCriacao,
    BigDecimal limite
) {}