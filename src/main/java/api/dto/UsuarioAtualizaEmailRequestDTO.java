package api.dto;

import api.validation.EmailValido;

public record UsuarioAtualizaEmailRequestDTO(
    @EmailValido String email
) {}