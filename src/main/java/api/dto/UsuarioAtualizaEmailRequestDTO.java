package api.dto;

import api.validation.EmailValido;
import lombok.Data;

@Data
public class UsuarioAtualizaEmailRequestDTO {

    @EmailValido
    private String email;
    
}
