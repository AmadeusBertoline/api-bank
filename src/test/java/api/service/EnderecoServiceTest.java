package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import api.dto.EnderecoRequestDTO;
import api.dto.EnderecoResponseDTO;
import api.enums.StatusConta;
import api.enums.TipoConta;
import api.enums.TipoRole;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.EnderecoRepository;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private EnderecoService enderecoService;

    private Usuario usuarioExistente;
    private Endereco enderecoExistente;
    private Conta contaExistente;
    private EnderecoRequestDTO enderecoRequestDTO;

    private Usuario usuarioDestino;
    private Endereco enderecoDestino;
    private Conta contaDestino;

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

        usuarioExistente.setConta(contaExistente);

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

        contaDestino = new Conta();
        contaDestino.setId(2L);
        contaDestino.setUsuario(usuarioDestino);
        contaDestino.setStatus(StatusConta.ATIVA);

        usuarioDestino.setConta(contaDestino);
    }

    @Test
    @DisplayName("Deve atualizar endereço com sucesso")
    void deveAtualizarEnderecoComSucesso() {
        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(enderecoRepository.findById(enderecoExistente.getId())).thenReturn(Optional.of(enderecoExistente));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoExistente);

        // ACT
        EnderecoResponseDTO resultado = enderecoService.atualizar(enderecoRequestDTO);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.cep()).isEqualTo(enderecoRequestDTO.cep());
        assertThat(resultado.id()).isEqualTo(enderecoExistente.getId());

        verify(enderecoRepository, times(1)).save(any(Endereco.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar endereço com conta bloqueada")
    void deveLancarExcecaoAoAtualizarEnderecoComContaBloqueada() {
        // ARRANGE
        contaExistente.setStatus(StatusConta.BLOQUEADA);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);

        // ACT + ASSERT
        assertThatThrownBy(() -> enderecoService.atualizar(enderecoRequestDTO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Sua conta está bloqueada, você não pode realizar transações nem alterações");

        verify(enderecoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao não encontrar usuário logado ao atualizar endereço")
    void deveLancarExcecaoAoNaoEncontrarUsuarioLogadoAoAtualizarEndereco() {
        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado())
                .thenThrow(new ResourceNotFoundException("Usuário inexistente"));

        // ACT + ASSERT
        assertThatThrownBy(() -> enderecoService.atualizar(enderecoRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário inexistente");

        verify(enderecoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando endereço não for encontrado ao atualizar")
    void deveLancarExcecaoEnderecoNaoEncontradoAoAtualizar() {
        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(enderecoRepository.findById(usuarioExistente.getEndereco().getId())).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> enderecoService.atualizar(enderecoRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Endereço não encontrado de id " + usuarioExistente.getEndereco().getId());

        verify(enderecoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar endereço de outro usuário")
    void deveLancarExcecaoAoTentarAtualizarEnderecoDeOutroUsuario() {
        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(enderecoRepository.findById(usuarioExistente.getEndereco().getId()))
                .thenReturn(Optional.of(enderecoDestino));

        // ACT + ASSERT
        assertThatThrownBy(() -> enderecoService.atualizar(enderecoRequestDTO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Um usuário só pode alterar o próprio endereço");

        verify(enderecoRepository, never()).save(any());
    }
}