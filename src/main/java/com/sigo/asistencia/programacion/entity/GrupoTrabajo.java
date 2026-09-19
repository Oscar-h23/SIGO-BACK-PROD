package com.sigo.asistencia.programacion.entity;

import com.sigo.asistencia.personal.entity.Plaza;
import com.sigo.asistencia.personal.entity.Trabajador;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "programacion_grupo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GrupoTrabajo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "plaza_id", nullable = false)
    private Plaza plaza;

    @Column(nullable = false, length = 80)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "controlador_id", nullable = false)
    private Trabajador controlador;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (activo == null) activo = true;
    }

    @PreUpdate void preUpdate() { updatedAt = OffsetDateTime.now(); }
}
