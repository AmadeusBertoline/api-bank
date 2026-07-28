package api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusContaRequestDTO{

    @NotNull(message = "Deve escolher um valor para modificar a conta: true or false")
    private Boolean ativa;

}

