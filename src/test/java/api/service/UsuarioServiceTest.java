package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
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
public class UsuarioServiceTest {

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

        enderecoResponseDTO = new EnderecoResponseDTO();
        enderecoResponseDTO.setLogradouro("Avenida Paulista");
    }

    @Test
    void deveRetornarMeusDados() {

        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(enderecoService.toDTO(enderecoExistente)).thenReturn(enderecoResponseDTO);

        // ACT
        UsuarioResponseDTO resultado = usuarioService.meusDados();

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Amadeus Bertoline");
        assertThat(resultado.getEmail()).isEqualTo("amadeus@email.com");
        assertThat(resultado.getEndereco()).isNotNull();
        assertThat(resultado.getEndereco().getLogradouro()).isEqualTo("Avenida Paulista");

        verify(usuarioAutenticadoService).getUsuarioLogado();
        verify(enderecoService).toDTO(usuarioExistente.getEndereco());

    }

    @Test
    void deveLancarExcecaoAoBuscarMeusDadosQuandoUsuarioNaoEncontrado() {

        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado())
                .thenThrow(new ResourceNotFoundException("Usuário inexistente"));

        // ACT + ASSERT
        assertThatThrownBy(() -> usuarioService.meusDados())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário inexistente");

        verify(usuarioAutenticadoService).getUsuarioLogado();
        verify(enderecoService, never()).toDTO(any());

    }

    @Test
    void deveLancarExcecaoAoBuscarMeusDadosQuandoErroNoMapeamentoDoEndereco() {

        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(enderecoService.toDTO(usuarioExistente.getEndereco()))
                .thenThrow(new RegraNegocioException("Erro ao converter endereço do usuário"));

        // ACT + ASSERT
        assertThatThrownBy(() -> usuarioService.meusDados())
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Erro ao converter endereço do usuário");

        verify(usuarioAutenticadoService).getUsuarioLogado();
        verify(enderecoService).toDTO(usuarioExistente.getEndereco());

    }

    @Test
    void deveAtualizarEmailComSucesso() {

        // ARRANGE
        UsuarioAtualizaEmailRequestDTO dto = new UsuarioAtualizaEmailRequestDTO();
        dto.setEmail("novo.email@email.com");

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(usuarioRepository.existsByEmail("novo.email@email.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

        // ACT
        UsuarioResponseDTO resultado = usuarioService.atualizar(dto);

        // ASSERT
        assertThat(resultado).isNotNull();
        verify(usuarioRepository).save(usuarioExistente);
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstiverEmUso() {

        // ARRANGE
        UsuarioAtualizaEmailRequestDTO dto = new UsuarioAtualizaEmailRequestDTO();
        dto.setEmail("outro.usuario@email.com");

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(usuarioRepository.existsByEmail("outro.usuario@email.com")).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> usuarioService.atualizar(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Esse e-mail já pertence a outro usuário");

        verify(usuarioRepository, never()).save(any());
    }

}