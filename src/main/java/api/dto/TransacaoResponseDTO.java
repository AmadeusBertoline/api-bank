package api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import api.enums.TipoTransacao;
import lombok.Data;

@Data
public class TransacaoResponseDTO {
    private String titularConta;
    private String titularContaDestino;
    private TipoTransacao tipo;
    private BigDecimal valor;
    private String descricao;
    private LocalDateTime dataHora;
}
