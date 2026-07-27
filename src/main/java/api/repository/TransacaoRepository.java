package api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import api.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    @Query("SELECT t FROM Transacao t WHERE t.contaOrigem.id = :contaId OR t.contaDestino.id = :contaId ORDER BY t.dataHora DESC")
    List<Transacao> encontrarTransacoes(@Param("contaId") Long contaId);
}
