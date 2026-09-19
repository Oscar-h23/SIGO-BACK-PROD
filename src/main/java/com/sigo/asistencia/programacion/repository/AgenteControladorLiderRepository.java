package com.sigo.asistencia.programacion.repository;

import com.sigo.asistencia.programacion.entity.AgenteControladorLider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AgenteControladorLiderRepository extends JpaRepository<AgenteControladorLider,Long> {
    Optional<AgenteControladorLider> findByAgenteIdAndActivoTrue(Long agenteId);
    List<AgenteControladorLider> findByPlazaIdAndActivoTrueOrderByAgenteNombreCompletoAsc(Long plazaId);
}
