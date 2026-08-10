package api.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import api.dto.transacao.TransacaoResponseDTO;
import api.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
        @Query("SELECT t FROM Transacao t WHERE t.contaOrigem.id = :contaId OR t.contaDestino.id = :contaId ORDER BY t.dataHora DESC")
        Page<Transacao> encontrarTransacoes(@Param("contaId") Long contaId, Pageable pageable);

        @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t " +
                        "WHERE t.contaOrigem.id = :contaId AND t.dataHora >= :inicioDoDia AND t.tipo = 'PIX'")
        BigDecimal sumValorEnviadoHoje(Long contaId, LocalDateTime inicioDoDia);

        @Query("""
                        SELECT new api.dto.transacao.TransacaoResponseDTO(
                            t.contaOrigem.usuario.nome,
                            t.contaDestino.usuario.nome,
                            t.tipo,
                            t.valor,
                            t.descricao,
                            t.dataHora
                        )
                                            FROM Transacao t
                                            WHERE (t.contaOrigem.id = :contaId OR t.contaDestino.id = :contaId)
                                              AND t.dataHora BETWEEN :dataInicio AND :dataFim
                                            ORDER BY t.dataHora DESC
                                        """)
        List<TransacaoResponseDTO> buscarParaExtrato(
                        @Param("contaId") Long contaId,
                        @Param("dataInicio") LocalDateTime dataInicio,
                        @Param("dataFim") LocalDateTime dataFim);
}
