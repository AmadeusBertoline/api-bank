package api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.model.ChavePix;

public interface ChavePixRepository extends JpaRepository<ChavePix, Long> {
    
}
