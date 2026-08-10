package api.dto.statusConta;

import api.validation.StatusAtivoValido;

public record StatusContaRequestDTO(
    @StatusAtivoValido
    Boolean ativa
) {}