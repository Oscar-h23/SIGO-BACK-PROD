package com.sigo.asistencia.programacion.controller;

import com.sigo.asistencia.programacion.dto.*;
import com.sigo.asistencia.programacion.service.ProgramacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/programacion")
@RequiredArgsConstructor
public class ProgramacionController {

    private final ProgramacionService service;

    @GetMapping("/me/semana")
    public List<ProgramacionDiaResponse> miSemana(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio
    ) {
        return service.miSemana(inicio);
    }

    @GetMapping("/plazas/{plazaId}/mes")
    public List<ProgramacionDiaResponse> mes(
            @PathVariable Long plazaId,
            @RequestParam int anio,
            @RequestParam int mes
    ) {
        return service.mesPlaza(plazaId, anio, mes);
    }

    @PutMapping("/supervisor/jornadas")
    public List<ProgramacionDiaResponse> guardarJornadas(
            @Valid @RequestBody List<@Valid JornadaItemRequest> items
    ) {
        return service.guardarJornadas(items);
    }

    @PutMapping("/controlador/asignaciones")
    public List<ProgramacionDiaResponse> guardarAsignaciones(
            @Valid @RequestBody List<@Valid AsignacionItemRequest> items
    ) {
        return service.guardarAsignaciones(items);
    }

    @GetMapping("/plazas/{plazaId}/cobertura")
    public List<CoberturaResponse> cobertura(
            @PathVariable Long plazaId,
            @RequestParam int anio,
            @RequestParam int mes
    ) {
        return service.cobertura(plazaId, anio, mes);
    }

    @GetMapping("/grupos")
    public List<GrupoTrabajoResponse> grupos(@RequestParam Long plazaId) {
        return service.grupos(plazaId);
    }

    @PostMapping("/grupos")
    public GrupoTrabajoResponse crearGrupo(@Valid @RequestBody GrupoTrabajoRequest request) {
        return service.crearGrupo(request);
    }

    @PutMapping("/grupos/{id}")
    public GrupoTrabajoResponse actualizarGrupo(
            @PathVariable Long id,
            @Valid @RequestBody GrupoTrabajoRequest request
    ) {
        return service.actualizarGrupo(id, request);
    }

    @DeleteMapping("/grupos/{id}")
    public ResponseEntity<Void> desactivarGrupo(@PathVariable Long id) {
        service.desactivarGrupo(id);
        return ResponseEntity.noContent().build();
    }
}
