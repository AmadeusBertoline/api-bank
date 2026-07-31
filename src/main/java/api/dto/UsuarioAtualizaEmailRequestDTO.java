package api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioAtualizaEmailRequestDTO {

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email incorreto")
    private String email;
    
}
