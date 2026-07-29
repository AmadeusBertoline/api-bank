package api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import api.model.ChavePix;

public interface ChavePixRepository extends JpaRepository<ChavePix, Long> {
    List<ChavePix> findAllByContaId(Long conta);

    @Query("SELECT c.conta.usuario.id FROM ChavePix c WHERE c.id = :chaveId")
    Long findUsuarioIdByIdDaChave(@Param("chaveId") Long chaveId);

    Optional<ChavePix> findByChave(String chave);

}
