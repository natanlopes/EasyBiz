package br.com.easybiz.model;

import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "negocio_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NegocioConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "negocio_id", nullable = false, unique = true)
    private Negocio negocio;

    // Dias que normalmente atende (opcional)
    @ElementCollection
    @CollectionTable(
        name = "negocio_dias_disponiveis",
        joinColumns = @JoinColumn(name = "config_id")
    )
    @Column(name = "dia_semana")
    private Set<String> diasDisponiveis;

    // Turnos genéricos (opcional)
    @ElementCollection
    @CollectionTable(
        name = "negocio_turnos_disponiveis",
        joinColumns = @JoinColumn(name = "config_id")
    )
    @Column(name = "turno")
    private Set<String> turnosDisponiveis;

    // Texto livre (ESSENCIAL para pedreiro, mecânico, etc)
    @Column(length = 255)
    private String observacao;

    // Indica se ele aceita combinar horário pelo app
    private Boolean aceitaContato = true;
}
