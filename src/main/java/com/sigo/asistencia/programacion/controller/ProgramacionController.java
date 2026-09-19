package com.sigo.asistencia.programacion.controller;

import com.sigo.asistencia.programacion.service.ProgramacionService;
import com.sigo.asistencia.programacion.service.ProgramacionService.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/programacion")
@RequiredArgsConstructor
public class ProgramacionController {
    private final ProgramacionService service;

    @GetMapping("/turnos")
    @PreAuthorize("hasAnyRole('SUPERVISOR','CONTROLADOR')")
    public List<ProgramacionDiaResponse> turnos(@RequestParam Long plazaId,@RequestParam int anio,@RequestParam int mes){
        return service.listarTurnos(plazaId,anio,mes);
    }

    @PutMapping("/turnos")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<ProgramacionDiaResponse> guardar(@Valid @RequestBody GuardarProgramacionRequest request){
        return service.guardarTurnos(request);
    }

    @GetMapping("/mi-horario")
    @PreAuthorize("hasAnyRole('SUPERVISOR','CONTROLADOR','OPERADOR')")
    public MiHorarioResponse miHorario(
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate hasta){
        return service.miHorario(desde,hasta);
    }

    @GetMapping("/grupos")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<GrupoLiderResponse> grupos(@RequestParam Long plazaId){
        return service.listarLideres(plazaId);
    }

    @PutMapping("/grupos/lider")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public GrupoLiderResponse lider(@Valid @RequestBody GrupoLiderRequest request){
        return service.asignarLider(request);
    }
}
