package api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ContaRequestDTO {
    
    private String titular;
    private String numeroConta;
    private BigDecimal saldo;
    private String tipoConta;

}
