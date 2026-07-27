package api.dto;

import api.enums.TipoChavePix;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChavePixRequestDTO{

    @NotBlank(message = "A chave é obrigatória")
    private String chave;

    @NotNull(message = "O tipo da chave Pix é obrigatório.")
    private TipoChavePix tipo;

}
