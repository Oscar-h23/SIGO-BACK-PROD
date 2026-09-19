package com.sigo.asistencia.programacion.repository;

import com.sigo.asistencia.programacion.entity.ProgramacionTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProgramacionTurnoRepository extends JpaRepository<ProgramacionTurno,Long> {
    Optional<ProgramacionTurno> findByTrabajadorIdAndFecha(Long trabajadorId, LocalDate fecha);

    @Query("""
        select p from ProgramacionTurno p
        join fetch p.trabajador t
        join fetch p.plaza pl
        where pl.id=:plazaId and p.fecha between :desde and :hasta
        order by t.nombreCompleto asc, p.fecha asc
    """)
    List<ProgramacionTurno> findMes(@Param("plazaId") Long plazaId,@Param("desde") LocalDate desde,@Param("hasta") LocalDate hasta);

    @Query("""
        select p from ProgramacionTurno p
        join fetch p.trabajador t
        join fetch p.plaza pl
        where t.id=:trabajadorId and p.fecha between :desde and :hasta
        order by p.fecha asc
    """)
    List<ProgramacionTurno> findHorario(@Param("trabajadorId") Long trabajadorId,@Param("desde") LocalDate desde,@Param("hasta") LocalDate hasta);
}
