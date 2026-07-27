package api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import api.model.Pix;

public interface PixRepository extends JpaRepository<Pix, UUID>{
    
}
