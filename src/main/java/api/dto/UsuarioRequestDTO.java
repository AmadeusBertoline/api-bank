package api.dto;

import java.time.LocalDate;
import api.validation.CpfValido;
import api.validation.EmailValido;
import api.validation.EnderecoValido;
import api.validation.MaiorDeIdade;
import api.validation.NomeValido;
import api.validation.SenhaValida;
import lombok.Data;

@Data
public class UsuarioRequestDTO {
    @NomeValido
    private String nome;

    @EmailValido
    private String email;

    @SenhaValida
    private String senha;

    @SenhaValida
    private String confirmarSenha;

    @CpfValido
    private String cpf;

    @MaiorDeIdade
    private LocalDate dataNascimento;

    @EnderecoValido
    private EnderecoRequestDTO endereco;
}
