package api.repository;

import api.enums.TipoConta;
import api.model.Conta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {

    boolean existsByUsuarioEmailAndTipoConta(String email, TipoConta tipoConta);

    Optional<Conta> findByUsuarioEmailAndTipoConta(String email, TipoConta tipoConta);

}
