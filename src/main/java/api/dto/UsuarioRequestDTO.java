package api.dto;

import java.time.LocalDate;
import org.hibernate.validator.constraints.br.CPF;
import api.validation.MaiorDeIdade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UsuarioRequestDTO {
    
    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    @NotBlank(message = "A senha é obrigatória")
    private String confirmarSenha;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos.")
    @CPF(message = "CPF inválido.")
    private String cpf;

    @NotNull(message = "A data de nascimento é obrigatória")
    @MaiorDeIdade(message = "O usuário precisa ser maior de idade para se cadastrar")
    private LocalDate dataNascimento;

    @NotNull(message = "O endereço é obrigatório")
    @Valid 
    private EnderecoRequestDTO endereco;

}
