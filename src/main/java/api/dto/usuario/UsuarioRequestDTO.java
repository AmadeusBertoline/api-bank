package api.dto.usuario;

import java.time.LocalDate;

import api.dto.endereco.EnderecoRequestDTO;
import api.validation.CpfValido;
import api.validation.EmailValido;
import api.validation.EnderecoValido;
import api.validation.NomeValido;
import api.validation.PeloMenos16Anos;
import api.validation.SenhaValida;

public record UsuarioRequestDTO(
        @NomeValido String nome,
        @EmailValido String email,
        @SenhaValida String senha,
        @SenhaValida String confirmarSenha,
        @CpfValido String cpf,
        @PeloMenos16Anos LocalDate dataNascimento,
        @EnderecoValido EnderecoRequestDTO endereco) {
}