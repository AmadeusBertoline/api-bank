package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
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
import api.dto.page.PageResponseDTO;
import api.dto.pix.PixRequestDTO;
import api.dto.transacao.ArquivoDownloadRequestDTO;
import api.dto.transacao.ArquivoDownloadResponseDTO;
import api.dto.transacao.TransacaoResponseDTO;
import api.enums.StatusConta;
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

    @BeforeEach
    void setup() {

        // CONTA ORIGEM
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
        contaExistente.setLimiteDiario(new BigDecimal("1000.00"));
        contaExistente.setTipoConta(TipoConta.PAGAMENTO);
        contaExistente.setStatus(StatusConta.ATIVA);
        contaExistente.setDataCriacao(LocalDateTime.now());

        usuarioExistente.setConta(contaExistente);

        chavePixExistente = new ChavePix();
        chavePixExistente.setId(1L);
        chavePixExistente.setChave("57561884010");
        chavePixExistente.setTipo(TipoChavePix.CPF);
        chavePixExistente.setConta(contaExistente);

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
        contaDestino.setLimiteDiario(new BigDecimal("1000.00"));
        contaDestino.setTipoConta(TipoConta.PAGAMENTO);
        contaDestino.setStatus(StatusConta.ATIVA);
        contaDestino.setDataCriacao(LocalDateTime.now());

        usuarioDestino.setConta(contaDestino);

        chavePixDestino = new ChavePix();
        chavePixDestino.setId(2L);
        chavePixDestino.setChave("12345678901");
        chavePixDestino.setTipo(TipoChavePix.CPF);
        chavePixDestino.setConta(contaDestino);

        dto = new PixRequestDTO(
                "12345678901",
                new BigDecimal("500.00"),
                "pix da praia");
    }

    @Test
    @DisplayName("Deve realizar uma transferência Pix com sucesso")
    void deveCriarTransacaoPixComSucesso() {
        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix("12345678901")).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));
        when(transacaoRepository.sumValorEnviadoHoje(eq(1L), any())).thenReturn(BigDecimal.ZERO);

        // ACT
        TransacaoResponseDTO transacao = transacaoService.pix(dto);

        // ASSERT
        assertThat(transacao).isNotNull();
        assertThat(transacao.valor()).isEqualByComparingTo("500.00");
        assertThat(contaExistente.getSaldo()).isEqualByComparingTo("500.00");
        assertThat(contaDestino.getSaldo()).isEqualByComparingTo("1000.00");

        verify(transacaoRepository, times(1)).save(any(Transacao.class));
        verify(pixRepository, times(1)).save(any(Pix.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar realizar Pix com a conta bloqueada no usuário logado")
    void deveLancarExcecaoQuandoContaEstiverBloqueada() {
        // ARRANGE
        contaExistente.setStatus(StatusConta.BLOQUEADA);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Sua conta está bloqueada, você não pode realizar transações");

        verify(contaRepository, never()).findByUsuarioEmail(any());
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao exceder o limite diário de Pix")
    void deveLancarExcecaoLimiteDiarioExcedido() {
        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix("12345678901")).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));
        when(transacaoRepository.sumValorEnviadoHoje(eq(1L), any())).thenReturn(new BigDecimal("600.00"));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Limite diário de Pix excedido");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o saldo for insuficiente")
    void deveLancarExcecaoSaldoInsuficiente() {
        // ARRANGE
        dto = new PixRequestDTO(dto.chavePix(), new BigDecimal("9999.00"), dto.descricao());
        contaExistente.setLimiteDiario(new BigDecimal("20000.00"));

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix("12345678901")).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));
        when(transacaoRepository.sumValorEnviadoHoje(eq(1L), any())).thenReturn(BigDecimal.ZERO);

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar realizar um Pix para a própria conta")
    void deveLancarExcecaoTransferenciaParaSiMesmo() {
        // ARRANGE
        dto = new PixRequestDTO("57561884010", dto.valor(), dto.descricao());
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix("57561884010")).thenReturn(Optional.of(contaExistente));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("iguais");

        verify(contaRepository, never()).findByIdWithLock(any());
        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao realizar um Pix sem usuário autenticado")
    void deveLancarExcecaoTransferenciaSemToken() {
        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado())
                .thenThrow(new ResourceNotFoundException("Usuário inexistente"));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário inexistente");

        verify(contaRepository, never()).findByUsuarioEmail(any());
        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a conta de origem não for encontrada")
    void deveLancarExcecaoContaOrigemNaoEncontrada() {
        // ARRANGE
        usuarioExistente.setEmail("incorreto@gmail.com");
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conta origem não encontrada de id " + usuarioExistente.getId());

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a conta de destino não for encontrada")
    void deveLancarExcecaoContaDestinoNaoEncontrada() {
        // ARRANGE
        dto = new PixRequestDTO("35625605084", dto.valor(), dto.descricao());
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix(dto.chavePix())).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conta destino não encontrada, verifique se essa chave pix está cadastrada");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a chave pix não for encontrada")
    void deveLancarExcecaoChavePixNaoEncontrada() {
        // ARRANGE
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix(dto.chavePix())).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Chave pix não encontrada");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a conta de origem estiver encerrada")
    void deveLancarExcecaoContaOrigemInativa() {
        // ARRANGE
        contaExistente.setStatus(StatusConta.ENCERRADA);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix(dto.chavePix())).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Sua conta está inativa, você não pode enviar ou receber transações");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a conta de destino estiver inativa ou bloqueada")
    void deveLancarExcecaoContaDestinoInativa() {
        // ARRANGE
        contaDestino.setStatus(StatusConta.BLOQUEADA);
        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByChavesPix(dto.chavePix())).thenReturn(Optional.of(contaDestino));
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(contaExistente));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(contaDestino));
        when(chavePixRepository.findByChave("12345678901")).thenReturn(Optional.of(chavePixDestino));

        // ACT + ASSERT
        assertThatThrownBy(() -> transacaoService.pix(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("A conta destino está inativa e não pode receber ou enviar transações");

        verify(transacaoRepository, never()).save(any());
        verify(pixRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar o extrato paginado da conta com sucesso")
    void listarPorConta_DeveRetornarPageDeDTOs() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dataCriacao").descending());
        Transacao transacao = new Transacao();
        Page<Transacao> pageMock = new PageImpl<>(List.of(transacao), pageable, 1);

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(anyString())).thenReturn(Optional.of(contaExistente));
        when(transacaoRepository.encontrarTransacoes(eq(contaExistente.getId()), eq(pageable))).thenReturn(pageMock);

        // ACT
        PageResponseDTO<TransacaoResponseDTO> resultado = transacaoService.extrato(pageable);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.totalElementos());
        assertEquals(1, resultado.conteudo().size());
        verify(transacaoRepository, times(1)).encontrarTransacoes(contaExistente.getId(), pageable);
    }

    @Test
    @DisplayName("Deve gerar o DTO do arquivo CSV com sucesso quando houver transações no período")
    void gerarCsvExtrato_DeveRetornarArquivoDownloadDTO_QuandoSucesso() {
        // ARRANGE
        LocalDate inicio = LocalDate.of(2026, 8, 1);
        LocalDate fim = LocalDate.of(2026, 8, 10);
        ArquivoDownloadRequestDTO filtro = new ArquivoDownloadRequestDTO(inicio, fim);

        TransacaoResponseDTO transacaoDTO = new TransacaoResponseDTO(
                usuarioExistente.getNome(),
                "Maria Souza",
                TipoTransacao.PIX,
                new BigDecimal("150.00"),
                "Pagamento de conta",
                LocalDateTime.of(2026, 8, 5, 14, 30, 0)
        );

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(transacaoRepository.buscarParaExtrato(
                eq(contaExistente.getId()),
                eq(inicio.atStartOfDay()),
                eq(fim.atTime(LocalTime.MAX))
        )).thenReturn(List.of(transacaoDTO));

        // ACT
        ArquivoDownloadResponseDTO resultado = transacaoService.gerarCsvExtrato(filtro);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("extrato_2026-08-01_2026-08-10.csv", resultado.nomeArquivo());
        assertNotNull(resultado.conteudo());

        String csvTexto = new String(resultado.conteudo(), StandardCharsets.UTF_8);
        assertTrue(csvTexto.contains("Origem;Destino;Tipo;Valor;Descrição;Data e Hora"));
        assertTrue(csvTexto.contains("Amadeus Bertoline;Maria Souza;PIX;150.00;Pagamento de conta;05/08/2026 14:30:00"));

        verify(usuarioAutenticadoService, times(1)).getUsuarioLogado();
        verify(contaRepository, times(1)).findByUsuarioEmail(usuarioExistente.getEmail());
        verify(transacaoRepository, times(1)).buscarParaExtrato(contaExistente.getId(), inicio.atStartOfDay(), fim.atTime(LocalTime.MAX));
    }

    @Test
    @DisplayName("Deve gerar CSV apenas com cabeçalho quando a lista de transações for vazia")
    void gerarCsvExtrato_DeveGerarCsvComApenasCabecalho_QuandoNaoHouverTransacoes() {
        // ARRANGE
        LocalDate inicio = LocalDate.of(2026, 8, 1);
        LocalDate fim = LocalDate.of(2026, 8, 10);
        ArquivoDownloadRequestDTO filtro = new ArquivoDownloadRequestDTO(inicio, fim);

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.of(contaExistente));
        when(transacaoRepository.buscarParaExtrato(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // ACT
        ArquivoDownloadResponseDTO resultado = transacaoService.gerarCsvExtrato(filtro);

        // ASSERT
        assertNotNull(resultado);
        String csvTexto = new String(resultado.conteudo(), StandardCharsets.UTF_8);
        assertEquals("Origem;Destino;Tipo;Valor;Descrição;Data e Hora\n", csvTexto);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando a conta não for encontrada para o e-mail do usuário logado")
    void gerarCsvExtrato_DeveLancarExcecao_QuandoContaNaoEncontrada() {
        // ARRANGE
        ArquivoDownloadRequestDTO filtro = new ArquivoDownloadRequestDTO(LocalDate.now(), LocalDate.now());

        when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuarioExistente);
        when(contaRepository.findByUsuarioEmail(usuarioExistente.getEmail())).thenReturn(Optional.empty());

        // ACT + ASSERTT
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> transacaoService.gerarCsvExtrato(filtro)
        );

        assertEquals("Conta não encontrada para o usuário autenticado", excecao.getMessage());
        verifyNoInteractions(transacaoRepository);
    }
}
