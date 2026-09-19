package com.sigo.asistencia.programacion.dto;

import java.time.LocalDate;

public record ProgramacionDiaResponse(
        Long id,
        LocalDate fecha,
        String codigo,
        String descripcion,
        Long trabajadorId,
        Integer trabajadorCodigo,
        String trabajador,
        Long plazaId,
        String plazaCodigo,
        Long viaId,
        Integer viaNumero,
        String viaNombre,
        String posicionEspecial,
        Long liderId,
        Integer liderCodigo,
        String lider
) {}
