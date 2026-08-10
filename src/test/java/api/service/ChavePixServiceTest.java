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
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import api.dto.chavePix.ChavePixRequestDTO;
import api.dto.chavePix.ChavePixResponseDTO;
import api.enums.StatusConta;
import api.enums.TipoChavePix;
import api.enums.TipoConta;
import api.enums.TipoRole;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.ChavePix;
import api.model.Conta;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.ChavePixRepository;
import api.repository.ContaRepository;

@ExtendWith(MockitoExtension.class)
public class ChavePixServiceTest {

    @Mock
    private ChavePixRepository chavePixRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private ContaRepository contaRepository;

    @InjectMocks
    private ChavePixService chavePixService;

    // CONTA ORIGEM
    private ChavePixRequestDTO chavePixRequestDTO;
    private Endereco enderecoExistente;
    private Conta contaExistente;
    private Usuario usuarioExistente;
    private ChavePix chavePix;

    // CONTA DESTINO
    private Usuario usuarioDestino;
    private Endereco enderecoDestino;
    private Conta contaDestino;
    private ChavePix chavePixDestino;

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
        contaExistente.setAgencia("0001");
        contaExistente.setNumeroConta("0001-1");
        contaExistente.setDigito("1");
        contaExistente.setSaldo(new BigDecimal("1000.00"));
        contaExistente.setTipoConta(TipoConta.PAGAMENTO);
        contaExistente.setStatus(StatusConta.ATIVA);
        contaExistente.setDataCriacao(LocalDateTime.now());

        usuarioExistente.setConta(contaExistente);

        chavePixRequestDTO = new ChavePixRequestDTO(usuarioExistente.getCpf());

        chavePix = new ChavePix();
        chavePix.setId(1L);
        chavePix.setChave(usuarioExistente.getCpf());
        chavePix.setTipo(TipoChavePix.CPF);
        chavePix.setConta(contaExistente);

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
        contaDestino.setAgencia("0001");
        contaDestino.setNumeroConta("0002-2");
        contaDestino.setDigito("2");
        contaDestino.setSaldo(new BigDecimal("500.00"));
        contaDestino.setTipoConta(TipoConta.PAGAMENTO);
        contaDestino.setStatus(StatusConta.ATIVA);
        contaDestino.setDataCriacao(LocalDateTime.now());

        usuarioDestino.setConta(contaDestino);

