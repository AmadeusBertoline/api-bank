package api.service;

import api.dto.ContaRequestDTO;
import api.dto.ContaResponseDTO;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Usuario;
import api.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    @InjectMocks
    private ContaService contaService;

    private Conta contaExistente;
    private ContaRequestDTO requestDTO;
    private Usuario usuarioExistente;

    @BeforeEach
    void setup() {

        usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);
        usuarioExistente.setNome("Amadeus Bertoline");
        usuarioExistente.setEmail("amadeus@email.com");
        usuarioExistente.setSenha("$2a$10$vQ3E9V7zG3P7kR9sX8zOueH7yvK2eD5mN6qL1rBtYwG");                                                          
        usuarioExistente.setRole("ROLE_CLIENTE");

        contaExistente = new Conta();
        contaExistente.setId(1L);
        contaExistente.setUsuario(usuarioExistente);
        contaExistente.setNumeroConta("001-1");
        contaExistente.setSaldo(new BigDecimal("1000.00"));
        contaExistente.setTipoConta("CORRENTE");
        contaExistente.setAtiva(true);
        contaExistente.setDataCriacao(LocalDateTime.now());

        requestDTO = new ContaRequestDTO();
        requestDTO.setNumeroConta("001-1");
        requestDTO.setSaldo(new BigDecimal("1000.00"));
        requestDTO.setTipoConta("CORRENTE");
    }

    @Test
    void deveCriarContaComSucesso() {

        // ARRANGE
        when(contaRepository.save(any(Conta.class))).thenReturn(contaExistente);

        // ACT
        ContaResponseDTO resultado = contaService.criar(requestDTO);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsuario()).isEqualTo(usuarioExistente);
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
        assertThat(resultado.getUsuario()).isEqualTo(usuarioExistente);
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
        assertThat(resultado.get(0).getUsuario()).isEqualTo(usuarioExistente);
    }

    @Test
    void deveLancarExcecaoAoDeletarContaInexistente() {

        // ARRANGE
        when(contaRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> contaService.deletar(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(contaRepository, never()).delete(any());

    }
}