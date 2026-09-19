package com.sigo.asistencia.programacion.dto;

import java.time.LocalDate;

public record CoberturaResponse(LocalDate fecha, String posicion, long cantidad) {}
