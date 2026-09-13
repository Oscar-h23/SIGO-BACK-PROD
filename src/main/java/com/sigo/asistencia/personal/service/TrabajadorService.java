package com.sigo.asistencia.personal.service;

import com.sigo.asistencia.personal.entity.Trabajador;
import com.sigo.asistencia.personal.repository.TrabajadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;

    public TrabajadorService(
            TrabajadorRepository trabajadorRepository
    ) {
        this.trabajadorRepository = trabajadorRepository;
    }

    @Transactional(readOnly = true)
    public List<Trabajador> listarAgentesPorPlaza(
            Long plazaId
    ) {
        return trabajadorRepository
                .findAgentesByPlaza(plazaId);
    }

    @Transactional(readOnly = true)
    public List<Trabajador> listarControladoresPorPlaza(
            Long plazaId
    ) {
        return trabajadorRepository
                .findControladoresByPlaza(plazaId);
    }
}