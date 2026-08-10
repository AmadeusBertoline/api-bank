package api.dto.chavePix;

import api.validation.ChavePixValida;
import jakarta.validation.constraints.NotBlank;

public record ChavePixRequestDTO(
    @NotBlank(message = "A chave Pix é obrigatória.")
    @ChavePixValida
    String chave
) {}