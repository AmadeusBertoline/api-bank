package api.dto;

import lombok.Data;
import api.model.Usuario;
import jakarta.validation.constraints.NotNull;

@Data
public class ContaRequestDTO {

    @NotNull(message = "O usuário é obrigatório")
    private Usuario usuario;

}
