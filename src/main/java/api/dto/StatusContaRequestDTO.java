package api.dto;

import api.validation.StatusAtivoValido;
import lombok.Data;

@Data
public class StatusContaRequestDTO{

    @StatusAtivoValido
    private Boolean ativa;

}

