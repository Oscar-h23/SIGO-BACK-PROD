package com.sigo.asistencia.chat.service;

import com.sigo.asistencia.chat.dto.SigoChatPlan;
import com.sigo.asistencia.chat.dto.SigoToolRequest;
import com.sigo.asistencia.chat.dto.SigoToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final GeminiService geminiService;
    private final SigoReadOnlyQueryService queryService;
    private final ChatResponseFormatter formatter;

    public String procesar(String mensaje) {
        try {
            SigoChatPlan plan = geminiService.interpretar(mensaje);
            List<SigoToolRequest> herramientas = plan.toolsSeguras();

            if (herramientas.isEmpty()) {
                return "No pude determinar qué información de SIGO consultar.";
            }

            List<SigoToolResult> resultados = new ArrayList<>();
            for (SigoToolRequest herramienta : herramientas) {
                resultados.add(queryService.ejecutar(herramienta));
            }

            return formatter.formatear(resultados);
        } catch (Exception e) {
            System.err.println("Error en Asistente SIGO: " + e.getMessage());
            e.printStackTrace();

            if (e.getMessage() != null && e.getMessage().contains("temporalmente")) {
                return e.getMessage();
            }

            return "No pude procesar la consulta en este momento. Intenta nuevamente.";
        }
    }
}
