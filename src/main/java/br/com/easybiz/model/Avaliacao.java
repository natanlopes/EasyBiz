package br.com.easybiz.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "avaliacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer nota;
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private PedidoServico pedido;

    @ManyToOne
    @JoinColumn(name = "avaliador_id")
    private Usuario avaliador;

    @ManyToOne
    @JoinColumn(name = "avaliado_id")
    private Usuario avaliado;

    private LocalDateTime dataAvaliacao;

    @PrePersist
    public void prePersist() {
        this.dataAvaliacao = LocalDateTime.now();
    }
}
