package com.sigo.asistencia.asistencia.controller;

import com.sigo.asistencia.asistencia.dto.AsistenciaRequest;
import com.sigo.asistencia.asistencia.dto.AsistenciaResponse;
import com.sigo.asistencia.asistencia.dto.AsistenciaUpdateRequest;
import com.sigo.asistencia.asistencia.dto.EvidenciaResponse;
import com.sigo.asistencia.asistencia.service.AsistenciaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
@RequiredArgsConstructor
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    /*
     * =========================================================
     * REGISTRAR ASISTENCIA
     * =========================================================
     */
    @PostMapping
    public ResponseEntity<AsistenciaResponse> registrar(
            @Valid @RequestBody AsistenciaRequest request
    ) {

        return ResponseEntity.ok(
                asistenciaService.registrar(request)
        );
    }

    /*
     * =========================================================
     * ACTUALIZAR ASISTENCIA
     * =========================================================
     *
     * PUT /api/asistencias/1
     */
    @PutMapping("/{id}")
    public ResponseEntity<AsistenciaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AsistenciaUpdateRequest request
    ) {

        return ResponseEntity.ok(
                asistenciaService.actualizar(
                        id,
                        request
                )
        );
    }

    /*
     * =========================================================
     * LISTAR ASISTENCIAS
     * =========================================================
     *
     * Ejemplos:
     *
     * GET /api/asistencias
     *
     * Por defecto:
     * inicio = hoy
     * fin = hoy
     * plaza = todas
     *
     * GET /api/asistencias?plazaId=3
     *
     * GET /api/asistencias
     * ?inicio=2026-09-01
     * &fin=2026-09-02
     *
     * GET /api/asistencias
     * ?inicio=2026-09-01
     * &fin=2026-09-02
     * &plazaId=3
     */
    @GetMapping
    public ResponseEntity<List<AsistenciaResponse>> listar(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fin,

            @RequestParam(required = false)
            Long plazaId
    ) {

        return ResponseEntity.ok(
                asistenciaService.listar(
                        inicio,
                        fin,
                        plazaId
                )
        );
    }

    /*
     * =========================================================
     * OBTENER ASISTENCIA POR ID
     * =========================================================
     */
    @GetMapping("/{id}")
    public ResponseEntity<AsistenciaResponse> obtenerPorId(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                asistenciaService.obtenerPorId(id)
        );
    }

    /*
     * =========================================================
     * SUBIR EVIDENCIA
     * =========================================================
     *
     * POST /api/asistencias/1/evidencias
     *
     * Body:
     * form-data
     *
     * file = imagen
     * tipo = CALENTAMIENTO | INICIO_TURNO | TAPONES_AUDITIVOS
     */
    @PostMapping(
            value = "/{id}/evidencias",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<EvidenciaResponse> subirEvidencia(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("tipo") String tipo
    ) throws IOException {

        return ResponseEntity.ok(
                asistenciaService.guardarEvidencia(
                        id,
                        file,
                        tipo
                )
        );
    }

    /*
     * =========================================================
     * ELIMINAR EVIDENCIA
     * =========================================================
     *
     * DELETE
     * /api/asistencias/1/evidencias/5
     */
    @DeleteMapping(
            "/{asistenciaId}/evidencias/{evidenciaId}"
    )
    public ResponseEntity<Void> eliminarEvidencia(
            @PathVariable Long asistenciaId,
            @PathVariable Long evidenciaId
    ) throws IOException {

        asistenciaService.eliminarEvidencia(
                asistenciaId,
                evidenciaId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}