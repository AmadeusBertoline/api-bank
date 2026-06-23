package api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import api.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long>{
    List<Transacao> findByContaIdOrderByDataHoraDesc(Long contaId);
}
