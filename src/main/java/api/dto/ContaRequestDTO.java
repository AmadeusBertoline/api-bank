package api.dto;

import lombok.Data;
import java.math.BigDecimal;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Data
public class ContaRequestDTO {
    
    @NotBlank(message = "O nome do titular é obrigatório")
    private String titular;

    @NotBlank(message = "O número da conta é obrigatório")
    @Size(max = 20, message = "O número da conta pode ter no máximo 20 caracteres")
    private String numeroConta;

    @NotNull(message = "Um saldo inicial é obrigatório")
    @PositiveOrZero(message = "O saldo deve ser maior ou igual a zero")
    private BigDecimal saldo;

    @NotBlank(message = "O tipo da conta é obrigatório")
    private String tipoConta;

}
