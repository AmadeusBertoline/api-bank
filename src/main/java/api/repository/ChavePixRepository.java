package api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import api.model.ChavePix;

public interface ChavePixRepository extends JpaRepository<ChavePix, Long> {
    List<ChavePix> findAllByContaId(Long conta);
}
