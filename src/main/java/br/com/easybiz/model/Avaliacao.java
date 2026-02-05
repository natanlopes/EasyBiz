package br.com.easybiz.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer nota; // 1 a 5
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private PedidoServico pedido;

    @ManyToOne
    @JoinColumn(name = "avaliador_id")
    private Usuario avaliador; // Quem deu a nota (Geralmente o cliente)

    @ManyToOne
    @JoinColumn(name = "avaliado_id")
    private Usuario avaliado; // Quem recebeu a nota (O Prestador/Dono do Negócio)

    private LocalDateTime dataAvaliacao = LocalDateTime.now();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getNota() {
		return nota;
	}

	public void setNota(Integer nota) {
		this.nota = nota;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public PedidoServico getPedido() {
		return pedido;
	}

	public void setPedido(PedidoServico pedido) {
		this.pedido = pedido;
	}

	public Usuario getAvaliador() {
		return avaliador;
	}

	public void setAvaliador(Usuario avaliador) {
		this.avaliador = avaliador;
	}

	public Usuario getAvaliado() {
		return avaliado;
	}

	public void setAvaliado(Usuario avaliado) {
		this.avaliado = avaliado;
	}

	public LocalDateTime getDataAvaliacao() {
		return dataAvaliacao;
	}

	public void setDataAvaliacao(LocalDateTime dataAvaliacao) {
		this.dataAvaliacao = dataAvaliacao;
	}

}