package com.sigo.asistencia.programacion.dto;

import com.sigo.asistencia.programacion.entity.CodigoJornada;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record JornadaItemRequest(
        @NotNull Long trabajadorId,
        @NotNull LocalDate fecha,
        @NotNull CodigoJornada codigo
) {}
