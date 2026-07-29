package api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.model.Endereco;

public interface EnderecoRepository extends JpaRepository<Endereco, Long>{
    
}
