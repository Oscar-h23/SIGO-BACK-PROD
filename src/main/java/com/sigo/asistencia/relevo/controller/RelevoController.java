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
 @PutMapping("/{id}") public RelevoResponse actualizar(@PathVariable Long id,@Valid @RequestBody RelevoRequest r){ return service.actualizar(id,r); }
 @GetMapping("/{id}") public RelevoResponse obtener(@PathVariable Long id){ return service.obtener(id); }
 @GetMapping public List<RelevoResponse> listar(
  @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
  @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fin){ return service.listar(inicio,fin); }
 @PostMapping(value="/checklist/{checklistId}/evidencias",consumes="multipart/form-data")
 @ResponseStatus(HttpStatus.CREATED)
 public EvidenciaRelevoResponse evidenciaChecklist(@PathVariable Long checklistId,@RequestParam("file") MultipartFile file)throws IOException{
  return service.subirEvidenciaChecklist(checklistId,file);
 }
 @DeleteMapping("/checklist/{checklistId}/evidencias/{evidenciaId}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void eliminarEvidenciaChecklist(@PathVariable Long checklistId,@PathVariable Long evidenciaId)throws IOException{
  service.eliminarEvidenciaChecklist(checklistId,evidenciaId);
 }
 @PostMapping(value="/vias/{relevoViaId}/evidencias",consumes="multipart/form-data")
 @ResponseStatus(HttpStatus.CREATED)
 public EvidenciaRelevoResponse evidenciaVia(@PathVariable Long relevoViaId,@RequestParam("file") MultipartFile file)throws IOException{
  return service.subirEvidenciaVia(relevoViaId,file);
 }
 @DeleteMapping("/vias/{relevoViaId}/evidencias/{evidenciaId}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void eliminarEvidenciaVia(@PathVariable Long relevoViaId,@PathVariable Long evidenciaId)throws IOException{
  service.eliminarEvidenciaVia(relevoViaId,evidenciaId);
 }
}
