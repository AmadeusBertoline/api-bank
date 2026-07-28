package api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import api.enums.TipoRole;
import lombok.Data;

@Data
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private LocalDate dataNascimento;
    private TipoRole role;
    private LocalDateTime dataCriacao;
    private EnderecoResponseDTO endereco;
    
}
