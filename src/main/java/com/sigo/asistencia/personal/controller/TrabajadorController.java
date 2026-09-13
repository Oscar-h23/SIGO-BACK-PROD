package com.sigo.asistencia.personal.controller;

import com.sigo.asistencia.personal.entity.Trabajador;
import com.sigo.asistencia.shared.exception.ResourceNotFoundException;
import com.sigo.asistencia.personal.repository.TrabajadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trabajadores")
@RequiredArgsConstructor
public class TrabajadorController {

    private final TrabajadorRepository r;

    @GetMapping
    public List<Trabajador> listar(
            @RequestParam(required = false) String puesto
    ) {

        return puesto == null || puesto.isBlank()
                ? r.findByActivoTrueOrderByNombreCompletoAsc()
                : r.findByPuestoNombreIgnoreCaseAndActivoTrueOrderByNombreCompletoAsc(
                puesto
        );
    }

    @GetMapping("/{id}")
    public Trabajador obtener(
            @PathVariable Long id
    ) {

        return r.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Trabajador no encontrado"
                        )
                );
    }

    @GetMapping("/codigo/{codigo}")
    public Trabajador codigo(
            @PathVariable Integer codigo
    ) {

        return r.findByCodigo(codigo)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Trabajador no encontrado"
                        )
                );
    }

    /*
     * Devuelve todos los agentes activos
     * pertenecientes a una plaza.
     *
     * Incluye:
     * - Agente de Recaudación
     * - Agente de Recaudacion - Part Time
     * - Agente de Recaudación Suplencia
     */
    @GetMapping("/agentes")
    public List<Trabajador> listarAgentesPorPlaza(
            @RequestParam Long plazaId
    ) {

        return r.findAgentesByPlaza(plazaId);
    }

    /*
     * Devuelve todos los controladores activos
     * pertenecientes a una plaza.
     *
     * Incluye:
     * - Controlador
     * - Controlador ATF
     */
    @GetMapping("/controladores")
    public List<Trabajador> listarControladoresPorPlaza(
            @RequestParam Long plazaId
    ) {

        return r.findControladoresByPlaza(plazaId);
    }
}