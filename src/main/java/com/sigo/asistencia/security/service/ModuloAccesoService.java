package com.sigo.asistencia.security.service;

import com.sigo.asistencia.personal.entity.RolSistema;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuloAccesoService {

    public List<String> modulosPara(RolSistema rol) {
        return switch (rol) {
            case SUPERVISOR -> List.of(
                    "DASHBOARD", "RELEVOS", "ASISTENCIA", "INVENTARIO",
                    "ADMIN_PRODUCTOS", "TRABAJADORES", "CHAT"
            );
            case CONTROLADOR -> List.of(
                    "DASHBOARD", "RELEVOS", "ASISTENCIA", "INVENTARIO",
                    "ADMIN_PRODUCTOS", "CHAT"
            );
            case OPERADOR -> List.of(
                    "RELEVOS", "INVENTARIO"
            );
        };
    }
}
