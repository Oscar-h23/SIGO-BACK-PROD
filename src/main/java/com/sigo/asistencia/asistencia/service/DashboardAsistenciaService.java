package com.sigo.asistencia.asistencia.service;

import com.sigo.asistencia.asistencia.dto.AusenciaMotivoResponse;
import com.sigo.asistencia.asistencia.dto.DashboardPuntoResponse;
import com.sigo.asistencia.asistencia.dto.ResumenAsistenciaResponse;

import com.sigo.asistencia.asistencia.repository.AsistenciaAusenciaRepository;
import com.sigo.asistencia.asistencia.repository.AsistenciaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardAsistenciaService {

 private final AsistenciaRepository ar;
 private final AsistenciaAusenciaRepository aur;

 /*
  * =============================================
  * ASISTENCIA DIARIA
  * =============================================
  */
 @Transactional(readOnly = true)
 public List<DashboardPuntoResponse> diario(
         int anio,
         int mes
 ) {

  validarMes(mes);

  return ar.obtenerDiario(anio, mes)
          .stream()
          .map(this::convertirPunto)
          .toList();
 }

 /*
  * =============================================
  * ASISTENCIA ANUAL
  * =============================================
  */
 @Transactional(readOnly = true)
 public List<DashboardPuntoResponse> anual(
         int anio
 ) {

  return ar.obtenerAnual(anio)
          .stream()
          .map(this::convertirPunto)
          .toList();
 }

 /*
  * =============================================
  * AUSENCIAS POR MOTIVO
  * =============================================
  */
 @Transactional(readOnly = true)
 public List<AusenciaMotivoResponse> motivos(
         int anio,
         Integer mes
 ) {

  if (mes != null) {
   validarMes(mes);
  }

  return aur.contarPorMotivo(anio, mes)
          .stream()
          .map((Object[] r) ->
                  new AusenciaMotivoResponse(
                          String.valueOf(r[0]),
                          ((Number) r[1]).longValue()
                  )
          )
          .toList();
 }

 /*
  * =============================================
  * RESUMEN
  * =============================================
  */
 @Transactional(readOnly = true)
 public ResumenAsistenciaResponse resumen(
         LocalDate inicio,
         LocalDate fin
 ) {

  Object[] r =
          ar.obtenerResumen(inicio, fin).get(0);

  long registros =
          ((Number) r[0]).longValue();

  long presentes =
          ((Number) r[1]).longValue();

  long programados =
          ((Number) r[2]).longValue();

  long ausentes =
          ((Number) r[3]).longValue();

  BigDecimal porcentaje;

  if (programados == 0) {

   porcentaje = BigDecimal.ZERO;

  } else {

   porcentaje =
           BigDecimal
                   .valueOf(presentes)
                   .multiply(
                           BigDecimal.valueOf(100)
                   )
                   .divide(
                           BigDecimal.valueOf(programados),
                           2,
                           RoundingMode.HALF_UP
                   );
  }

  return new ResumenAsistenciaResponse(
          registros,
          presentes,
          programados,
          ausentes,
          porcentaje
  );
 }

 /*
  * =============================================
  * CONVERTIR CONSULTA SQL
  * =============================================
  */
 private DashboardPuntoResponse convertirPunto(
         Object[] r
 ) {

  return new DashboardPuntoResponse(
          ((Number) r[0]).intValue(),
          ((Number) r[1]).longValue(),
          ((Number) r[2]).longValue(),
          (BigDecimal) r[3]
  );
 }

 /*
  * =============================================
  * VALIDAR MES
  * =============================================
  */
 private void validarMes(int mes) {

  if (mes < 1 || mes > 12) {
   throw new IllegalArgumentException(
           "El mes debe estar entre 1 y 12"
   );
  }
 }
}