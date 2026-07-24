package api.dto;

import lombok.Data;
import api.enums.TipoConta;
import jakarta.validation.constraints.NotNull;

@Data
public class ContaRequestDTO {

    @NotNull(message = "O tipo da conta é obrigatório")
    private TipoConta tipoConta;

}
