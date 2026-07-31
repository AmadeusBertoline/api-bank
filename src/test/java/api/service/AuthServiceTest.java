package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
import api.enums.TipoRole;
import api.exception.RegraNegocioException;
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

    @BeforeEach
    void setup() {

        enderecoRequestDTO = new EnderecoRequestDTO();
        enderecoRequestDTO.setLogradouro("Avenida Paulista");
        enderecoRequestDTO.setNumero("1000");
        enderecoRequestDTO.setComplemento("Apto 42");
        enderecoRequestDTO.setBairro("Bela Vista");
        enderecoRequestDTO.setCidade("São Paulo");
        enderecoRequestDTO.setUf("SP");
        enderecoRequestDTO.setCep("01310-100");

        usuarioRequestDTO = new UsuarioRequestDTO();
        usuarioRequestDTO.setNome("Amadeus Bertoline");
        usuarioRequestDTO.setEmail("amadeus@email.com");
        usuarioRequestDTO.setCpf("57561884010");
        usuarioRequestDTO.setDataNascimento(LocalDate.parse("1998-05-20"));
        usuarioRequestDTO.setSenha("Senha@123");
        usuarioRequestDTO.setConfirmarSenha("Senha@123");
        usuarioRequestDTO.setEndereco(enderecoRequestDTO);

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

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail(usuarioExistente.getEmail());
        loginRequestDTO.setSenha("Senha@123");

    }

    @Test
    void deveCriarUsuarioComSucesso() {

        // ARRANGE

        // ACT
        String resultado = authService.registrarUsuario(usuarioRequestDTO, usuarioExistente.getRole());

        // ASSERT
        assertThat(resultado).isEqualTo("Usuário registrado com sucesso");

    }

    @Test
    void deveLancarExceptionEmailJaCadastrado() {

        // ARRANGE
        when(usuarioRepository.findByEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(usuarioExistente));

        // ACT + ASSERT

        assertThatThrownBy(() -> authService.registrarUsuario(usuarioRequestDTO, usuarioExistente.getRole()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Email já cadastrado");

    }

    @Test
    void deveLancarExceptionCpfJaCadastrado() {

        // ARRANGE
        when(usuarioRepository.findByCpf(usuarioExistente.getCpf())).thenReturn(Optional.of(usuarioExistente));

        // ACT + ASSERT

        assertThatThrownBy(() -> authService.registrarUsuario(usuarioRequestDTO, usuarioExistente.getRole()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("CPF já cadastrado");

    }

    @Test
    void deveLancarExceptionSenhasDiferentes() {

        // ARRANGE
        usuarioRequestDTO.setConfirmarSenha("batata");

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.registrarUsuario(usuarioRequestDTO, usuarioExistente.getRole()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("As senhas devem ser iguais");

    }

    @Test
    void deveLogarComSucesso() {

        // ARRANGE
        String token = "token_simulado123@";
        when(usuarioRepository.findByEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(usuarioExistente));
        when(jwtService.gerarToken(usuarioExistente.getId(), usuarioExistente.getEmail(), usuarioExistente.getRole())).thenReturn(token);
        when(passwordEncoder.matches(loginRequestDTO.getSenha(), usuarioExistente.getSenha())).thenReturn(true);

        // ACT
        LoginResponseDTO resultado = authService.login(loginRequestDTO);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getToken()).isEqualTo(token);
        assertThat(resultado.getTipo()).isEqualTo("Bearer");

    }

    @Test
    void deveLancarExceptionEmailIncorreto() {

        // ARRANGE
        loginRequestDTO.setEmail("emailfakeerrado@email.com");
        when(usuarioRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Email ou senha inválidos");

        verify(jwtService, never()).gerarToken(any(), any(), any());

    }

    @Test
    void deveLancarExcecaoSenhaIncorreta() {

        // ARRANGE
        loginRequestDTO.setSenha("senhamuitoerradanadaaver");
        when(usuarioRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.matches(loginRequestDTO.getSenha(), usuarioExistente.getSenha())).thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Email ou senha inválidos");

        verify(jwtService, never()).gerarToken(any(), any(), any());

    }

}
