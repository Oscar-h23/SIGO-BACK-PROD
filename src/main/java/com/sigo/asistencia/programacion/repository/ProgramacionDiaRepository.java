package com.sigo.asistencia.programacion.repository;

import com.sigo.asistencia.programacion.entity.ProgramacionDia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProgramacionDiaRepository extends JpaRepository<ProgramacionDia, Long> {
    Optional<ProgramacionDia> findByTrabajadorIdAndFecha(Long trabajadorId, LocalDate fecha);
    List<ProgramacionDia> findByTrabajadorIdAndFechaBetweenOrderByFechaAsc(Long trabajadorId, LocalDate inicio, LocalDate fin);
    List<ProgramacionDia> findByPlazaIdAndFechaBetweenOrderByTrabajadorNombreCompletoAscFechaAsc(Long plazaId, LocalDate inicio, LocalDate fin);
}
