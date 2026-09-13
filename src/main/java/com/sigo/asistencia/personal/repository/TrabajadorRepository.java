package com.sigo.asistencia.personal.repository;

import com.sigo.asistencia.personal.entity.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrabajadorRepository
        extends JpaRepository<Trabajador, Long> {

    Optional<Trabajador> findByCodigo(Integer codigo);

    List<Trabajador> findByActivoTrueOrderByNombreCompletoAsc();

    List<Trabajador>
    findByPuestoNombreIgnoreCaseAndActivoTrueOrderByNombreCompletoAsc(
            String puesto
    );

    /*
     * Agentes correspondientes a una plaza.
     *
     * Incluye:
     * - Agente de Recaudación
     * - Agente de Recaudacion - Part Time
     * - Agente de Recaudación Suplencia
     */
    @Query("""
        SELECT t
        FROM Trabajador t
        JOIN FETCH t.puesto p
        WHERE t.plaza.id = :plazaId
          AND t.activo = true
          AND p.nombre IN (
              'Agente de Recaudación',
              'Agente de Recaudacion - Part Time',
              'Agente de Recaudación Suplencia'
          )
        ORDER BY t.nombreCompleto ASC
    """)
    List<Trabajador> findAgentesByPlaza(
            @Param("plazaId") Long plazaId
    );

    /*
     * Controladores correspondientes a una plaza.
     *
     * Incluye:
     * - Controlador
     * - Controlador ATF
     */
    @Query("""
        SELECT t
        FROM Trabajador t
        JOIN FETCH t.puesto p
        WHERE t.plaza.id = :plazaId
          AND t.activo = true
          AND p.nombre IN (
              'Controlador',
              'Controlador ATF'
          )
        ORDER BY t.nombreCompleto ASC
    """)
    List<Trabajador> findControladoresByPlaza(
            @Param("plazaId") Long plazaId
    );
}