package com.sigo.asistencia.asistencia.repository;

import com.sigo.asistencia.asistencia.entity.AsistenciaAusencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsistenciaAusenciaRepository
        extends JpaRepository<AsistenciaAusencia, Long> {

 List<AsistenciaAusencia> findByAsistenciaId(
         Long asistenciaId
 );

 List<AsistenciaAusencia> findByAsistenciaIdIn(
         List<Long> asistenciaIds
 );

 void deleteByAsistenciaId(
         Long asistenciaId
 );

 @Query(
         value = """
                    SELECT
                        ma.nombre,
                        COUNT(aa.id)
                    FROM asistencia_ausencia aa
                    INNER JOIN asistencia_registro ar
                        ON ar.id = aa.asistencia_id
                    INNER JOIN motivos_ausencia ma
                        ON ma.id = aa.motivo_id
                    WHERE EXTRACT(YEAR FROM ar.fecha) = :anio
                      AND (
                            :mes IS NULL
                            OR EXTRACT(MONTH FROM ar.fecha) = :mes
                          )
                    GROUP BY ma.id, ma.nombre
                    ORDER BY COUNT(aa.id) DESC
                    """,
         nativeQuery = true
 )
 List<Object[]> contarPorMotivo(
         @Param("anio") int anio,
         @Param("mes") Integer mes
 );

 @Query(
         value = """
                    SELECT DISTINCT
                        t.codigo,
                        t.nombre_completo
                    FROM asistencia_ausencia aa
                    INNER JOIN trabajadores t
                        ON t.id = aa.trabajador_id
                    WHERE
                        (
                            :codigo IS NULL
                            OR t.codigo = :codigo
                        )
                      AND
                        (
                            :nombre IS NULL
                            OR LOWER(t.nombre_completo)
                               LIKE LOWER(CONCAT('%', :nombre, '%'))
                        )
                    ORDER BY t.nombre_completo
                    """,
         nativeQuery = true
 )
 List<Object[]> buscarTrabajadoresConFaltas(
         @Param("nombre") String nombre,
         @Param("codigo") Integer codigo
 );

 @Query(
         value = """
                    SELECT
                        t.codigo,
                        t.nombre_completo,
                        COUNT(aa.id) AS total_faltas
                    FROM asistencia_ausencia aa
                    INNER JOIN asistencia_registro ar
                        ON ar.id = aa.asistencia_id
                    INNER JOIN trabajadores t
                        ON t.id = aa.trabajador_id
                    WHERE
                        (
                            :codigo IS NULL
                            OR t.codigo = :codigo
                        )
                      AND
                        (
                            :nombre IS NULL
                            OR LOWER(t.nombre_completo)
                               LIKE LOWER(CONCAT('%', :nombre, '%'))
                        )
                      AND
                        (
                            CAST(:desde AS date) IS NULL
                            OR ar.fecha >= CAST(:desde AS date)
                        )
                      AND
                        (
                            CAST(:hasta AS date) IS NULL
                            OR ar.fecha <= CAST(:hasta AS date)
                        )
                    GROUP BY
                        t.id,
                        t.codigo,
                        t.nombre_completo
                    ORDER BY
                        total_faltas DESC,
                        t.nombre_completo ASC
                    """,
         nativeQuery = true
 )
 List<Object[]> resumenFaltasTrabajador(
         @Param("nombre") String nombre,
         @Param("codigo") Integer codigo,
         @Param("desde") LocalDate desde,
         @Param("hasta") LocalDate hasta
 );

 @Query(
         value = """
                    SELECT
                        ar.fecha,
                        ma.nombre
                    FROM asistencia_ausencia aa
                    INNER JOIN asistencia_registro ar
                        ON ar.id = aa.asistencia_id
                    INNER JOIN trabajadores t
                        ON t.id = aa.trabajador_id
                    INNER JOIN motivos_ausencia ma
                        ON ma.id = aa.motivo_id
                    WHERE t.codigo = :codigo
                      AND
                        (
                            CAST(:desde AS date) IS NULL
                            OR ar.fecha >= CAST(:desde AS date)
                        )
                      AND
                        (
                            CAST(:hasta AS date) IS NULL
                            OR ar.fecha <= CAST(:hasta AS date)
                        )
                    ORDER BY ar.fecha DESC
                    """,
         nativeQuery = true
 )
 List<Object[]> fechasFaltasTrabajador(
         @Param("codigo") Integer codigo,
         @Param("desde") LocalDate desde,
         @Param("hasta") LocalDate hasta
 );

 @Query(
         value = """
                    SELECT
                        t.codigo,
                        t.nombre_completo,
                        COUNT(aa.id) AS total_faltas
                    FROM asistencia_ausencia aa
                    INNER JOIN asistencia_registro ar
                        ON ar.id = aa.asistencia_id
                    INNER JOIN trabajadores t
                        ON t.id = aa.trabajador_id
                    INNER JOIN plazas p
                        ON p.id = ar.plaza_id
                    WHERE
                        (
                            CAST(:desde AS date) IS NULL
                            OR ar.fecha >= CAST(:desde AS date)
                        )
                      AND
                        (
                            CAST(:hasta AS date) IS NULL
                            OR ar.fecha <= CAST(:hasta AS date)
                        )
                      AND
                        (
                            :plaza IS NULL
                            OR UPPER(p.codigo) = UPPER(:plaza)
                        )
                    GROUP BY
                        t.id,
                        t.codigo,
                        t.nombre_completo
                    ORDER BY
                        total_faltas DESC,
                        t.nombre_completo ASC
                    LIMIT :limite
                    """,
         nativeQuery = true
 )
 List<Object[]> topFaltas(
         @Param("desde") LocalDate desde,
         @Param("hasta") LocalDate hasta,
         @Param("plaza") String plaza,
         @Param("limite") int limite
 );

 @Query(
         value = """
                    SELECT COUNT(aa.id)
                    FROM asistencia_ausencia aa
                    INNER JOIN asistencia_registro ar
                        ON ar.id = aa.asistencia_id
                    INNER JOIN plazas p
                        ON p.id = ar.plaza_id
                    WHERE
                        (
                            CAST(:desde AS date) IS NULL
                            OR ar.fecha >= CAST(:desde AS date)
                        )
                      AND
                        (
                            CAST(:hasta AS date) IS NULL
                            OR ar.fecha <= CAST(:hasta AS date)
                        )
                      AND
                        (
                            :plaza IS NULL
                            OR UPPER(p.codigo) = UPPER(:plaza)
                        )
                    """,
         nativeQuery = true
 )
 long totalFaltas(
         @Param("desde") LocalDate desde,
         @Param("hasta") LocalDate hasta,
         @Param("plaza") String plaza
 );

 @Query(
         value = """
                    SELECT COUNT(aa.id)
                    FROM asistencia_ausencia aa
                    INNER JOIN asistencia_registro ar
                        ON ar.id = aa.asistencia_id
                    INNER JOIN motivos_ausencia ma
                        ON ma.id = aa.motivo_id
                    INNER JOIN plazas p
                        ON p.id = ar.plaza_id
                    WHERE
                        LOWER(ma.nombre)
                        LIKE LOWER(CONCAT('%', :motivo, '%'))
                      AND
                        (
                            CAST(:desde AS date) IS NULL
                            OR ar.fecha >= CAST(:desde AS date)
                        )
                      AND
                        (
                            CAST(:hasta AS date) IS NULL
                            OR ar.fecha <= CAST(:hasta AS date)
                        )
                      AND
                        (
                            :plaza IS NULL
                            OR UPPER(p.codigo) = UPPER(:plaza)
                        )
                    """,
         nativeQuery = true
 )
 long totalFaltasPorMotivo(
         @Param("motivo") String motivo,
         @Param("desde") LocalDate desde,
         @Param("hasta") LocalDate hasta,
         @Param("plaza") String plaza
 );

 @Query(
         value = """
                    SELECT
                        ma.nombre,
                        COUNT(aa.id) AS total
                    FROM asistencia_ausencia aa
                    INNER JOIN asistencia_registro ar
                        ON ar.id = aa.asistencia_id
                    INNER JOIN motivos_ausencia ma
                        ON ma.id = aa.motivo_id
                    INNER JOIN plazas p
                        ON p.id = ar.plaza_id
                    WHERE
                        (
                            CAST(:desde AS date) IS NULL
                            OR ar.fecha >= CAST(:desde AS date)
                        )
                      AND
                        (
                            CAST(:hasta AS date) IS NULL
                            OR ar.fecha <= CAST(:hasta AS date)
                        )
                      AND
                        (
                            :plaza IS NULL
                            OR UPPER(p.codigo) = UPPER(:plaza)
                        )
                    GROUP BY
                        ma.id,
                        ma.nombre
                    ORDER BY
                        total DESC,
                        ma.nombre ASC
                    LIMIT :limite
                    """,
         nativeQuery = true
 )
 List<Object[]> topMotivos(
         @Param("desde") LocalDate desde,
         @Param("hasta") LocalDate hasta,
         @Param("plaza") String plaza,
         @Param("limite") int limite
 );
}
