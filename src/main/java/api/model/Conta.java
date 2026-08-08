package api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import api.enums.StatusConta;
import api.enums.TipoConta;

@Data
@Entity
@Table(name = "contas")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "conta", fetch = FetchType.LAZY)
    private List<ChavePix> chavesPix;

    @Column(nullable = false, length = 4)
    private String agencia = "0001";

    @Column(name = "numero_conta", nullable = false, unique = true, length = 20)
    private String numeroConta;

    @Column(length = 2)
    private String digito;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConta tipoConta;

    @Column(nullable = false)
    private StatusConta status;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private BigDecimal limiteDiario;


    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusConta.ATIVA;
        this.limiteDiario = new BigDecimal("500.00");
        if (this.saldo == null) {
            this.saldo = new BigDecimal("10000.00");
        }
        if (this.agencia == null || this.agencia.isBlank()) {
            this.agencia = "0001";
        }
    }

}