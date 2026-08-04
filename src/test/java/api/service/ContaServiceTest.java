package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.junit.jupiter.api.DisplayName;
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

        // Instanciação direta via construtor do Record
        contaRequestDTO = new ContaRequestDTO(usuarioExistente);
    }

    @Test
    @DisplayName("Deve criar conta com sucesso")
    void deveCriarContaComSucesso() {
        when(contaRepository.existsByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(contaExistente);

        ContaResponseDTO resultado = contaService.criar(contaRequestDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.titular()).isEqualTo("Amadeus Bertoline");
        assertThat(resultado.saldo()).isEqualByComparingTo("1000.00");
        verify(contaRepository, times(1)).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve listar todas as contas paginadas")
    void deveListarTodasAsContas() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dataCriacao").descending());
        Page<Conta> pageMock = new PageImpl<>(List.of(contaExistente), pageable, 1);

        when(contaRepository.findAll(pageable)).thenReturn(pageMock);

        Page<ContaResponseDTO> resultado = contaService.listarTodas(pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).titular()).isEqualTo("Amadeus Bertoline");
    }

    @Test
    @DisplayName("Deve retornar dados da minha conta com sucesso")
    void deveListarMinhaContaComSucesso() {
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));

        ContaResponseDTO conta = contaService.meusDados();

        assertThat(conta).isNotNull();
        assertThat(conta.titular()).isEqualTo(contaExistente.getUsuario().getNome());
    }

    @Test
    @DisplayName("Admin deve desativar conta por ID com sucesso")
    void deveDesativarContaComSucessoAdmin() {
        contaExistente.setAtiva(true);

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByIdWithLock(contaExistente.getId())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContaResponseDTO resultado = contaService.desativar(contaExistente.getId());

        assertThat(resultado).isNotNull();
        assertThat(resultado.ativa()).isFalse();
        verify(contaRepository, times(1)).save(contaExistente);
    }

    @Test
    @DisplayName("Admin deve ativar conta por ID com sucesso")
    void deveAtivarContaComSucessoAdmin() {
        contaExistente.setAtiva(false);

        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByIdWithLock(contaExistente.getId())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContaResponseDTO resultado = contaService.ativar(contaExistente.getId());

        assertThat(resultado).isNotNull();
        assertThat(resultado.ativa()).isTrue();
        verify(contaRepository, times(1)).save(contaExistente);
    }

    @Test
    @DisplayName("Deve desativar a própria conta com sucesso")
    void deveDesativarContaComSucesso() {
        contaExistente.setAtiva(true);

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmailWithLock(usuarioExistente.getEmail()))
                .thenReturn(Optional.of(contaExistente));
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContaResponseDTO resultado = contaService.desativarMinhaConta();

        assertThat(resultado).isNotNull();
        assertThat(resultado.ativa()).isFalse();
        verify(contaRepository, times(1)).save(contaExistente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar desativar própria conta inexistente")
    void deveLancarExcecaoAoDesativarContaInexistente() {
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmailWithLock(usuarioExistente.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.desativarMinhaConta())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conta não encontrada");

        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve ativar a própria conta com sucesso")
    void deveAtivarContaComSucesso() {
        contaExistente.setAtiva(false);

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmailWithLock(usuarioExistente.getEmail()))
                .thenReturn(Optional.of(contaExistente));
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContaResponseDTO resultado = contaService.ativarMinhaConta();

        assertThat(resultado).isNotNull();
        assertThat(resultado.ativa()).isTrue();
        verify(contaRepository, times(1)).save(contaExistente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar ativar própria conta inexistente")
    void deveLancarExcecaoAoAtivarContaInexistente() {
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmailWithLock(usuarioExistente.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.ativarMinhaConta())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conta não encontrada");

        verify(contaRepository, never()).save(any(Conta.class));
    }
}