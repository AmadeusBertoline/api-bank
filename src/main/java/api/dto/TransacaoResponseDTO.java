package api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TransacaoResponseDTO {
    private Long id;
    private Long contaId;
    private String titularConta;
    private Long contaDestinoId;
    private String titularContaDestino;
    private String tipo;
    private BigDecimal valor;
    private String descricao;
    private LocalDateTime dataHora;
}
