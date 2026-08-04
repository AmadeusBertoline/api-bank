package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import api.dto.ContaRequestDTO;
import api.dto.ContaResponseDTO;
import api.enums.TipoConta;
import api.enums.TipoRole;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.ContaRepository;
import api.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock(answer = Answers.RETURNS_SELF)
    private Query query;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private AuthService authService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ContaService contaService;

    private Endereco enderecoExistente;
    private Conta contaExistente;
    private Usuario usuarioExistente;
    private ContaRequestDTO contaRequestDTO;

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
        contaExistente.setAtiva(true);
        contaExistente.setDataCriacao(LocalDateTime.now());

        contaRequestDTO = new ContaRequestDTO();
        contaRequestDTO.setUsuario(usuarioExistente);

    }

    @Test
    void deveCriarContaComSucesso() {

        // ARRANGE
        when(contaRepository.existsByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(contaExistente);

        // ACT
        ContaResponseDTO resultado = contaService.criar(contaRequestDTO);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitular()).isEqualTo("Amadeus Bertoline");
        assertThat(resultado.getSaldo()).isEqualByComparingTo("1000.00");
        verify(contaRepository, times(1)).save(any(Conta.class));

    }

    @Test
    void deveListarTodasAsContas() {

        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dataCriacao").descending());

        Page<Conta> pageMock = new PageImpl<>(List.of(contaExistente), pageable, 1);

        when(contaRepository.findAll(pageable)).thenReturn(pageMock);

        // ACT
        Page<ContaResponseDTO> resultado = contaService.listarTodas(pageable);

        // ASSERT
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getTitular()).isEqualTo("Amadeus Bertoline");
    }

    @Test
    void deveListarMinhaContaComSucesso() {

        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));

        // ACT
        ContaResponseDTO conta = contaService.meusDados();

        // ASSERT
        assertThat(conta).isNotNull();
        assertThat(conta.getTitular()).isEqualTo(contaExistente.getUsuario().getNome());

    }

    @Test
    void deveDesativarContaComSucessoAdmin() {

        // ARRANGE
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByIdWithLock(usuarioExistente.getId())).thenReturn(Optional.of(contaExistente));
        contaExistente.setAtiva(false);
        when(contaRepository.save(contaExistente)).thenReturn(contaExistente);

        // ACT
        ContaResponseDTO resultado = contaService.desativar(contaExistente.getId());

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getAtiva()).isFalse();

    }

    @Test
    void deveAtivarContaComSucessoAdmin() {

        // ARRANGE
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByIdWithLock(usuarioExistente.getId())).thenReturn(Optional.of(contaExistente));
        contaExistente.setAtiva(true);
        when(contaRepository.save(contaExistente)).thenReturn(contaExistente);

        // ACT
        ContaResponseDTO resultado = contaService.ativar(contaExistente.getId());

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getAtiva()).isTrue();

    }

    @Test
    void deveDesativarContaComSucesso() {
        // Arrange
        contaExistente.setAtiva(true);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmailWithLock(usuarioExistente.getEmail()))
                .thenReturn(Optional.of(contaExistente));
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ContaResponseDTO resultado = contaService.desativarMinhaConta();

        // Assert
        assertNotNull(resultado);
        assertFalse(contaExistente.getAtiva(), "A conta deveria estar desativada (ativa = false)");

        verify(usuarioAutenticadoService, times(1)).getUsuarioLogado();
        verify(contaRepository, times(1)).findByUsuarioEmailWithLock(usuarioExistente.getEmail());
        verify(contaRepository, times(1)).save(contaExistente);
    }

    @Test
    void deveLancarExcecaoAoDesativarContaInexistente() {
        // Arrange
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmailWithLock(usuarioExistente.getEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> contaService.desativarMinhaConta());

        assertEquals("Conta não encontrada", exception.getMessage());
        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    void deveAtivarContaComSucesso() {
        // Arrange
        contaExistente.setAtiva(false);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmailWithLock(usuarioExistente.getEmail()))
                .thenReturn(Optional.of(contaExistente));
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ContaResponseDTO resultado = contaService.ativarMinhaConta();

        // Assert
        assertNotNull(resultado);
        assertTrue(contaExistente.getAtiva(), "A conta deveria estar ativa (ativa = true)");

        verify(usuarioAutenticadoService, times(1)).getUsuarioLogado();
        verify(contaRepository, times(1)).findByUsuarioEmailWithLock(usuarioExistente.getEmail());
        verify(contaRepository, times(1)).save(contaExistente);
    }

    @Test
    void deveLancarExcecaoAoAtivarContaInexistente() {
        // Arrange
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmailWithLock(usuarioExistente.getEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> contaService.ativarMinhaConta());

        assertEquals("Conta não encontrada", exception.getMessage());
        verify(contaRepository, never()).save(any(Conta.class));
    }
}
