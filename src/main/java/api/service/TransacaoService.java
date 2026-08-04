package api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import api.dto.PixRequestDTO;
import api.dto.TransacaoResponseDTO;
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
    public TransacaoResponseDTO pix(PixRequestDTO dto) {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Conta origemTemp = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Conta origem não encontrada de id " + usuario.getId()));

        Conta destinoTemp = contaRepository.findByChavesPix(dto.getChavePix())
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
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada id " + primeiroId));
        Conta conta2 = contaRepository.findByIdWithLock(segundoId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada id " + segundoId));

        // mapeia de volta quem é a origem e quem é o destino
        Conta contaOrigem = origemTemp.getId().equals(primeiroId) ? conta1 : conta2;
        Conta contaDestino = destinoTemp.getId().equals(primeiroId) ? conta1 : conta2;

        ChavePix chavePix = chavePixRepository.findByChave(dto.getChavePix())
                .orElseThrow(() -> new ResourceNotFoundException("Chave pix não encontrada"));

        if (!contaOrigem.getAtiva()) {
            throw new RegraNegocioException("Sua conta está inativa, você não pode enviar ou receber transações");
        }

        if (!contaDestino.getAtiva()) {
            throw new RegraNegocioException("A conta destino está inativa e não pode receber ou enviar transações");
        }

        // validação de limite diário
        LocalDateTime inicioDoDia = LocalDate.now().atStartOfDay();
        BigDecimal totalEnviadoHoje = transacaoRepository.sumValorEnviadoHoje(contaOrigem.getId(), inicioDoDia);
        BigDecimal novoTotal = totalEnviadoHoje.add(dto.getValor());

        if (contaOrigem.getLimiteDiarioPix() != null && novoTotal.compareTo(contaOrigem.getLimiteDiarioPix()) > 0) {
            throw new RegraNegocioException("Limite diário de Pix excedido. Limite atual: R$ "
                    + contaOrigem.getLimiteDiarioPix() + ". Já utilizado hoje: R$ " + totalEnviadoHoje);
        }

        if (contaOrigem.getSaldo().compareTo(dto.getValor()) < 0) {
            throw new RegraNegocioException("Saldo insuficiente. Saldo Atual: " + contaOrigem.getSaldo());
        }

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.PIX);
        transacao.setValor(dto.getValor());
        transacao.setDescricao(dto.getDescricao());
        transacao.setContaOrigem(contaOrigem);
        transacao.setContaDestino(contaDestino);

        Pix pix = new Pix();
        pix.setTransacao(transacao);
        pix.setChavePix(dto.getChavePix());
        pix.setTipoChave(chavePix.getTipo());
        pix.setEndToEndId(EndToEndIdUtil.gerar());

        contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.getValor()));
        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));

        transacaoRepository.save(transacao);
        pixRepository.save(pix);

        return toDTO(transacao);
    }

    public Page<TransacaoResponseDTO> listarPorConta(Pageable pageable) {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id: " + usuario.getId()));

        return transacaoRepository.encontrarTransacoes(conta.getId(), pageable)
                .map(this::toDTO);
    }

    private TransacaoResponseDTO toDTO(Transacao transacao) {

        TransacaoResponseDTO dto = new TransacaoResponseDTO();
        dto.setTipo(transacao.getTipo());
        dto.setValor(transacao.getValor());
        dto.setDescricao(transacao.getDescricao());
        dto.setDataHora(transacao.getDataHora());

        if (transacao.getContaDestino() != null) {
            dto.setTitularContaDestino(transacao.getContaDestino().getUsuario().getNome());
        }

        if (transacao.getContaOrigem() != null) {
            dto.setTitularConta(transacao.getContaOrigem().getUsuario().getNome());
        }

        return dto;

    }

}