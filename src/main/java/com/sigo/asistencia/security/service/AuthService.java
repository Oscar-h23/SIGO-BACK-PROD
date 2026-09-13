package com.sigo.asistencia.security.service;

import com.sigo.asistencia.personal.entity.Trabajador;
import com.sigo.asistencia.personal.repository.TrabajadorRepository;
import com.sigo.asistencia.security.dto.LoginRequest;
import com.sigo.asistencia.security.dto.LoginResponse;
import com.sigo.asistencia.security.dto.MeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TrabajadorRepository trabajadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final ModuloAccesoService moduloAccesoService;

    public LoginResponse login(LoginRequest request) {
        Trabajador t = trabajadorRepository.findByCodigo(request.codigo())
                .filter(x -> Boolean.TRUE.equals(x.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (t.getPasswordHash() == null || !passwordEncoder.matches(request.password(), t.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generar(t);
        return new LoginResponse(token, "Bearer", jwtService.expirationSeconds(), toSesion(t));
    }

    public MeResponse me() {
        Trabajador t = currentUserService.requireCurrent();
        return new MeResponse(
                t.getId(), t.getId(), t.getCodigo(), t.getNombreCompleto(), t.getRolSistema().name(),
                t.getPlaza() == null ? null : t.getPlaza().getId(),
                t.getPlaza() == null ? null : t.getPlaza().getCodigo(),
                moduloAccesoService.modulosPara(t.getRolSistema())
        );
    }

    private LoginResponse.UsuarioSesion toSesion(Trabajador t) {
        return new LoginResponse.UsuarioSesion(
                t.getId(), t.getId(), t.getCodigo(), t.getNombreCompleto(), t.getRolSistema().name(),
                t.getPlaza() == null ? null : t.getPlaza().getId(),
                t.getPlaza() == null ? null : t.getPlaza().getCodigo(),
                moduloAccesoService.modulosPara(t.getRolSistema())
        );
    }
}
