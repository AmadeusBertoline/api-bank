package api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contas")
public class Conta {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "usuario_id", nullable = false)
        private Usuario usuario;

        @Column(nullable = false, unique = true, length = 20)
        private String numeroConta;

        @Column(nullable = false)
        private BigDecimal saldo;

        @Column(nullable = false)
        private String tipoConta;

        @Column(nullable = false)
        private Boolean ativa;

        @Column(nullable = false)
        private LocalDateTime dataCriacao;

        @PrePersist
        public void prePersist(){
            this.dataCriacao = LocalDateTime.now();
            this.ativa = true;
            if(this.saldo == null){
                this.saldo = BigDecimal.ZERO;
            }
        }

}