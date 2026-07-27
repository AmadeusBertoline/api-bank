package api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PixRequestDTO {

    @NotBlank(message = "A chave Pix é obrigatória")
    private String chavePix;

    @NotNull(message = "O valor é obrigatório")
    @Positive(message = "O valor da transferência deve ser maior que zero")
    private BigDecimal valor;

    @Size(max = 140, message = "A mensagem não pode exceder 140 caracteres")
    private String descricao;
}