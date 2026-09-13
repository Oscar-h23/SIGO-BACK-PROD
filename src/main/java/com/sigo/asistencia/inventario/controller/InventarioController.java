package com.sigo.asistencia.inventario.controller;
import com.sigo.asistencia.inventario.dto.request.*;
import com.sigo.asistencia.inventario.dto.response.*;
import com.sigo.asistencia.inventario.entity.EstadoInventario;
import com.sigo.asistencia.inventario.security.InventarioUsuarioContextService;
import com.sigo.asistencia.inventario.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/inventarios") @RequiredArgsConstructor
public class InventarioController {
  private final InventarioService service;
  private final InventarioUsuarioContextService usuarios;

  @PostMapping public InventarioResumenResponse iniciar(){
    return service.iniciar(usuarios.obtenerActual());
  }
  @GetMapping("/{id}/productos") public List<ProductoInventarioResponse> productos(@PathVariable Long id){
    return service.productosPermitidos(usuarios.obtenerActual(),id);
  }
  @PutMapping("/{id}/detalle") public InventarioDetalleResponse guardar(@PathVariable Long id,@Valid @RequestBody GuardarConteoRequest r){
    return service.guardarDetalle(usuarios.obtenerActual(),id,r);
  }
  @PostMapping("/{id}/finalizar") public InventarioDetalleResponse finalizar(@PathVariable Long id){
    return service.finalizar(usuarios.obtenerActual(),id);
  }
  @PostMapping("/{id}/anular") public InventarioDetalleResponse anular(@PathVariable Long id,@Valid @RequestBody AnularInventarioRequest r){
    return service.anular(usuarios.obtenerActual(),id,r);
  }
  @GetMapping("/{id}") public InventarioDetalleResponse detalle(@PathVariable Long id){
    return service.detalle(usuarios.obtenerActual(),id);
  }
  @GetMapping public Page<InventarioResumenResponse> historial(
      
      @RequestParam(required=false) Long plazaId,@RequestParam(required=false) Long responsableId,
      @RequestParam(required=false) String rol,@RequestParam(required=false) EstadoInventario estado,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate desde,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate hasta,
      @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size){
    return service.historial(usuarios.obtenerActual(),plazaId,responsableId,rol,estado,desde,hasta,page,size);
  }
}
