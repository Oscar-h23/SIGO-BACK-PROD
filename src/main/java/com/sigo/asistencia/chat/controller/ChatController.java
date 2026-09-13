package com.sigo.asistencia.chat.controller;

import com.sigo.asistencia.chat.dto.ChatRequest;
import com.sigo.asistencia.chat.dto.ChatResponse;
import com.sigo.asistencia.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(new ChatResponse(chatService.procesar(request.message())));
    }
}
