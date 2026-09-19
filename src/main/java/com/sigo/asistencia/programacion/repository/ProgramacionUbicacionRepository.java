package com.sigo.asistencia.programacion.repository;

import com.sigo.asistencia.programacion.entity.ProgramacionUbicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgramacionUbicacionRepository extends JpaRepository<ProgramacionUbicacion,Long> {
    List<ProgramacionUbicacion> findByPlazaIdAndActivoTrueOrderByOrdenAscCodigoAsc(Long plazaId);
}
