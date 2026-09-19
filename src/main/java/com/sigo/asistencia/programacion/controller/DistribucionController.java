package com.sigo.asistencia.programacion.controller;

import com.sigo.asistencia.programacion.service.ProgramacionService;
import com.sigo.asistencia.programacion.service.ProgramacionService.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/distribucion")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPERVISOR','CONTROLADOR')")
public class DistribucionController {
    private final ProgramacionService service;

    @GetMapping("/ubicaciones")
    public List<UbicacionResponse> ubicaciones(@RequestParam Long plazaId){
        return service.ubicaciones(plazaId);
    }

    @GetMapping
    public List<DistribucionDiaResponse> listar(@RequestParam Long plazaId,@RequestParam int anio,@RequestParam int mes){
        return service.listarDistribucion(plazaId,anio,mes);
    }

    @PutMapping
    public List<DistribucionDiaResponse> guardar(@Valid @RequestBody GuardarDistribucionRequest request){
        return service.guardarDistribucion(request);
    }

    @GetMapping("/resumen-trabajador/{trabajadorId}")
    public ResumenTrabajadorResponse resumen(@PathVariable Long trabajadorId,@RequestParam int anio,@RequestParam int mes){
        return service.resumen(trabajadorId,anio,mes);
    }

    @GetMapping("/cobertura")
    public List<CoberturaUbicacionResponse> cobertura(@RequestParam Long plazaId,@RequestParam int anio,@RequestParam int mes){
        return service.cobertura(plazaId,anio,mes);
    }
}
