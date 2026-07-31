package api.dto;

import api.validation.EmailValido;
import api.validation.SenhaValida;
import lombok.Data;

@Data
public class LoginRequestDTO {
    
    @EmailValido
    private String email;

    @SenhaValida
    private String senha;

}
