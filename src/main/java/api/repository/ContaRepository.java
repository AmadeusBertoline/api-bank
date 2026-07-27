package api.repository;
import api.model.Conta;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    boolean existsByUsuarioEmail(String email);

    Optional<Conta> findByUsuarioEmail(String email);

    @Query("SELECT c FROM Conta c JOIN c.chavesPix cp WHERE cp.chave = :chave")
    Optional<Conta> findByChavesPix(@Param("chave") String chave);

}
