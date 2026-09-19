package com.sigo.asistencia.programacion.entity;

import com.sigo.asistencia.personal.entity.Trabajador;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "programacion_grupo_miembro",
    uniqueConstraints = @UniqueConstraint(name = "uq_programacion_miembro_trabajador", columnNames = "trabajador_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GrupoTrabajoMiembro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoTrabajo grupo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Trabajador trabajador;
}
