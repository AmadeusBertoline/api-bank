package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import api.dto.EnderecoRequestDTO;
import api.dto.LoginRequestDTO;
import api.dto.LoginResponseDTO;
import api.dto.UsuarioRequestDTO;
import api.enums.StatusConta;
import api.enums.TipoConta;
import api.enums.TipoRole;
import api.exception.RegraNegocioException;
import api.model.Conta;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.UsuarioRepository;
import api.security.JwtService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ContaService contaService;

    @InjectMocks
    private AuthService authService;

    private EnderecoRequestDTO enderecoRequestDTO;
    private UsuarioRequestDTO usuarioRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private Usuario usuarioExistente;
    private Endereco enderecoExistente;
    private Conta contaExistente;

    @BeforeEach
    void setup() {

        enderecoRequestDTO = new EnderecoRequestDTO(
                "Avenida Paulista",
                "1000",
                "Apto 42",
                "Bela Vista",
                "São Paulo",
                "SP",
                "01310-100");

        usuarioRequestDTO = new UsuarioRequestDTO(
                "Amadeus Bertoline",
                "amadeus@email.com",
                "Senha@123",
                "Senha@123",
                "57561884010",
                LocalDate.parse("1998-05-20"),
                enderecoRequestDTO);

        enderecoExistente = new Endereco();
        enderecoExistente.setId(1L);
        enderecoExistente.setLogradouro("Avenida Paulista");
        enderecoExistente.setNumero("1000");
        enderecoExistente.setComplemento("Apto 42");
        enderecoExistente.setBairro("Bela Vista");
        enderecoExistente.setCidade("São Paulo");
        enderecoExistente.setUf("SP");
        enderecoExistente.setCep("01310-100");

        usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);
        usuarioExistente.setNome("Amadeus Bertoline");
        usuarioExistente.setEmail("amadeus@email.com");
        usuarioExistente.setSenha("$2a$10$vQ3E9V7zG3P7kR9sX8zOueH7yvK2eD5mN6qL1rBtYwG");
        usuarioExistente.setCpf("57561884010");
        usuarioExistente.setDataNascimento(LocalDate.parse("1998-05-20"));
        usuarioExistente.setRole(TipoRole.ROLE_USUARIO);
        usuarioExistente.setDataCriacao(LocalDateTime.now());
        
        usuarioExistente.setEndereco(enderecoExistente);
        enderecoExistente.setUsuario(usuarioExistente);

        contaExistente = new Conta();
        contaExistente.setId(1L);
        contaExistente.setUsuario(usuarioExistente);
        contaExistente.setAgencia("0001");
        contaExistente.setNumeroConta("0001-1");
        contaExistente.setDigito("1");
        contaExistente.setSaldo(new BigDecimal("1000.00"));
        contaExistente.setTipoConta(TipoConta.PAGAMENTO);
        contaExistente.setStatus(StatusConta.ATIVA);
        contaExistente.setDataCriacao(LocalDateTime.now());
        contaExistente.setLimiteDiario(new BigDecimal("500.00"));

        usuarioExistente.setConta(contaExistente);

        loginRequestDTO = new LoginRequestDTO(usuarioExistente.getEmail(), "Senha@123");

    }

    @Test
    @DisplayName("Deve registrar um usuário com sucesso")
    void deveCriarUsuarioComSucesso() {

        // ARRANGE

        // ACT
        String resultado = authService.registrarUsuario(usuarioRequestDTO, usuarioExistente.getRole());

        // ASSERT
        assertThat(resultado).isEqualTo("Usuário registrado com sucesso");

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar um e-mail já cadastrado")
    void deveLancarExcecaoEmailJaCadastrado() {

        // ARRANGE
        when(usuarioRepository.findByEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(usuarioExistente));

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.registrarUsuario(usuarioRequestDTO, usuarioExistente.getRole()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Email já cadastrado");

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar um CPF já cadastrado")
    void deveLancarExcecaoCpfJaCadastrado() {

        // ARRANGE
        when(usuarioRepository.findByCpf(usuarioExistente.getCpf())).thenReturn(Optional.of(usuarioExistente));

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.registrarUsuario(usuarioRequestDTO, usuarioExistente.getRole()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("CPF já cadastrado");

    }

    @Test
    @DisplayName("Deve lançar exceção quando as senhas forem diferentes")
    void deveLancarExcecaoSenhasDiferentes() {

        // ARRANGE
        UsuarioRequestDTO dtoSenhasDiferentes = new UsuarioRequestDTO(
                usuarioRequestDTO.nome(),
                usuarioRequestDTO.email(),
                usuarioRequestDTO.senha(),
                "batata",
                usuarioRequestDTO.cpf(),
                usuarioRequestDTO.dataNascimento(),
                usuarioRequestDTO.endereco());

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.registrarUsuario(dtoSenhasDiferentes, usuarioExistente.getRole()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("As senhas devem ser iguais");

    }

    @Test
    @DisplayName("Deve realizar login com sucesso")
    void deveLogarComSucesso() {

        // ARRANGE
        String token = "token_simulado123@";
        when(usuarioRepository.findByEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(usuarioExistente));
        when(jwtService.gerarToken(usuarioExistente.getId(), usuarioExistente.getEmail(), usuarioExistente.getRole()))
                .thenReturn(token);
        when(passwordEncoder.matches(loginRequestDTO.senha(), usuarioExistente.getSenha())).thenReturn(true);

        // ACT
        LoginResponseDTO resultado = authService.login(loginRequestDTO);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.token()).isEqualTo(token);
        assertThat(resultado.tipo()).isEqualTo("Bearer");

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar realizar login com e-mail incorreto")
    void deveLancarExcecaoEmailIncorreto() {

        // ARRANGE
        LoginRequestDTO loginEmailIncorreto = new LoginRequestDTO("emailfakeerrado@email.com", loginRequestDTO.senha());
        when(usuarioRepository.findByEmail(loginEmailIncorreto.email())).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.login(loginEmailIncorreto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Email ou senha inválidos");

        verify(jwtService, never()).gerarToken(any(), any(), any());

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar realizar login com senha incorreta")
    void deveLancarExcecaoSenhaIncorreta() {

        // ARRANGE
        LoginRequestDTO loginSenhaIncorreta = new LoginRequestDTO(loginRequestDTO.email(), "senhamuitoerradanadaaver");
        when(usuarioRepository.findByEmail(loginSenhaIncorreta.email())).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.matches(loginSenhaIncorreta.senha(), usuarioExistente.getSenha())).thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.login(loginSenhaIncorreta))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Email ou senha inválidos");

        verify(jwtService, never()).gerarToken(any(), any(), any());

    }

}