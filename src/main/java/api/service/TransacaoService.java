package api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private PixRepository pixRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Autowired
    private ChavePixRepository chavePixRepository;

    @Transactional
    public TransacaoResponseDTO pix(PixRequestDTO dto) {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Conta contaOrigem = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Conta origem não encontrada de id " + usuario.getId()));

        Conta contaDestino = contaRepository.findByChavesPix(dto.getChavePix())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Conta destino não encontrada de id " + usuario.getId()
                                + " verifique se essa chave pix está cadastrada"));

        ChavePix chavePix = chavePixRepository.findByChave(dto.getChavePix())
                .orElseThrow(() -> new ResourceNotFoundException("Chave pix não encontrada"));

        if (!contaOrigem.getAtiva()) {
            throw new RegraNegocioException("Sua conta está inativa, você não pode enviar ou receber transações");
        }

        if (!contaDestino.getAtiva()) {
            throw new RegraNegocioException("A conta destino está inativa e não pode receber ou enviar transações");
        }

        if (contaOrigem.getId().equals(contaDestino.getId())) {
            throw new RegraNegocioException("A conta de origem e destino não podem ser iguais.");
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
        contaRepository.save(contaOrigem);
        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));
        contaRepository.save(contaDestino);

        transacaoRepository.save(transacao);
        pixRepository.save(pix);

        return toDTO(transacao);

    }

    public List<TransacaoResponseDTO> listarPorConta() {

        Usuario usuario = usuarioAutenticadoService.getUsuarioLogado();

        Conta conta = contaRepository.findByUsuarioEmail(usuario.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id: " + usuario.getId()));

        return transacaoRepository.encontrarTransacoes(conta.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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