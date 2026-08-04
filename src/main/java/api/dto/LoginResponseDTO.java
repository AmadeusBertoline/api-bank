package api.dto;

import api.enums.TipoRole;

public record LoginResponseDTO(
    String token,
    String tipo,
    String nome,
    TipoRole role
) {}