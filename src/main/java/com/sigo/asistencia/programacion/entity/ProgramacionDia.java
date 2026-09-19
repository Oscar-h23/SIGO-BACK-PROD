package com.sigo.asistencia.programacion.entity;

import com.sigo.asistencia.personal.entity.Plaza;
import com.sigo.asistencia.personal.entity.Trabajador;
import com.sigo.asistencia.relevo.entity.Via;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "programacion_dia",
    uniqueConstraints = @UniqueConstraint(name = "uq_programacion_trabajador_fecha", columnNames = {"trabajador_id", "fecha"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProgramacionDia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Trabajador trabajador;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "plaza_id", nullable = false)
    private Plaza plaza;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "jornada_codigo", nullable = false, length = 10)
    private CodigoJornada jornadaCodigo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "via_id")
    private Via via;

    @Column(name = "posicion_especial", length = 30)
    private String posicionEspecial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por_id")
    private Trabajador actualizadoPor;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
