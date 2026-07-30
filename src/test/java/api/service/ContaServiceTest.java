package api.service;

import api.dto.ContaResponseDTO;
import api.enums.TipoConta;
import api.enums.TipoRole;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Endereco;
import api.model.Usuario;
import api.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private ContaRepository contaRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ContaService contaService;

    private Endereco enderecoExistente;
    private Conta contaExistente;
    private Usuario usuarioExistente;

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

    }

    @Test
    void deveCriarContaComSucesso() {

        // ARRANGE
        when(contaRepository.existsByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenReturn(contaExistente);

        // ACT
        ContaResponseDTO resultado = contaService.criar(usuarioExistente);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitular()).isEqualTo("Amadeus Bertoline");
        assertThat(resultado.getSaldo()).isEqualByComparingTo("1000.00");
        verify(contaRepository, times(1)).save(any(Conta.class));

    }

    @Test
    void deveBuscarContaPorIdComSucesso() {

        // ARRANGE
        when(contaRepository.findById(1L)).thenReturn(Optional.of(contaExistente));

        // ACT
        ContaResponseDTO resultado = contaService.buscarPorId(1L);

        // ASSERT
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitular()).isEqualTo("Amadeus Bertoline");
    }

    @Test
    void deveLancarExcecaoQuandoContaNaoEncontrada() {

        // ARRANGE
        when(contaRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> contaService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
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

}