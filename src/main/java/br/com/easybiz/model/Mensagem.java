package br.com.easybiz.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
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
@Table(name = "mensagem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "pedido_servico_id", nullable = false)
    private PedidoServico pedidoServico;

    @ManyToOne
    @JoinColumn(name = "remetente_id", nullable = false)
    private Usuario remetente;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String conteudo;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm;

    @Column(nullable = false)
    private Boolean lida = false;

    @Column(name = "lida_em")
    private LocalDateTime lidaEm;

    @PrePersist
    public void prePersist() {
        this.enviadoEm = LocalDateTime.now();
        if (this.lida == null) {
            this.lida = false;
        }
    }
}
