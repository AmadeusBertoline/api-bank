package api.dto;

import api.enums.TipoChavePix;

public record ChavePixResponseDTO(
    Long id,
    String chave,
    TipoChavePix tipo,
    Long contaId
) {}