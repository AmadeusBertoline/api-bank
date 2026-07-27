package api.dto;

import api.enums.TipoChavePix;
import lombok.Data;

@Data
public class ChavePixResponseDTO{

    private Long id;
    private String chave;
    private TipoChavePix tipo;
    private Long contaId;

}

    