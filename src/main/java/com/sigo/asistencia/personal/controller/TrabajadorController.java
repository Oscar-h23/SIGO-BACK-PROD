package com.sigo.asistencia.personal.controller;

import com.sigo.asistencia.personal.dto.TrabajadorResponse;
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
    public List<TrabajadorResponse> listar(@RequestParam(required = false) String puesto) {
        var trabajadores = puesto == null || puesto.isBlank()
                ? r.findByActivoTrueOrderByNombreCompletoAsc()
                : r.findByPuestoNombreIgnoreCaseAndActivoTrueOrderByNombreCompletoAsc(puesto);
        return trabajadores.stream().map(TrabajadorResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TrabajadorResponse obtener(@PathVariable Long id) {
        return r.findById(id).map(TrabajadorResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado"));
    }

    @GetMapping("/codigo/{codigo}")
    public TrabajadorResponse codigo(@PathVariable Integer codigo) {
        return r.findByCodigo(codigo).map(TrabajadorResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado"));
    }

    @GetMapping("/agentes")
    public List<TrabajadorResponse> listarAgentesPorPlaza(@RequestParam Long plazaId) {
        return r.findAgentesByPlaza(plazaId).stream().map(TrabajadorResponse::from).toList();
    }

    @GetMapping("/controladores")
    public List<TrabajadorResponse> listarControladores(
            @RequestParam(required = false) Long plazaId
    ) {
        var lista = plazaId == null
                ? r.findControladoresActivos()
                : r.findControladoresByPlaza(plazaId);
        return lista.stream().map(TrabajadorResponse::from).toList();
    }
}
