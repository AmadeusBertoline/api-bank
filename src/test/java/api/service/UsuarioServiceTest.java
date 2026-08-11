package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import api.dto.endereco.EnderecoResponseDTO;
import api.dto.usuario.UsuarioAtualizaEmailRequestDTO;
import api.dto.usuario.UsuarioAtualizaSenhaRequestDTO;
import api.dto.usuario.UsuarioResponseDTO;
import api.enums.StatusConta;
import api.enums.TipoConta;
import api.enums.TipoRole;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EnderecoService enderecoService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Endereco enderecoExistente;
    private Usuario usuarioExistente;
    private Conta contaExistente;
    private EnderecoResponseDTO enderecoResponseDTO;

    @BeforeEach
    void setup() {
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
        contaExistente.setStatus(StatusConta.ATIVA);
        contaExistente.setTipoConta(TipoConta.PAGAMENTO);
        usuarioExistente.setConta(contaExistente);

        enderecoResponseDTO = new EnderecoResponseDTO(1L,
                "Avenida Paulista",
                "1000",
                "Apto 42",
                "Bela Vista",
                "São Paulo",
                "SP",
                "01310-100");
    }

    @Test
    @DisplayName("Deve retornar os dados do usuário logado com sucesso")
    void deveRetornarMeusDados() {

        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(enderecoService.toDTO(enderecoExistente)).thenReturn(enderecoResponseDTO);

        // ACT
        UsuarioResponseDTO resultado = usuarioService.meusDados();

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo("Amadeus Bertoline");
        assertThat(resultado.email()).isEqualTo("amadeus@email.com");
        assertThat(resultado.endereco()).isNotNull();
        assertThat(resultado.endereco().logradouro()).isEqualTo("Avenida Paulista");

        verify(usuarioAutenticadoService, times(1)).getUsuarioLogado();
        verify(enderecoService, times(1)).toDTO(usuarioExistente.getEndereco());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar meus dados quando usuário não for encontrado")
    void deveLancarExcecaoAoBuscarMeusDadosQuandoUsuarioNaoEncontrado() {

        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado())
                .thenThrow(new ResourceNotFoundException("Usuário inexistente"));

        // ACT + ASSERT
        assertThatThrownBy(() -> usuarioService.meusDados())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário inexistente");

        verify(usuarioAutenticadoService, times(1)).getUsuarioLogado();
        verify(enderecoService, never()).toDTO(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar meus dados quando ocorrer erro no mapeamento de endereço")
    void deveLancarExcecaoAoBuscarMeusDadosQuandoErroNoMapeamentoDoEndereco() {

        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(enderecoService.toDTO(usuarioExistente.getEndereco()))
                .thenThrow(new RegraNegocioException("Erro ao converter endereço do usuário"));

        // ACT + ASSERT
        assertThatThrownBy(() -> usuarioService.meusDados())
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Erro ao converter endereço do usuário");

        verify(usuarioAutenticadoService, times(1)).getUsuarioLogado();
        verify(enderecoService, times(1)).toDTO(usuarioExistente.getEndereco());
    }

    @Test
    @DisplayName("Deve atualizar o e-mail do usuário logado com sucesso")
    void deveAtualizarEmailComSucesso() {

        // ARRANGE
        UsuarioAtualizaEmailRequestDTO dto = new UsuarioAtualizaEmailRequestDTO("novo.email@email.com");

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enderecoService.toDTO(enderecoExistente)).thenReturn(enderecoResponseDTO);

        // ACT
        UsuarioResponseDTO resultado = usuarioService.atualizarEmail(dto);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(usuarioExistente.getEmail()).isEqualTo(dto.email());

        verify(usuarioAutenticadoService, times(1)).getUsuarioLogado();
        verify(usuarioRepository, times(1)).existsByEmail(dto.email());
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    @DisplayName("Deve permitir atualizar e-mail quando o novo e-mail for idêntico ao atual sem consultar o repositório")
    void deveAtualizarEmailQuandoEmailForOProprioEmail() {

        // ARRANGE
        UsuarioAtualizaEmailRequestDTO dto = new UsuarioAtualizaEmailRequestDTO("amadeus@email.com");

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enderecoService.toDTO(enderecoExistente)).thenReturn(enderecoResponseDTO);

        // ACT
        UsuarioResponseDTO resultado = usuarioService.atualizarEmail(dto);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.email()).isEqualTo("amadeus@email.com");

        verify(usuarioRepository, never()).existsByEmail(any());
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar e-mail quando a conta estiver bloqueada")
    void deveLancarExcecaoAoAtualizarEmailQuandoContaEstiverBloqueada() {

        // ARRANGE
        contaExistente.setStatus(StatusConta.BLOQUEADA);
        UsuarioAtualizaEmailRequestDTO dto = new UsuarioAtualizaEmailRequestDTO("novo.email@email.com");

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);

        // ACT + ASSERT
        assertThatThrownBy(() -> usuarioService.atualizarEmail(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Sua conta está bloqueada, você não pode realizar transações nem alterações");

        verify(usuarioRepository, never()).existsByEmail(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar e-mail quando o e-mail já estiver em uso por outro usuário")
    void deveLancarExcecaoQuandoEmailJaEstiverEmUso() {

        // ARRANGE
        UsuarioAtualizaEmailRequestDTO dto = new UsuarioAtualizaEmailRequestDTO("outro.usuario@email.com");

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> usuarioService.atualizarEmail(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Esse e-mail já pertence a outro usuário");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar a senha com sucesso quando os dados forem válidos")
    void atualizarSenha_ComSucesso() {
        // ARRANGE
        var dto = new UsuarioAtualizaSenhaRequestDTO("senha123", "novaSenha123", "novaSenha123");

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(passwordEncoder.matches("senha123", usuarioExistente.getSenha())).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$novoHashBcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        UsuarioResponseDTO response = usuarioService.atualizarSenha(dto);

        // ASSERT
        assertNotNull(response);
        assertEquals("$2a$10$novoHashBcrypt", usuarioExistente.getSenha());
        verify(passwordEncoder).encode("novaSenha123");
        verify(usuarioRepository).save(usuarioExistente);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha atual estiver incorreta")
    void atualizarSenha_SenhaAtualIncorreta() {
        // ARRANGE
        var dto = new UsuarioAtualizaSenhaRequestDTO("senhaIncorreta", "novaSenha123", "novaSenha123");

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(passwordEncoder.matches("senhaIncorreta", usuarioExistente.getSenha())).thenReturn(false);

        // ACT + ASSERT
        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> usuarioService.atualizarSenha(dto));

        assertEquals("A senha atual está incorreta", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a confirmação da nova senha não coincidir")
    void atualizarSenha_ConfirmacaoSenhaDiferente() {
        // ARRANGE
        var dto = new UsuarioAtualizaSenhaRequestDTO("senha123", "novaSenha123", "senhaDiferente");

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(passwordEncoder.matches("senha123", usuarioExistente.getSenha())).thenReturn(true);

        // ACT + ASSERTT
        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> usuarioService.atualizarSenha(dto));

        assertEquals("A nova senha deve ser igual a confirmação de senha", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}