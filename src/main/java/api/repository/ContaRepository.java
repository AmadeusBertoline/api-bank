package api.repository;

import api.model.Conta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long>{

    Optional<Conta> findByUsuarioEmail(String email);

}