        chavePixDestino = new ChavePix();
        chavePixDestino.setId(2L);
        chavePixDestino.setChave("12345678901");
        chavePixDestino.setTipo(TipoChavePix.CPF);
        chavePixDestino.setConta(contaDestino);

    }

    @Test
    @DisplayName("Deve cadastrar uma chave Pix com sucesso")
    void deveCriarChavePixComSucesso() {

        // ARRANGE
        when(chavePixRepository.findByChave(chavePixRequestDTO.chave())).thenReturn(Optional.empty());
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(chavePixRepository.save(any(ChavePix.class))).thenReturn(chavePix);

        // ACT
        ChavePixResponseDTO resultado = chavePixService.cadastrar(chavePixRequestDTO);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.chave()).isEqualTo(chavePixRequestDTO.chave());
        assertThat(resultado.contaId()).isEqualByComparingTo(contaExistente.getId());

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar chave Pix com conta bloqueada")
    void deveLancarExcecaoAoCadastrarChaveComContaBloqueada() {

        // ARRANGE
        contaExistente.setStatus(StatusConta.BLOQUEADA);
        when(chavePixRepository.findByChave(chavePixRequestDTO.chave())).thenReturn(Optional.empty());
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);

        // ACT + ASSERT
        assertThatThrownBy(() -> chavePixService.cadastrar(chavePixRequestDTO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Sua conta está bloqueada, você não pode realizar transações nem alterações de chave pix");

        verify(chavePixRepository, never()).save(any());

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar CPF de terceiros como sua chave Pix")
    void deveLancarExcecaoCpfIncorreto() {

        // ARRANGE
        chavePixRequestDTO = new ChavePixRequestDTO("90594260027");
        when(chavePixRepository.findByChave(chavePixRequestDTO.chave())).thenReturn(Optional.empty());
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));

        // ACT + ASSERT
        assertThatThrownBy(() -> chavePixService.cadastrar(chavePixRequestDTO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Você não pode usar o CPF de terceiros");

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar uma chave Pix já cadastrada")
    void deveLancarExcecaoChavePixJaCadastrada() {

        // ARRANGE
        chavePixRequestDTO = new ChavePixRequestDTO("57561884010");
        when(chavePixRepository.findByChave(chavePixRequestDTO.chave())).thenReturn(Optional.of(chavePix));

        // ACT + ASSERT
        assertThatThrownBy(() -> chavePixService.cadastrar(chavePixRequestDTO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Chave pix já cadastrada");

    }

    @Test
    @DisplayName("Deve lançar exceção quando a conta não for encontrada")
    void deveLancarExcecaoContaNaoEncontrada() {

        // ARRANGE
        when(chavePixRepository.findByChave(chavePixRequestDTO.chave())).thenReturn(Optional.empty());
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> chavePixService.cadastrar(chavePixRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conta não encontrada");

        verify(chavePixRepository, never()).save(any());

    }

    @Test
    @DisplayName("Deve listar as chaves Pix com sucesso")
    void deveListarChavesPixComSucesso() {

        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(chavePixRepository.findAllByContaId(contaExistente.getId())).thenReturn(List.of(chavePix));

        // ACT
        List<ChavePixResponseDTO> resultado = chavePixService.listarChavesPix();

        // ASSERT
        assertThat(resultado).isNotEmpty();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).chave()).isEqualTo(chavePix.getChave());
        assertThat(resultado.get(0).tipo()).isEqualTo(chavePix.getTipo());

    }

    @Test
    @DisplayName("Deve lançar exceção ao listar chaves Pix sem usuário logado")
    void deveLancarExcecaoAoListarChavePixSemUsuarioLogado() {

        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado())
                .thenThrow(new ResourceNotFoundException("Usuário inexistente"));

        // ACT + ASSERT
        assertThatThrownBy(() -> chavePixService.listarChavesPix())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário inexistente");

    }

    @Test
    @DisplayName("Deve deletar uma chave Pix com sucesso")
    void deveDeletarChavePixComSucesso() {

        // ARRANGE
        when(chavePixRepository.findById(1L)).thenReturn(Optional.of(chavePix));
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);

        // ACT
        chavePixService.deletar(1L);

        // ASSERT
        verify(chavePixRepository, times(1)).delete(any());

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar chave Pix com conta bloqueada")
    void deveLancarExcecaoAoDeletarChaveComContaBloqueada() {

        // ARRANGE
        contaExistente.setStatus(StatusConta.BLOQUEADA);
        when(chavePixRepository.findById(1L)).thenReturn(Optional.of(chavePix));
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);

        // ACT + ASSERT
        assertThatThrownBy(() -> chavePixService.deletar(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Sua conta está bloqueada, você não pode realizar transações nem alterações de chave pix");

        verify(chavePixRepository, never()).delete(any());

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar uma chave Pix inexistente")
    void deveLancarExcecaoAoNaoAcharChave() {

        // ARRANGE
        when(chavePixRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> chavePixService.deletar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Chave pix não encontrada de id " + 99L);

        verify(chavePixRepository, never()).delete(any());

    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar uma chave Pix de outro usuário")
    void deveLancarExcecaoNaoPodeDeletarChaveDeOutrosUsuarios() {

        // ARRANGE
        when(chavePixRepository.findById(2L)).thenReturn(Optional.of(chavePixDestino));
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);

        // ACT + ASSERT
        assertThatThrownBy(() -> chavePixService.deletar(2L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Somente a conta dona da chave pode altera-la");

        verify(chavePixRepository, never()).delete(any());

    }

}