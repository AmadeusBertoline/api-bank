package api.dto.usuario;

import api.validation.EmailValido;

public record UsuarioAtualizaEmailRequestDTO(
    @EmailValido String email
) {}