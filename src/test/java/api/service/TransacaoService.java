package api.service;

import api.dto.TransacaoRequestDTO;
import api.exception.RegraNegocioException;
import api.model.Conta;
import api.model.Usuario;
import api.repository.ContaRepository;
import api.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    private Conta conta;
    private TransacaoRequestDTO dto;
    private Usuario usuarioExistente;

    @BeforeEach
    void setup() {

        usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);
        usuarioExistente.setNome("Amadeus Bertoline");
        usuarioExistente.setEmail("amadeus@email.com");
        usuarioExistente.setSenha("$2a$10$vQ3E9V7zG3P7kR9sX8zOueH7yvK2eD5mN6qL1rBtYwG");                                                          
        usuarioExistente.setRole("ROLE_CLIENTE");

        conta = new Conta();
        conta.setId(1L);
        conta.setUsuario(usuarioExistente);
        conta.setSaldo(new BigDecimal("1000.00"));
        conta.setAtiva(true);

        dto = new TransacaoRequestDTO();
        dto.setContaId(1L);
        dto.setValor(new BigDecimal("200.00"));
    }

    @Test
    void deveRealizarSaqueComSucesso() {
        // ARRANGE
        dto.setTipo("SAQUE");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // ACT
        transacaoService.realizarTransacao(dto);

        // ASSERT — saldo deve ter diminuído
        assertThat(conta.getSaldo()).isEqualByComparingTo("800.00");
        verify(contaRepository, times(1)).save(conta);
    }

    @Test
    void deveLancarExcecaoSaldoInsuficiente() {

        // ARRANGE
        dto.setTipo("SAQUE");
        dto.setValor(new BigDecimal("9999.00")); 
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.realizarTransacao(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");

        // Garante que nada foi salvo
        verify(contaRepository, never()).save(any());
        verify(transacaoRepository, never()).save(any());
        
    }

    @Test
    void deveLancarExcecaoTransferenciaParaSiMesmo() {

        // ARRANGE
        dto.setTipo("TRANSFERENCIA");
        dto.setContaDestinoId(1L); 
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.realizarTransacao(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("iguais");

    }

    @Test
    void deveLancarExcecaoTipoInvalido() {

        // ARRANGE
        dto.setTipo("INVALIDO");
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.realizarTransacao(dto))
                .isInstanceOf(RegraNegocioException.class);
    }

}