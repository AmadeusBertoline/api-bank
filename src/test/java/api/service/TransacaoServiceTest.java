package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import api.dto.PixRequestDTO;
import api.dto.TransacaoResponseDTO;
import api.enums.TipoChavePix;
import api.enums.TipoConta;
import api.enums.TipoRole;
import api.enums.TipoTransacao;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.ChavePix;
import api.model.Conta;
import api.model.Endereco;
import api.model.Pix;
import api.model.Transacao;
import api.model.Usuario;
import api.repository.ChavePixRepository;
import api.repository.ContaRepository;
import api.repository.PixRepository;
import api.repository.TransacaoRepository;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private PixRepository pixRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private ChavePixRepository chavePixRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    // CONTA ORIGEM
    private Conta contaExistente;
    private PixRequestDTO dto;
    private Usuario usuarioExistente;
    private Endereco enderecoExistente;
    private ChavePix chavePixExistente;

    // CONTA DESTINO
    private Usuario usuarioDestino;
    private Endereco enderecoDestino;
    private Conta contaDestino;
    private ChavePix chavePixDestino;
    private TransacaoResponseDTO transacaoResponseDTO;
    private String idempotencyKey;

    @BeforeEach
    void setup() {

        idempotencyKey = "123e4567-e89b-12d3-a456-426614174000";

        // CONTA ORIGEM

        chavePixExistente = new ChavePix();
        chavePixExistente.setId(1L);
        chavePixExistente.setChave("57561884010");
        chavePixExistente.setTipo(TipoChavePix.CPF);
        chavePixExistente.setConta(contaExistente);

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
        contaExistente.setLimiteDiarioPix(new BigDecimal("1000.00"));
        contaExistente.setTipoConta(TipoConta.PAGAMENTO);
        contaExistente.setAtiva(true);
        contaExistente.setDataCriacao(LocalDateTime.now());

        // CONTA DESTINO

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
        contaDestino.setLimiteDiarioPix(new BigDecimal("1000.00"));
        contaDestino.setTipoConta(TipoConta.PAGAMENTO);
        contaDestino.setAtiva(true);
        contaDestino.setDataCriacao(LocalDateTime.now());

        chavePixDestino = new ChavePix();
        chavePixDestino.setId(2L);
        chavePixDestino.setChave("12345678901");
        chavePixDestino.setTipo(TipoChavePix.CPF);
        chavePixDestino.setConta(contaDestino);

        dto = new PixRequestDTO();
        dto.setChavePix("12345678901");
        dto.setValor(new BigDecimal("500.00"));
        dto.setDescricao("pix da praia");

        transacaoResponseDTO = new TransacaoResponseDTO();
        transacaoResponseDTO.setTitularConta("João Silva");
        transacaoResponseDTO.setTitularContaDestino("Maria Oliveira");
        transacaoResponseDTO.setTipo(TipoTransacao.PIX);
        transacaoResponseDTO.setValor(dto.getValor());
        transacaoResponseDTO.setDescricao(dto.getDescricao());
        transacaoResponseDTO.setDataHora(LocalDateTime.now());
    }

    @Test
    void deveCriarTransacaoPixComSucesso() {

        // ARRANGE
        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix("12345678901")).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));
        when(transacaoRepository.sumValorEnviadoHoje(eq(1L), any())).thenReturn(BigDecimal.ZERO);

        // ACT
        TransacaoResponseDTO transacao = transacaoService.pix(dto, idempotencyKey);

        // ASSERT
        assertThat(transacao).isNotNull();
        assertThat(transacao.getValor()).isEqualByComparingTo("500.00");
        assertThat(contaExistente.getSaldo()).isEqualByComparingTo("500.00");
        assertThat(contaDestino.getSaldo()).isEqualByComparingTo("1000.00");

        verify(transacaoRepository, times(1)).save(any(Transacao.class));
        verify(pixRepository, times(1)).save(any(Pix.class));

    }

    @Test
    void deveLancarExcecaoTransacaoDuplicadaIdempotencia() {

        // ARRANGE
        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto, idempotencyKey))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Esta transação já foi processada.");

        verify(usuarioAutenticadoService, never()).getUsuarioLogado();
        verify(transacaoRepository, never()).save(any());

    }

    @Test
    void deveLancarExcecaoLimiteDiarioExcedido() {

        // ARRANGE
        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix("12345678901")).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));
        when(transacaoRepository.sumValorEnviadoHoje(eq(1L), any())).thenReturn(new BigDecimal("600.00"));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto, idempotencyKey))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Limite diário de Pix excedido");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());

    }

    @Test
    void deveLancarExcecaoSaldoInsuficiente() {

        // ARRANGE
        dto.setValor(new BigDecimal("9999.00"));
        contaExistente.setLimiteDiarioPix(new BigDecimal("20000.00"));

        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix("12345678901")).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));
        when(transacaoRepository.sumValorEnviadoHoje(eq(1L), any())).thenReturn(BigDecimal.ZERO);

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto, idempotencyKey))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());

    }

    @Test
    void deveLancarExcecaoTransferenciaParaSiMesmo() {

        // ARRANGE
        dto.setChavePix("57561884010");
        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix("57561884010")).thenReturn(Optional.of(contaExistente));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto, idempotencyKey))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("iguais");

        verify(contaRepository, never()).findByIdWithLock(any());
        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());

    }

    @Test
    void deveLancarExcecaoTransferenciaSemToken() {

        // ARRANGE
        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(usuarioAutenticadoService.getUsuarioLogado())
                .thenThrow(new ResourceNotFoundException("Usuário inexistente"));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto, idempotencyKey))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário inexistente");

        verify(contaRepository, never()).findByUsuarioEmail(any());
        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());

    }

    @Test
    void deveLancarExcecaoContaOrigemNaoEncontrada() {

        usuarioExistente.setEmail("incorreto@gmail.com");

        // ARRANGE
        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto, idempotencyKey))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conta origem não encontrada de id " + usuarioExistente.getId());

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());

    }

    @Test
    void deveLancarExcecaoContaDestinoNaoEncontrada() {

        dto.setChavePix("35625605084");

        // ARRANGE
        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix(dto.getChavePix())).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto, idempotencyKey))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conta destino não encontrada de id " + usuarioExistente.getId());

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());

    }

    @Test
    void deveLancarExcecaoContaOrigemInativa() {

        contaExistente.setAtiva(false);

        // ARRANGE
        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix(dto.getChavePix())).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto, idempotencyKey))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Sua conta está inativa, você não pode enviar ou receber transações");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());

    }

    @Test
    void deveLancarExcecaoContaDestinoInativa() {

        contaDestino.setAtiva(false);

        // ARRANGE
        when(transacaoRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix(dto.getChavePix())).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto, idempotencyKey))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("A conta destino está inativa e não pode receber ou enviar transações");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());

    }

    @Test
    void listarPorConta_DeveRetornarPageDeDTOs() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dataCriacao").descending());
        Transacao transacao = new Transacao(/* inicializar com dados mock */);
        Page<Transacao> pageMock = new PageImpl<>(List.of(transacao), pageable, 1);

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(anyString())).thenReturn(Optional.of(contaExistente));
        when(transacaoRepository.encontrarTransacoes(eq(contaExistente.getId()), eq(pageable))).thenReturn(pageMock);

        // ACT
        Page<TransacaoResponseDTO> resultado = transacaoService.listarPorConta(pageable);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        verify(transacaoRepository, times(1)).encontrarTransacoes(contaExistente.getId(), pageable);
    }

}