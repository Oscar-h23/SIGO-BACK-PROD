package com.sigo.asistencia.relevo.controller;
import com.sigo.asistencia.asistencia.dto.*; import com.sigo.asistencia.relevo.dto.*;
import com.sigo.asistencia.relevo.service.RelevoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/relevos") @RequiredArgsConstructor
public class RelevoController {
 private final RelevoService service;
 @GetMapping("/elementos") public List<ElementoRelevoResponse> elementos(){ return service.listarElementos(); }
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public RelevoResponse registrar(@Valid @RequestBody RelevoRequest r){ return service.registrar(r); }
 @GetMapping("/{id}") public RelevoResponse obtener(@PathVariable Long id){ return service.obtener(id); }
 @GetMapping public List<RelevoResponse> listar(
  @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
  @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fin){ return service.listar(inicio,fin); }
 @PostMapping(value="/checklist/{checklistId}/evidencias",consumes="multipart/form-data")
 @ResponseStatus(HttpStatus.CREATED)
 public EvidenciaRelevoResponse evidenciaChecklist(@PathVariable Long checklistId,@RequestParam("file") MultipartFile file)throws IOException{
  return service.subirEvidenciaChecklist(checklistId,file);
 }
 @PostMapping(value="/vias/{relevoViaId}/evidencias",consumes="multipart/form-data")
 @ResponseStatus(HttpStatus.CREATED)
 public EvidenciaRelevoResponse evidenciaVia(@PathVariable Long relevoViaId,@RequestParam("file") MultipartFile file)throws IOException{
  return service.subirEvidenciaVia(relevoViaId,file);
 }
}
