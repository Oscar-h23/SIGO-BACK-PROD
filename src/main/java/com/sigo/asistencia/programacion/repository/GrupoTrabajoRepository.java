package com.sigo.asistencia.programacion.repository;

import com.sigo.asistencia.programacion.entity.GrupoTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoTrabajoRepository extends JpaRepository<GrupoTrabajo, Long> {
    List<GrupoTrabajo> findByPlazaIdAndActivoTrueOrderByNombreAsc(Long plazaId);
}
