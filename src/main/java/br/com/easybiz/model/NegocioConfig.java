package br.com.easybiz.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.Set;

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
