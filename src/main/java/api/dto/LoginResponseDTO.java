package api.dto;

import api.enums.TipoRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String tipo;   
    private String nome;
    private TipoRole role;
}