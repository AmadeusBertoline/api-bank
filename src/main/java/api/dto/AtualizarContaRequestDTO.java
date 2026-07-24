package api.dto;

import api.enums.TipoConta;
import lombok.Data;

@Data
public class AtualizarContaRequestDTO {

    private TipoConta tipoConta;
    
}
