package com.sigo.asistencia.security.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String tipo,
        long expiresIn,
        UsuarioSesion usuario
) {
    public record UsuarioSesion(
            Long id,
            Integer codigo,
            String nombre,
            String rol,
            Long plazaId,
            String plaza,
            List<String> modulos
    ) {}
}
