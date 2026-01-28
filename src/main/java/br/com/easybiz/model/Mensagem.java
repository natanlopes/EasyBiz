package br.com.easybiz.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    public Boolean getLida() {
		return lida;
	}
	public void setLida(Boolean lida) {
		this.lida = lida;
	}
	public LocalDateTime getLidaEm() {
		return lidaEm;
	}
	public void setLidaEm(LocalDateTime lidaEm) {
		this.lidaEm = lidaEm;
	}
	@PrePersist
    public void prePersist() {
        this.enviadoEm = LocalDateTime.now();
        if (this.lida == null) {
            this.lida = false;
        }
    }
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PedidoServico getPedidoServico() {
		return pedidoServico;
	}

	public void setPedidoServico(PedidoServico pedidoServico) {
		this.pedidoServico = pedidoServico;
	}

	public Usuario getRemetente() {
		return remetente;
	}

	public void setRemetente(Usuario remetente) {
		this.remetente = remetente;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

	public LocalDateTime getEnviadoEm() {
		return enviadoEm;
	}

	public void setEnviadoEm(LocalDateTime enviadoEm) {
		this.enviadoEm = enviadoEm;
	}
    
}
