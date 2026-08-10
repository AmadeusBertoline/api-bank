package api.dto.conta;

import api.model.Usuario;
import jakarta.validation.constraints.NotNull;

public record ContaRequestDTO(
    @NotNull(message = "O usuário é obrigatório")
    Usuario usuario
) {}