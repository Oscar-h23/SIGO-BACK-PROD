package com.sigo.asistencia.inventario.controller;
import com.sigo.asistencia.inventario.dto.response.StockActualResponse;
import com.sigo.asistencia.inventario.security.InventarioUsuarioContextService;
import com.sigo.asistencia.inventario.service.InventarioStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/inventario/stock") @RequiredArgsConstructor
public class InventarioStockController {
  private final InventarioStockService service; private final InventarioUsuarioContextService usuarios;
  @GetMapping public List<StockActualResponse> consultar(@RequestParam(required=false) Long plazaId,@RequestParam(required=false) String buscar){
    return service.consultar(usuarios.obtenerActual(),plazaId,buscar);
  }
}
