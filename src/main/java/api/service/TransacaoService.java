package api.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import api.dto.page.PageResponseDTO;
import api.dto.pix.PixRequestDTO;
import api.dto.transacao.ArquivoDownloadResponseDTO;
import api.dto.transacao.ArquivoDownloadRequestDTO;
import api.dto.transacao.TransacaoResponseDTO;
import api.enums.StatusConta;
import api.enums.TipoTransacao;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.ChavePix;
import api.model.Conta;
import api.model.Pix;
import api.model.Transacao;
import api.model.Usuario;
import api.repository.ChavePixRepository;
import api.repository.ContaRepository;
import api.repository.PixRepository;
import api.repository.TransacaoRepository;
import api.util.EndToEndIdUtil;
import jakarta.transaction.Transactional;

@Service
public class TransacaoService {

        private final TransacaoRepository transacaoRepository;
        private final PixRepository pixRepository;
        private final ContaRepository contaRepository;
        private final UsuarioAutenticadoService usuarioAutenticadoService;
        private final ChavePixRepository chavePixRepository;

        public TransacaoService(
                        TransacaoRepository transacaoRepository,
                        PixRepository pixRepository,
                        ContaRepository contaRepository,
                        UsuarioAutenticadoService usuarioAutenticadoService,
                        ChavePixRepository chavePixRepository) {

                this.transacaoRepository = transacaoRepository;
                this.pixRepository = pixRepository;
                this.contaRepository = contaRepository;
                this.usuarioAutenticadoService = usuarioAutenticadoService;
                this.chavePixRepository = chavePixRepository;
        }

