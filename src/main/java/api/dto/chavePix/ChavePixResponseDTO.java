package api.dto.chavePix;

import api.enums.TipoChavePix;

public record ChavePixResponseDTO(
    Long id,
    String chave,
    TipoChavePix tipo,
    Long contaId
) {}