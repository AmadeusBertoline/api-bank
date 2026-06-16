package api.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ContaResponseDTO {
    
    private Long id;
    private String titular;
    private String numeroConta;
    private BigDecimal saldo;
    private String tipoConta;
    private Boolean ativa;
    private String dataCriacao;

}