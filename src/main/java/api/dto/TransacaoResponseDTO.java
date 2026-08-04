package api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import api.enums.TipoTransacao;

public record TransacaoResponseDTO(
    String titularConta,
    String titularContaDestino,
    TipoTransacao tipo,
    BigDecimal valor,
    String descricao,
    LocalDateTime dataHora
) {}