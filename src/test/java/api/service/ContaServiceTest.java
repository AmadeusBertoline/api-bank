package api.service;

import api.dto.ContaRequestDTO;
import api.dto.ContaResponseDTO;
import api.enums.TipoConta;
import api.enums.TipoRole;
import api.model.Conta;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.ContaRepository;
import api.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock(answer = Answers.RETURNS_SELF)
    private Query query;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

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

        when(entityManager.createNativeQuery(ArgumentMatchers.anyString()))
                .thenReturn(query);

        when(contaRepository.save(ArgumentMatchers.any(Conta.class)))
                .thenReturn(contaExistente);

        when(authentication.getName()).thenReturn("amadeus@email.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

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
        when(contaRepository.findAll()).thenReturn(List.of(contaExistente));

        // ACT
        List<ContaResponseDTO> resultado = contaService.listarTodas();

        // ASSERT
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTitular()).isEqualTo("Amadeus Bertoline");
    }

    @Test
    void deveListarMinhaContaComSucesso() {

        // ARRANGE
        when(contaRepository.findByUsuarioEmail("amadeus@email.com")).thenReturn(Optional.of(contaExistente));

        // ACT
        ContaResponseDTO conta = contaService.meusDados();

        // ASSERT
        assertThat(conta).isNotNull();
        assertThat(conta.getTitular()).isEqualTo(contaExistente.getUsuario().getNome());

    }

    @Test
    void deveDesativarContaComSucesso() {

        // ARRANGE
        when(usuarioRepository.findByEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(usuarioExistente));
        when(contaRepository.findById(usuarioExistente.getId())).thenReturn(Optional.of(contaExistente));
        contaExistente.setAtiva(false);
        when(contaRepository.save(contaExistente)).thenReturn(contaExistente);

        // ACT
        ContaResponseDTO resultado = contaService.desativar(contaExistente.getId());

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getAtiva()).isFalse();

    }

    @Test
    void deveAtivarContaComSucesso() {

        // ARRANGE
        when(usuarioRepository.findByEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(usuarioExistente));
        when(contaRepository.findById(usuarioExistente.getId())).thenReturn(Optional.of(contaExistente));
        contaExistente.setAtiva(true);
        when(contaRepository.save(contaExistente)).thenReturn(contaExistente);

        // ACT
        ContaResponseDTO resultado = contaService.ativar(contaExistente.getId());

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getAtiva()).isTrue();

    }

}