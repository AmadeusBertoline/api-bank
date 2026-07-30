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
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import api.dto.EnderecoRequestDTO;
import api.dto.EnderecoResponseDTO;
import api.enums.TipoRole;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.ChavePix;
import api.model.Conta;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.EnderecoRepository;

@ExtendWith(MockitoExtension.class)
public class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private EnderecoService enderecoService;

    private Usuario usuarioExistente;
    private Endereco enderecoExistente;
    private EnderecoRequestDTO enderecoRequestDTO;

    private Usuario usuarioDestino;
    private Endereco enderecoDestino;
    private Conta contaDestino;
    private ChavePix chavePixDestino;

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

        enderecoDestino = new Endereco();
        enderecoDestino.setId(2L);
        enderecoDestino.setLogradouro("Avenida Atlântica");
        enderecoDestino.setNumero("1702");
        enderecoDestino.setComplemento("Apto 501");
        enderecoDestino.setBairro("Copacabana");
        enderecoDestino.setCidade("Rio de Janeiro");
        enderecoDestino.setUf("RJ");
        enderecoDestino.setCep("22021-001");

        usuarioDestino = new Usuario();
        usuarioDestino.setId(2L);
        usuarioDestino.setNome("Maria Silva");
        usuarioDestino.setEmail("maria.silva@email.com");
        usuarioDestino.setSenha("$2a$10$vQ3E9V7zG3P7kR9sX8zOueH7yvK2eD5mN6qL1rBtYwG");
        usuarioDestino.setCpf("12345678901");
        usuarioDestino.setDataNascimento(LocalDate.parse("1995-10-15"));
        usuarioDestino.setRole(TipoRole.ROLE_USUARIO);
        usuarioDestino.setDataCriacao(LocalDateTime.now());

        usuarioDestino.setEndereco(enderecoDestino);
        enderecoDestino.setUsuario(usuarioDestino);

    }

    @Test
    void deveAtualizarEnderecoComSucesso() {

        // ARRANGE
        when(usuarioService.buscarUsuarioLogado()).thenReturn(usuarioExistente);
        when(enderecoRepository.findById(enderecoExistente.getId())).thenReturn(Optional.of(enderecoExistente));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoExistente);

        // ACT
        EnderecoResponseDTO resultado = enderecoService.atualizar(enderecoExistente.getId(), enderecoRequestDTO);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCep()).isEqualTo(enderecoRequestDTO.getCep());
        assertThat(resultado.getId()).isEqualTo(enderecoExistente.getId());

        verify(enderecoRepository, times(1)).save(any(Endereco.class));

    }

    @Test
    void excecaoAoNaoEncontrarUsuarioLogadoAoAtualizarEndereco() {

        // ARRANGE
        when(usuarioService.buscarUsuarioLogado()).thenThrow(new ResourceNotFoundException("Não há um usuário logado"));

        // ACT + ASSERT
        assertThatThrownBy(() -> enderecoService.atualizar(enderecoExistente.getId(), enderecoRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Não há um usuário logado");

        verify(enderecoRepository, never()).save(any());

    }

    @Test
    void excecaoEnderecoNaoEncontradoAoAtualizar() {

        // ARRANGE
        when(usuarioService.buscarUsuarioLogado()).thenReturn(usuarioExistente);
        when(enderecoRepository.findById(2L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> enderecoService.atualizar(2L, enderecoRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Endereço não encontrado de id " + 2L);

        verify(enderecoRepository, never()).save(any());

    }

    @Test
    void excecaoNaoPodeAlterarEnderecoDeTerceiros() {

        // ARRANGE
        when(usuarioService.buscarUsuarioLogado()).thenReturn(usuarioDestino);
        when(enderecoRepository.findById(enderecoExistente.getId())).thenReturn(Optional.of(enderecoExistente));

        // ACT + ASSERT
        assertThatThrownBy(() -> enderecoService.atualizar(1L, enderecoRequestDTO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Um usuário só pode alterar o próprio endereço");

        verify(enderecoRepository, never()).save(any());

    }

}
