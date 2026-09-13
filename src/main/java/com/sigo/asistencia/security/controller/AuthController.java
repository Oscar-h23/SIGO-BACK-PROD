package com.sigo.asistencia.security.controller;

import com.sigo.asistencia.security.dto.LoginRequest;
import com.sigo.asistencia.security.dto.LoginResponse;
import com.sigo.asistencia.security.dto.MeResponse;
import com.sigo.asistencia.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MeResponse me() {
        return authService.me();
    }
}
