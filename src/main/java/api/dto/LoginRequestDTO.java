package api.dto;

import api.validation.EmailValido;
import api.validation.SenhaValida;

public record LoginRequestDTO(
        @EmailValido String email,
        @SenhaValida String senha) {
}