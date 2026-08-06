package api.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import api.enums.TipoTransacao;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record TransacaoResponseDTO(
        String titularConta,
        String titularContaDestino,
        TipoTransacao tipo,
        BigDecimal valor,
        String descricao,
        LocalDateTime dataHora) implements Serializable {
}