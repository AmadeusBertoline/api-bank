package api.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransacaoRequestDTO {
    
    @NotNull(message = "O id da conta é obrigatório")
    private Long contaId;

    private Long contaDestinoId;

    @NotBlank(message = "O tipo da transação é obrigatório")
    private String tipo;

    @Positive(message = "O valor deve ser maior que zero")
    @NotNull(message = "O valor é obrigatório")
    private BigDecimal valor;

    private String descricao;

}
