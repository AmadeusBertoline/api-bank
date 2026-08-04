package api.repository;

import api.model.Conta;
import jakarta.persistence.LockModeType;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    boolean existsByUsuarioEmail(String email);

    Optional<Conta> findByUsuarioEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Conta c WHERE c.usuario.email = :email")
    Optional<Conta> findByUsuarioEmailWithLock(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Conta c JOIN c.chavesPix cp WHERE cp.chave = :chave")
    Optional<Conta> findByChavesPixWithLock(@Param("chave") String chave);

    @Query("SELECT c FROM Conta c JOIN c.chavesPix cp WHERE cp.chave = :chave")
    Optional<Conta> findByChavesPix(@Param("chave") String chave);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Conta c WHERE c.id = :id")
    Optional<Conta> findByIdWithLock(@Param("id") Long id);

}
