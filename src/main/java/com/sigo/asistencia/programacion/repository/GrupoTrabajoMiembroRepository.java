package com.sigo.asistencia.programacion.repository;

import com.sigo.asistencia.programacion.entity.GrupoTrabajoMiembro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GrupoTrabajoMiembroRepository extends JpaRepository<GrupoTrabajoMiembro, Long> {
    List<GrupoTrabajoMiembro> findByGrupoIdOrderByTrabajadorNombreCompletoAsc(Long grupoId);
    Optional<GrupoTrabajoMiembro> findByTrabajadorId(Long trabajadorId);
    void deleteByGrupoId(Long grupoId);
    void deleteByTrabajadorIdIn(Collection<Long> trabajadorIds);
}
