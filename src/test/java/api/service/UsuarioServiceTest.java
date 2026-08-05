package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import api.dto.EnderecoResponseDTO;
import api.dto.UsuarioAtualizaEmailRequestDTO;
import api.dto.UsuarioResponseDTO;
import api.enums.TipoRole;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
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

    @InjectMocks
    private UsuarioService usuarioService;

    private Endereco enderecoExistente;
    private Usuario usuarioExistente;
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

        // INSTANCIAÇÃO DO RECORD
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
    @DisplayName("Deve lançar exceção ao atualizar e-mail quando o e-mail já estiver em uso")
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

}