package com.sigo.asistencia.programacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GrupoTrabajoRequest(
        @NotNull Long plazaId,
        @NotBlank @Size(max = 80) String nombre,
        @NotNull Long controladorId,
        @NotEmpty List<Long> miembroIds
) {}
