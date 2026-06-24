package api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import api.dto.TransacaoRequestDTO;
import api.dto.TransacaoResponseDTO;
import api.exception.RegraNegocioException;
import api.exception.ResourceNotFoundException;
import api.model.Conta;
import api.model.Transacao;
import api.repository.ContaRepository;
import api.repository.TransacaoRepository;
import jakarta.transaction.Transactional;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;
    
    @Autowired
    private ContaRepository contaRepository;

    @Transactional
    public TransacaoResponseDTO realizarTransacao(TransacaoRequestDTO dto){

        Conta conta = contaRepository.findById(dto.getContaId())
        .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id "+dto.getContaId()));

        if(!conta.getAtiva()){
            throw new RegraNegocioException("A conta precisa estar ativa para realizar transações");
        }

        return switch(dto.getTipo().toUpperCase()){
            case "DEPOSITO"         ->  depositar(conta, dto);
            case "SAQUE"            ->  sacar(conta, dto);
            case "TRANSFERENCIA"    ->  transferir(conta, dto);
            default -> throw new RegraNegocioException("Tipo de transação é inválido, use: DEPOSITO, SAQUE OU TRANSFERÊNCIA");
        };

    }

    private TransacaoResponseDTO depositar(Conta conta, TransacaoRequestDTO dto){

        conta.setSaldo(conta.getSaldo().add(dto.getValor()));
        contaRepository.save(conta);

        Transacao transacao = new Transacao();
        transacao.setConta(conta);
        transacao.setTipo("DEPOSITO");
        transacao.setValor(dto.getValor());
        transacao.setDescricao(dto.getDescricao());

        return toDTO(transacaoRepository.save(transacao));

    }

    private TransacaoResponseDTO sacar(Conta conta, TransacaoRequestDTO dto){

        if(conta.getSaldo().compareTo(dto.getValor()) < 0){
            throw new RegraNegocioException("Saldo insuficiente. Saldo atual: "+conta.getSaldo());
        }

        conta.setSaldo(conta.getSaldo().subtract(dto.getValor()));
        contaRepository.save(conta);

        Transacao transacao = new Transacao();
        transacao.setConta(conta);
        transacao.setTipo("SAQUE");
        transacao.setValor(dto.getValor());
        transacao.setDescricao(dto.getDescricao());

        return toDTO(transacaoRepository.save(transacao));

    }

    public TransacaoResponseDTO transferir(Conta conta, TransacaoRequestDTO dto){

        if(dto.getContaDestinoId() == null){
            throw new RegraNegocioException("A conta destino deve ser informada.");
        }

        if(conta.getId() == dto.getContaDestinoId()){
            throw new RegraNegocioException("A conta de origem e destino não podem ser iguais.");
        }

        Conta contaDestino = contaRepository.findById(dto.getContaDestinoId())
        .orElseThrow(() -> new ResourceNotFoundException("Conta destino não encontrada com id: "+dto.getContaDestinoId()));

        if(!contaDestino.getAtiva()){
            throw new RegraNegocioException("Contra destino inativa não pode receber transferências.");
        }

        if(conta.getSaldo().compareTo(dto.getValor()) < 0){
            throw new RegraNegocioException("Saldo insuficiente. Saldo Atual: "+conta.getSaldo());
        }

        conta.setSaldo(conta.getSaldo().subtract(dto.getValor()));
        contaRepository.save(conta);
        contaDestino.setSaldo(contaDestino.getSaldo().add(dto.getValor()));
        contaRepository.save(contaDestino);

        Transacao transacao = new Transacao();

        transacao.setConta(conta);
        transacao.setContaDestino(contaDestino);
        transacao.setTipo("TRANSFERENCIA");
        transacao.setValor(dto.getValor());
        transacao.setDescricao(dto.getDescricao());

        return toDTO(transacaoRepository.save(transacao));

    }

    public List<TransacaoResponseDTO> listarPorConta(Long id){
        
        contaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada de id: "+id));

        return transacaoRepository.findByContaIdOrderByDataHoraDesc(id)
        .stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
    }

    private TransacaoResponseDTO toDTO(Transacao transacao){

        TransacaoResponseDTO dto = new TransacaoResponseDTO();
        dto.setId(transacao.getId());
        dto.setTipo(transacao.getTipo());
        dto.setContaId(transacao.getConta().getId());
        dto.setTitularConta(transacao.getConta().getTitular());
        dto.setDescricao(transacao.getDescricao());
        dto.setValor(transacao.getValor());
        dto.setDataHora(transacao.getDataHora());
        
        if (transacao.getContaDestino() != null) {
            dto.setContaDestinoId(transacao.getContaDestino().getId());
            dto.setTitularContaDestino(transacao.getContaDestino().getTitular());
        }

        return dto;

    }

}
