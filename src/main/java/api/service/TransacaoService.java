package api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import api.dto.TransacaoRequestDTO;
import api.dto.TransacaoResponseDTO;
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
        }

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
    
}