        @Transactional
        @CacheEvict(value = "transacoes", allEntries = true)
        public TransacaoResponseDTO pix(PixRequestDTO dto) {

                Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

                if (usuario.getConta().getStatus().equals(StatusConta.BLOQUEADA)) {

                        throw new RegraNegocioException(
                                        "Sua conta está bloqueada, você não pode realizar transações nem alterações de chave pix");

                }

                Conta origemTemp = contaRepository.findByUsuarioEmail(usuario.getEmail())
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Conta origem não encontrada de id "
                                                                + usuario.getId()));

                Conta destinoTemp = contaRepository.findByChavesPix(dto.chavePix())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conta destino não encontrada, verifique se essa chave pix está cadastrada"));

                if (origemTemp.getId().equals(destinoTemp.getId())) {
                        throw new RegraNegocioException("A conta de origem e destino não podem ser iguais.");
                }

                // ordenação dos ids para travar as threads na mesma ordem
                Long primeiroId = Math.min(origemTemp.getId(), destinoTemp.getId());
                Long segundoId = Math.max(origemTemp.getId(), destinoTemp.getId());

                // busca com lock
                Conta conta1 = contaRepository.findByIdWithLock(primeiroId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conta não encontrada id " + primeiroId));
                Conta conta2 = contaRepository.findByIdWithLock(segundoId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conta não encontrada id " + segundoId));

                // mapeia de volta quem é a origem e quem é o destino
                Conta contaOrigem = origemTemp.getId().equals(primeiroId) ? conta1 : conta2;
                Conta contaDestino = destinoTemp.getId().equals(primeiroId) ? conta1 : conta2;

                ChavePix chavePix = chavePixRepository.findByChave(dto.chavePix())
                                .orElseThrow(() -> new ResourceNotFoundException("Chave pix não encontrada"));

                if (contaOrigem.getStatus() == StatusConta.BLOQUEADA
                                || contaOrigem.getStatus() == StatusConta.ENCERRADA) {
                        throw new RegraNegocioException(
                                        "Sua conta está inativa, você não pode enviar ou receber transações");
                }

                if (contaDestino.getStatus() == StatusConta.BLOQUEADA
                                || contaDestino.getStatus() == StatusConta.ENCERRADA) {
                        throw new RegraNegocioException(
                                        "A conta destino está inativa e não pode receber ou enviar transações");
                }

                // validação de limite diário
                LocalDateTime inicioDoDia = LocalDate.now().atStartOfDay();
                BigDecimal totalEnviadoHoje = transacaoRepository.sumValorEnviadoHoje(contaOrigem.getId(), inicioDoDia);
                BigDecimal novoTotal = totalEnviadoHoje.add(dto.valor());

                if (contaOrigem.getLimiteDiario() != null && novoTotal.compareTo(contaOrigem.getLimiteDiario()) > 0) {
                        throw new RegraNegocioException("Limite diário de Pix excedido. Limite atual: R$ "
                                        + contaOrigem.getLimiteDiario() + ". Já utilizado hoje: R$ "
                                        + totalEnviadoHoje);
                }

                if (contaOrigem.getSaldo().compareTo(dto.valor()) < 0) {
                        throw new RegraNegocioException("Saldo insuficiente. Saldo Atual: " + contaOrigem.getSaldo());
                }

                Transacao transacao = new Transacao();
                transacao.setTipo(TipoTransacao.PIX);
                transacao.setValor(dto.valor());
                transacao.setDescricao(dto.descricao());
                transacao.setContaOrigem(contaOrigem);
                transacao.setContaDestino(contaDestino);

                Pix pix = new Pix();
                pix.setTransacao(transacao);
                pix.setChavePix(dto.chavePix());
                pix.setTipoChave(chavePix.getTipo());
                pix.setEndToEndId(EndToEndIdUtil.gerar());

                contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.valor()));
                contaDestino.setSaldo(contaDestino.getSaldo().add(dto.valor()));

                transacaoRepository.save(transacao);
                pixRepository.save(pix);

                return toDTO(transacao);
        }

        @Cacheable(value = "transacoes", key = "{@usuarioAutenticadoService.getUsuarioLogado().id, #pageable.pageNumber, #pageable.pageSize}")
        public PageResponseDTO<TransacaoResponseDTO> extrato(Pageable pageable) {

                Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

                Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conta não encontrada de id: " + usuario.getId()));

                return new PageResponseDTO<>(transacaoRepository.encontrarTransacoes(conta.getId(), pageable)
                                .map(this::toDTO));
        }

        @Transactional
        public ArquivoDownloadResponseDTO gerarCsvExtrato(ArquivoDownloadRequestDTO filtro) {

                Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

                Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Conta não encontrada para o usuário autenticado"));


                LocalDateTime inicio = filtro.dataInicio().atStartOfDay();
                LocalDateTime fim = filtro.dataFim().atTime(LocalTime.MAX);

                List<TransacaoResponseDTO> transacoes = transacaoRepository.buscarParaExtrato(conta.getId(), inicio,
                                fim);

                byte[] csvBytes = construirCsv(transacoes);
                String nomeArquivo = String.format("extrato_%s_%s.csv", filtro.dataInicio(), filtro.dataFim());

                return new ArquivoDownloadResponseDTO(csvBytes, nomeArquivo);
        }

        private byte[] construirCsv(List<TransacaoResponseDTO> transacoes) {
                StringBuilder csv = new StringBuilder();
                csv.append("Origem;Destino;Tipo;Valor;Descrição;Data e Hora\n");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                for (TransacaoResponseDTO t : transacoes) {
                        csv.append(t.titularConta() != null ? t.titularConta() : "").append(";")
                                        .append(t.titularContaDestino() != null ? t.titularContaDestino() : "")
                                        .append(";")
                                        .append(t.tipo()).append(";")
                                        .append(t.valor()).append(";")
                                        .append(t.descricao() != null ? t.descricao() : "").append(";")
                                        .append(t.dataHora() != null ? t.dataHora().format(formatter) : "")
                                        .append("\n");
                }

                return csv.toString().getBytes(StandardCharsets.UTF_8);
        }

        private TransacaoResponseDTO toDTO(Transacao transacao) {

                String titularOrigem = Optional.ofNullable(transacao.getContaOrigem())
                                .map(Conta::getUsuario)
                                .map(Usuario::getNome)
                                .orElse(null);

                String titularDestino = Optional.ofNullable(transacao.getContaDestino())
                                .map(Conta::getUsuario)
                                .map(Usuario::getNome)
                                .orElse(null);

                return new TransacaoResponseDTO(
                                titularOrigem,
                                titularDestino,
                                transacao.getTipo(),
                                transacao.getValor(),
                                transacao.getDescricao(),
                                transacao.getDataHora());

        }

}