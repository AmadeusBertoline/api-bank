package api.dto.login;

import api.enums.TipoRole;

public record LoginResponseDTO(

        String token,
        String tipo,
        Long id,
        String nome,
        TipoRole role) {
}