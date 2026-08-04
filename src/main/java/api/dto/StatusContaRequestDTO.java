package api.dto;

import api.validation.StatusAtivoValido;

public record StatusContaRequestDTO(
    @StatusAtivoValido
    Boolean ativa
) {}