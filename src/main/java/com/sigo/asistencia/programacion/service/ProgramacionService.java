package com.sigo.asistencia.programacion.service;

import com.sigo.asistencia.personal.entity.RolSistema;
import com.sigo.asistencia.personal.entity.Trabajador;
import com.sigo.asistencia.personal.repository.PlazaRepository;
import com.sigo.asistencia.personal.repository.TrabajadorRepository;
import com.sigo.asistencia.programacion.dto.*;
import com.sigo.asistencia.programacion.entity.*;
import com.sigo.asistencia.programacion.repository.*;
import com.sigo.asistencia.relevo.entity.Via;
import com.sigo.asistencia.relevo.repository.ViaRepository;
import com.sigo.asistencia.security.service.CurrentUserService;
import com.sigo.asistencia.shared.exception.BusinessException;
import com.sigo.asistencia.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramacionService {

    private final ProgramacionDiaRepository programacionRepository;
    private final GrupoTrabajoRepository grupoRepository;
    private final GrupoTrabajoMiembroRepository miembroRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final PlazaRepository plazaRepository;
    private final ViaRepository viaRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<ProgramacionDiaResponse> miSemana(LocalDate inicio) {
        Trabajador actual = currentUserService.requireCurrent();
        LocalDate lunes = inicio == null
                ? LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                : inicio.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = lunes.plusDays(6);

        Map<LocalDate, ProgramacionDia> porFecha = programacionRepository
                .findByTrabajadorIdAndFechaBetweenOrderByFechaAsc(actual.getId(), lunes, domingo)
                .stream().collect(Collectors.toMap(ProgramacionDia::getFecha, x -> x));

        LiderInfo lider = liderDe(actual.getId());
        List<ProgramacionDiaResponse> out = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = lunes.plusDays(i);
            ProgramacionDia p = porFecha.get(fecha);
            out.add(p == null ? vacio(actual, fecha, lider) : toResponse(p, lider));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<ProgramacionDiaResponse> mesPlaza(Long plazaId, int anio, int mes) {
        Trabajador actual = currentUserService.requireCurrent();
        exigirPuedeVerPlaza(actual, plazaId);
        YearMonth ym = YearMonth.of(anio, mes);
        return programacionRepository
                .findByPlazaIdAndFechaBetweenOrderByTrabajadorNombreCompletoAscFechaAsc(
                        plazaId, ym.atDay(1), ym.atEndOfMonth())
                .stream()
                .map(p -> toResponse(p, liderDe(p.getTrabajador().getId())))
                .toList();
    }

    @Transactional
    public List<ProgramacionDiaResponse> guardarJornadas(List<JornadaItemRequest> items) {
        Trabajador supervisor = currentUserService.requireCurrent();
        exigirSupervisor(supervisor);
        if (items == null || items.isEmpty()) throw new BusinessException("Debe enviar al menos una jornada");

        List<ProgramacionDiaResponse> out = new ArrayList<>();
        for (JornadaItemRequest item : items) {
            Trabajador agente = agente(item.trabajadorId());
            if (agente.getPlaza() == null) throw new BusinessException("El trabajador no tiene plaza asignada");

            ProgramacionDia p = programacionRepository
                    .findByTrabajadorIdAndFecha(agente.getId(), item.fecha())
                    .orElseGet(ProgramacionDia::new);
            p.setTrabajador(agente);
            p.setPlaza(agente.getPlaza());
            p.setFecha(item.fecha());
            p.setJornadaCodigo(item.codigo());
            p.setActualizadoPor(supervisor);

            if (!item.codigo().isOperativo()) {
                p.setVia(null);
                p.setPosicionEspecial(null);
            }

            p = programacionRepository.save(p);
            out.add(toResponse(p, liderDe(agente.getId())));
        }
        return out;
    }

    @Transactional
    public List<ProgramacionDiaResponse> guardarAsignaciones(List<AsignacionItemRequest> items) {
        Trabajador controlador = currentUserService.requireCurrent();
        exigirControlador(controlador);
        if (controlador.getPlaza() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El controlador no tiene plaza asignada");
        }
        if (items == null || items.isEmpty()) throw new BusinessException("Debe enviar al menos una asignación");

        List<ProgramacionDiaResponse> out = new ArrayList<>();
        for (AsignacionItemRequest item : items) {
            Trabajador agente = agente(item.trabajadorId());
            if (agente.getPlaza() == null || !agente.getPlaza().getId().equals(controlador.getPlaza().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo puede asignar casetas a agentes de su propia plaza");
            }

            ProgramacionDia p = programacionRepository
                    .findByTrabajadorIdAndFecha(agente.getId(), item.fecha())
                    .orElseThrow(() -> new BusinessException(
                            "El supervisor aún no programó turno/estado para " + agente.getNombreCompleto()
                                    + " el " + item.fecha()));

            if (!p.getJornadaCodigo().isOperativo()) {
                throw new BusinessException("No se puede asignar caseta cuando el estado es " + p.getJornadaCodigo());
            }

            String especial = normalizarPosicion(item.posicionEspecial());
            if (item.viaId() != null && especial != null) {
                throw new BusinessException("Use una vía o una posición especial, no ambas");
            }

            Via via = null;
            if (item.viaId() != null) {
                via = viaRepository.findById(item.viaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Vía no encontrada"));
                if (!via.getPlaza().getId().equals(controlador.getPlaza().getId()) || !Boolean.TRUE.equals(via.getActiva())) {
                    throw new BusinessException("La vía no pertenece a la plaza del controlador o está inactiva");
                }
            }

            p.setVia(via);
            p.setPosicionEspecial(especial);
            p.setActualizadoPor(controlador);
            p = programacionRepository.save(p);
            out.add(toResponse(p, liderDe(agente.getId())));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<CoberturaResponse> cobertura(Long plazaId, int anio, int mes) {
        Trabajador actual = currentUserService.requireCurrent();
        exigirPuedeVerPlaza(actual, plazaId);
        YearMonth ym = YearMonth.of(anio, mes);
        Map<String, Long> agrupado = programacionRepository
                .findByPlazaIdAndFechaBetweenOrderByTrabajadorNombreCompletoAscFechaAsc(
                        plazaId, ym.atDay(1), ym.atEndOfMonth())
                .stream()
                .filter(p -> p.getJornadaCodigo().isOperativo())
                .map(p -> Map.entry(p.getFecha(), posicion(p)))
                .filter(e -> e.getValue() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getKey() + "|" + e.getValue(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        return agrupado.entrySet().stream().map(e -> {
            String[] parts = e.getKey().split("\\|", 2);
            return new CoberturaResponse(LocalDate.parse(parts[0]), parts[1], e.getValue());
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<GrupoTrabajoResponse> grupos(Long plazaId) {
        Trabajador actual = currentUserService.requireCurrent();
        exigirPuedeVerPlaza(actual, plazaId);
        return grupoRepository.findByPlazaIdAndActivoTrueOrderByNombreAsc(plazaId)
                .stream().map(this::toGrupoResponse).toList();
    }

    @Transactional
    public GrupoTrabajoResponse crearGrupo(GrupoTrabajoRequest request) {
        Trabajador supervisor = currentUserService.requireCurrent();
        exigirSupervisor(supervisor);
        var plaza = plazaRepository.findById(request.plazaId())
                .orElseThrow(() -> new ResourceNotFoundException("Plaza no encontrada"));
        Trabajador lider = controladorEnPlaza(request.controladorId(), plaza.getId());

        GrupoTrabajo grupo = new GrupoTrabajo();
        grupo.setPlaza(plaza);
        grupo.setNombre(request.nombre().trim());
        grupo.setControlador(lider);
        grupo.setActivo(true);
        grupo = grupoRepository.save(grupo);
        reemplazarMiembros(grupo, request.miembroIds());
        return toGrupoResponse(grupo);
    }

    @Transactional
    public GrupoTrabajoResponse actualizarGrupo(Long grupoId, GrupoTrabajoRequest request) {
        Trabajador supervisor = currentUserService.requireCurrent();
        exigirSupervisor(supervisor);
        GrupoTrabajo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));
        var plaza = plazaRepository.findById(request.plazaId())
                .orElseThrow(() -> new ResourceNotFoundException("Plaza no encontrada"));
        grupo.setPlaza(plaza);
        grupo.setNombre(request.nombre().trim());
        grupo.setControlador(controladorEnPlaza(request.controladorId(), plaza.getId()));
        grupo.setActivo(true);
        grupo = grupoRepository.save(grupo);
        reemplazarMiembros(grupo, request.miembroIds());
        return toGrupoResponse(grupo);
    }

    @Transactional
    public void desactivarGrupo(Long grupoId) {
        Trabajador supervisor = currentUserService.requireCurrent();
        exigirSupervisor(supervisor);
        GrupoTrabajo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado"));
        miembroRepository.deleteByGrupoId(grupoId);
        grupo.setActivo(false);
        grupoRepository.save(grupo);
    }

    private void reemplazarMiembros(GrupoTrabajo grupo, List<Long> ids) {
        List<Long> distintos = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (distintos.isEmpty()) throw new BusinessException("El grupo debe tener al menos un agente");

        List<Trabajador> agentes = distintos.stream().map(this::agente).toList();
        for (Trabajador a : agentes) {
            if (a.getPlaza() == null || !a.getPlaza().getId().equals(grupo.getPlaza().getId())) {
                throw new BusinessException("Todos los agentes deben pertenecer a " + grupo.getPlaza().getCodigo());
            }
        }

        miembroRepository.deleteByGrupoId(grupo.getId());
        miembroRepository.deleteByTrabajadorIdIn(distintos);
        for (Trabajador agente : agentes) {
            GrupoTrabajoMiembro m = new GrupoTrabajoMiembro();
            m.setGrupo(grupo);
            m.setTrabajador(agente);
            miembroRepository.save(m);
        }
    }

    private Trabajador agente(Long id) {
        Trabajador t = trabajadorRepository.findById(id)
                .filter(x -> Boolean.TRUE.equals(x.getActivo()))
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador no encontrado"));
        String puesto = t.getPuesto() == null ? "" : t.getPuesto().getNombre().toLowerCase(Locale.ROOT);
        if (!puesto.contains("agente de recaud")) {
            throw new BusinessException(t.getNombreCompleto() + " no es agente de recaudación");
        }
        return t;
    }

    private Trabajador controladorEnPlaza(Long id, Long plazaId) {
        Trabajador t = trabajadorRepository.findById(id)
                .filter(x -> Boolean.TRUE.equals(x.getActivo()))
                .orElseThrow(() -> new ResourceNotFoundException("Controlador no encontrado"));
        if (t.getRolSistema() != RolSistema.CONTROLADOR
                || t.getPlaza() == null
                || !t.getPlaza().getId().equals(plazaId)) {
            throw new BusinessException("El líder debe ser un controlador activo de la misma plaza");
        }
        return t;
    }

    private void exigirPuedeVerPlaza(Trabajador actual, Long plazaId) {
        if (actual.getRolSistema() == RolSistema.SUPERVISOR) return;
        if (actual.getRolSistema() == RolSistema.CONTROLADOR
                && actual.getPlaza() != null
                && actual.getPlaza().getId().equals(plazaId)) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene acceso a la programación de esa plaza");
    }

    private void exigirSupervisor(Trabajador t) {
        if (t.getRolSistema() != RolSistema.SUPERVISOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el supervisor puede programar turnos y grupos");
        }
    }

    private void exigirControlador(Trabajador t) {
        if (t.getRolSistema() != RolSistema.CONTROLADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los controladores pueden asignar casetas");
        }
    }

    private String normalizarPosicion(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String v = valor.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
        Set<String> permitidas = Set.of("AUX 1", "AUX 2", "AUX 3", "APPMOVIL", "APP MOVIL");
        if (!permitidas.contains(v)) {
            throw new BusinessException("Posición especial no válida. Use AUX 1, AUX 2, AUX 3 o APPMOVIL");
        }
        return v.equals("APP MOVIL") ? "APPMOVIL" : v;
    }

    private String posicion(ProgramacionDia p) {
        if (p.getVia() != null) return String.valueOf(p.getVia().getNumero());
        return p.getPosicionEspecial();
    }

    private LiderInfo liderDe(Long trabajadorId) {
        return miembroRepository.findByTrabajadorId(trabajadorId)
                .map(m -> {
                    Trabajador c = m.getGrupo().getControlador();
                    return new LiderInfo(c.getId(), c.getCodigo(), c.getNombreCompleto());
                })
                .orElse(new LiderInfo(null, null, null));
    }

    private ProgramacionDiaResponse vacio(Trabajador t, LocalDate fecha, LiderInfo lider) {
        return new ProgramacionDiaResponse(
                null, fecha, null, "Sin programación",
                t.getId(), t.getCodigo(), t.getNombreCompleto(),
                t.getPlaza() == null ? null : t.getPlaza().getId(),
                t.getPlaza() == null ? null : t.getPlaza().getCodigo(),
                null, null, null, null,
                lider.id, lider.codigo, lider.nombre
        );
    }

    private ProgramacionDiaResponse toResponse(ProgramacionDia p, LiderInfo lider) {
        return new ProgramacionDiaResponse(
                p.getId(), p.getFecha(), p.getJornadaCodigo().name(), p.getJornadaCodigo().getDescripcion(),
                p.getTrabajador().getId(), p.getTrabajador().getCodigo(), p.getTrabajador().getNombreCompleto(),
                p.getPlaza().getId(), p.getPlaza().getCodigo(),
                p.getVia() == null ? null : p.getVia().getId(),
                p.getVia() == null ? null : p.getVia().getNumero(),
                p.getVia() == null ? null : p.getVia().getNombre(),
                p.getPosicionEspecial(),
                lider.id, lider.codigo, lider.nombre
        );
    }

    private GrupoTrabajoResponse toGrupoResponse(GrupoTrabajo g) {
        List<GrupoTrabajoResponse.MiembroResponse> miembros = miembroRepository
                .findByGrupoIdOrderByTrabajadorNombreCompletoAsc(g.getId())
                .stream()
                .map(m -> new GrupoTrabajoResponse.MiembroResponse(
                        m.getTrabajador().getId(), m.getTrabajador().getCodigo(), m.getTrabajador().getNombreCompleto()))
                .toList();
        return new GrupoTrabajoResponse(
                g.getId(), g.getPlaza().getId(), g.getPlaza().getCodigo(), g.getNombre(),
                g.getControlador().getId(), g.getControlador().getCodigo(), g.getControlador().getNombreCompleto(),
                miembros
        );
    }

    private record LiderInfo(Long id, Integer codigo, String nombre) {}
}
