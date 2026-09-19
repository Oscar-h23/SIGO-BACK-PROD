package com.sigo.asistencia.programacion.dto;

import java.util.List;

public record GrupoTrabajoResponse(
        Long id,
        Long plazaId,
        String plazaCodigo,
        String nombre,
        Long controladorId,
        Integer controladorCodigo,
        String controlador,
        List<MiembroResponse> miembros
) {
    public record MiembroResponse(Long id, Integer codigo, String nombre) {}
}
