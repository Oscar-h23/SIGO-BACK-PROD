package com.sigo.asistencia.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String message) {}
