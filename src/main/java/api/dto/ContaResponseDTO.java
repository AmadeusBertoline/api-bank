package api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import api.enums.TipoConta;
import lombok.Data;

@Data
public class ContaResponseDTO {
    
    private Long id;
    private String titular;
    private String email;
    private String numeroConta;
    private BigDecimal saldo;
    private TipoConta tipoConta;
    private Boolean ativa;
    private LocalDateTime dataCriacao;

}