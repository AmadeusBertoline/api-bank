package api.model;

import java.util.UUID;
import api.enums.TipoChavePix;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "pix")

public class Pix {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transacao_id", nullable = false, unique = true)
    private Transacao transacao;

    @Column(name = "end_to_end_id", nullable = false, unique = true, length = 35)
    private String endToEndId;

    @Column(name = "chave_pix", nullable = false, length = 77)
    private String chavePix;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chave", nullable = false, length = 20)
    private TipoChavePix tipoChave;

}
