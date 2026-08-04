package api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import api.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    @Query("SELECT t FROM Transacao t WHERE t.contaOrigem.id = :contaId OR t.contaDestino.id = :contaId ORDER BY t.dataHora DESC")
    List<Transacao> encontrarTransacoes(@Param("contaId") Long contaId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t " +
            "WHERE t.contaOrigem.id = :contaId AND t.dataHora >= :inicioDoDia AND t.tipo = 'PIX'")
    BigDecimal sumValorEnviadoHoje(Long contaId, LocalDateTime inicioDoDia);
}
