package com.sigo.asistencia.programacion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AsignacionItemRequest(
        @NotNull Long trabajadorId,
        @NotNull LocalDate fecha,
        Long viaId,
        @Size(max = 30) String posicionEspecial
) {}
