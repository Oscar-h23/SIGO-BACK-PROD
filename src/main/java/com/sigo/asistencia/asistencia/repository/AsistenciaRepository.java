package com.sigo.asistencia.asistencia.repository;

import com.sigo.asistencia.asistencia.entity.AsistenciaRegistro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository
        extends JpaRepository<AsistenciaRegistro, Long> {

 boolean existsByPlazaIdAndTurnoIdAndFecha(
         Long plazaId,
         Long turnoId,
         LocalDate fecha
 );

 boolean existsByPlazaIdAndTurnoIdAndFechaAndIdNot(
         Long plazaId,
         Long turnoId,
         LocalDate fecha,
         Long id
 );

 /*
  * Historial por rango de fechas.
  * Se usa cuando:
  * plazaId = null
  * "Todas las plazas"
  */
 List<AsistenciaRegistro>
 findByFechaBetweenOrderByFechaDescIdDesc(
         LocalDate inicio,
         LocalDate fin
 );

 /*
  * Historial por rango de fechas + plaza.
  */
 List<AsistenciaRegistro>
 findByFechaBetweenAndPlazaIdOrderByFechaDescIdDesc(
         LocalDate inicio,
         LocalDate fin,
         Long plazaId
 );

 @Query(
         value = """
                    select
                        extract(day from fecha)::int,
                        sum(presentes)::bigint,
                        sum(programados)::bigint,
                        round(
                            sum(presentes)::numeric /
                            nullif(sum(programados),0) * 100,
                            2
                        )
                    from asistencia_registro
                    where extract(year from fecha) = :anio
                    and extract(month from fecha) = :mes
                    group by extract(day from fecha)
                    order by 1
                    """,
         nativeQuery = true
 )
 List<Object[]> obtenerDiario(
         @Param("anio") int anio,
         @Param("mes") int mes
 );

 @Query(
         value = """
                    select
                        extract(month from fecha)::int,
                        sum(presentes)::bigint,
                        sum(programados)::bigint,
                        round(
                            sum(presentes)::numeric /
                            nullif(sum(programados),0) * 100,
                            2
                        )
                    from asistencia_registro
                    where extract(year from fecha) = :anio
                    group by extract(month from fecha)
                    order by 1
                    """,
         nativeQuery = true
 )
 List<Object[]> obtenerAnual(
         @Param("anio") int anio
 );

 @Query(
         value = """
                    select
                        count(*)::bigint,
                        coalesce(sum(presentes),0)::bigint,
                        coalesce(sum(programados),0)::bigint,
                        coalesce(
                            sum(programados-presentes),
                            0
                        )::bigint
                    from asistencia_registro
                    where (:inicio is null or fecha >= :inicio)
                    and (:fin is null or fecha <= :fin)
                    """,
         nativeQuery = true
 )
 List<Object[]> obtenerResumen(
         @Param("inicio") LocalDate inicio,
         @Param("fin") LocalDate fin
 );
}