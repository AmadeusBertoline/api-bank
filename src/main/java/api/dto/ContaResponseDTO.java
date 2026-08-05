package api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import api.enums.TipoConta;

public record ContaResponseDTO(
    Long id,
    String titular,
    String email,
    String numeroConta,
    BigDecimal saldo,
    TipoConta tipoConta,
    Boolean ativa,
    LocalDateTime dataCriacao,
    BigDecimal limite
) {}