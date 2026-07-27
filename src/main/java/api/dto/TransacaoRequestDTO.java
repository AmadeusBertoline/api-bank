package api.dto;

import java.math.BigDecimal;
import api.enums.TipoTransacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransacaoRequestDTO {

    @NotNull(message = "O tipo da transação é obrigatório (DEPOSITO, SAQUE, TRANSFERENCIA)")
    private TipoTransacao tipo;

    private String numeroContaDestino;

    @NotNull(message = "O valor da transação é obrigatório")
    @Positive(message = "O valor da transação deve ser maior que zero")
    private BigDecimal valor;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String descricao;

}
