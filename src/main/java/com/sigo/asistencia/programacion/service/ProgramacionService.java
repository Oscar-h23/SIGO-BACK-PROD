package com.sigo.asistencia.programacion.service;

import com.sigo.asistencia.personal.entity.*;
import com.sigo.asistencia.personal.repository.*;
import com.sigo.asistencia.programacion.entity.*;
import com.sigo.asistencia.programacion.repository.*;
import com.sigo.asistencia.security.service.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramacionService {
    private final ProgramacionTurnoRepository programacionRepo;
    private final ProgramacionUbicacionRepository ubicacionRepo;
    private final DistribucionPersonalRepository distribucionRepo;
    private final AgenteControladorLiderRepository liderRepo;
    private final TrabajadorRepository trabajadorRepo;
    private final PlazaRepository plazaRepo;
    private final CurrentUserService currentUser;

    public record TurnoItemRequest(@NotNull Long trabajadorId,@NotNull LocalDate fecha,@NotNull EstadoProgramacion estado){}
    public record GuardarProgramacionRequest(@NotNull Long plazaId,@NotEmpty List<@Valid TurnoItemRequest> programaciones){}
    public record ProgramacionDiaResponse(Long programacionId,Long trabajadorId,Integer codigoTrabajador,String nombreTrabajador,Long plazaId,String plazaCodigo,LocalDate fecha,EstadoProgramacion estado){}
    public record UbicacionResponse(Long id,Long plazaId,String codigo,String nombre,TipoUbicacion tipo,Long viaId,Boolean activo,Integer orden){}
    public record DistribucionItemRequest(@NotNull Long programacionTurnoId,@NotNull Long ubicacionId,String observacion){}
    public record GuardarDistribucionRequest(@NotNull Long plazaId,@NotEmpty List<@Valid DistribucionItemRequest> distribuciones){}
    public record DistribucionDiaResponse(Long distribucionId,Long programacionTurnoId,Long trabajadorId,Integer codigoTrabajador,String nombreTrabajador,LocalDate fecha,EstadoProgramacion estado,Long ubicacionId,String ubicacionCodigo,String ubicacionNombre,TipoUbicacion ubicacionTipo,String observacion){}
    public record HorarioDiaResponse(LocalDate fecha,EstadoProgramacion estado,String ubicacionCodigo,String ubicacionNombre){}
    public record MiHorarioResponse(Long trabajadorId,Integer codigo,String nombre,Long plazaId,String plazaCodigo,String lider,List<HorarioDiaResponse> dias){}
    public record GrupoLiderRequest(@NotNull Long agenteId,@NotNull Long controladorId,@NotNull Long plazaId,LocalDate fechaInicio){}
    public record GrupoLiderResponse(Long id,Long agenteId,Integer agenteCodigo,String agenteNombre,Long controladorId,Integer controladorCodigo,String controladorNombre,Long plazaId,String plazaCodigo,LocalDate fechaInicio,LocalDate fechaFin,Boolean activo){}
    public record ResumenUbicacionResponse(String codigo,String nombre,long veces){}
    public record ResumenTrabajadorResponse(Long trabajadorId,Integer codigo,String nombre,List<ResumenUbicacionResponse> ubicaciones){}
    public record CoberturaUbicacionResponse(Long ubicacionId,String codigo,String nombre,Map<LocalDate,Long> porDia){}

    @Transactional(readOnly=true)
    public List<ProgramacionDiaResponse> listarTurnos(Long plazaId,int anio,int mes){
        validarAccesoLecturaPlaza(plazaId);
        YearMonth ym=yearMonth(anio,mes);
        return programacionRepo.findMes(plazaId,ym.atDay(1),ym.atEndOfMonth()).stream().map(this::toTurno).toList();
    }

    @Transactional
    public List<ProgramacionDiaResponse> guardarTurnos(GuardarProgramacionRequest req){
        Trabajador actual=requireSupervisor();
        Plaza plaza=plazaRepo.findById(req.plazaId()).filter(p->Boolean.TRUE.equals(p.getActivo()))
            .orElseThrow(()->bad("Plaza no válida"));
        Set<Long> agentes=trabajadorRepo.findAgentesByPlaza(plaza.getId()).stream().map(Trabajador::getId).collect(Collectors.toSet());
        List<ProgramacionTurno> guardados=new ArrayList<>();
        for(TurnoItemRequest item:req.programaciones()){
            if(!agentes.contains(item.trabajadorId())) throw bad("El trabajador "+item.trabajadorId()+" no es un agente activo de la plaza");
            Trabajador t=trabajadorRepo.findById(item.trabajadorId()).orElseThrow(()->bad("Trabajador no encontrado"));
            ProgramacionTurno p=programacionRepo.findByTrabajadorIdAndFecha(t.getId(),item.fecha()).orElseGet(()->{
                ProgramacionTurno n=new ProgramacionTurno();
                n.setTrabajador(t); n.setPlaza(plaza); n.setFecha(item.fecha()); n.setCreadoPor(actual);
                return n;
            });
            p.setPlaza(plaza);
            p.setEstado(item.estado());
            p.setActualizadoPor(actual);
            guardados.add(programacionRepo.save(p));
        }
        programacionRepo.flush();
        return guardados.stream().map(this::toTurno).toList();
    }

    @Transactional(readOnly=true)
    public List<UbicacionResponse> ubicaciones(Long plazaId){
        validarAccesoGestionPlaza(plazaId);
        return ubicacionRepo.findByPlazaIdAndActivoTrueOrderByOrdenAscCodigoAsc(plazaId).stream().map(this::toUbicacion).toList();
    }

    @Transactional(readOnly=true)
    public List<DistribucionDiaResponse> listarDistribucion(Long plazaId,int anio,int mes){
        validarAccesoGestionPlaza(plazaId);
        YearMonth ym=yearMonth(anio,mes);
        return distribucionRepo.findMes(plazaId,ym.atDay(1),ym.atEndOfMonth()).stream().map(this::toDistribucion).toList();
    }

    @Transactional
    public List<DistribucionDiaResponse> guardarDistribucion(GuardarDistribucionRequest req){
        Trabajador actual=validarAccesoGestionPlaza(req.plazaId());
        List<DistribucionPersonal> guardados=new ArrayList<>();
        for(DistribucionItemRequest item:req.distribuciones()){
            ProgramacionTurno p=programacionRepo.findById(item.programacionTurnoId())
                .orElseThrow(()->bad("Programación no encontrada: "+item.programacionTurnoId()));
            if(!Objects.equals(p.getPlaza().getId(),req.plazaId())) throw bad("La programación no pertenece a la plaza");
            if(!p.getEstado().esOperativo()) throw bad("Solo los estados A, B o C pueden tener caseta");
            ProgramacionUbicacion u=ubicacionRepo.findById(item.ubicacionId())
                .filter(x->Boolean.TRUE.equals(x.getActivo())).orElseThrow(()->bad("Ubicación no válida"));
            if(!Objects.equals(u.getPlaza().getId(),req.plazaId())) throw bad("La ubicación no pertenece a la plaza");

            DistribucionPersonal d=distribucionRepo.findByProgramacionTurnoId(p.getId()).orElseGet(()->{
                DistribucionPersonal n=new DistribucionPersonal();
                n.setProgramacionTurno(p); n.setAsignadoPor(actual);
                return n;
            });
            d.setUbicacion(u);
            d.setObservacion(item.observacion());
            d.setActualizadoPor(actual);
            guardados.add(distribucionRepo.save(d));
        }
        distribucionRepo.flush();
        return guardados.stream().map(this::toDistribucion).toList();
    }

    @Transactional(readOnly=true)
    public ResumenTrabajadorResponse resumen(Long trabajadorId,int anio,int mes){
        Trabajador t=trabajadorRepo.findById(trabajadorId).orElseThrow(()->notFound("Trabajador no encontrado"));
        if(t.getPlaza()==null) throw bad("El trabajador no tiene plaza");
        validarAccesoGestionPlaza(t.getPlaza().getId());
        YearMonth ym=yearMonth(anio,mes);
        List<DistribucionPersonal> datos=distribucionRepo.findByTrabajadorMes(t.getId(),ym.atDay(1),ym.atEndOfMonth());
        Map<Long,Long> count=new LinkedHashMap<>();
        Map<Long,ProgramacionUbicacion> refs=new LinkedHashMap<>();
        for(DistribucionPersonal d:datos){
            Long id=d.getUbicacion().getId();
            refs.putIfAbsent(id,d.getUbicacion());
            count.merge(id,1L,Long::sum);
        }
        List<ResumenUbicacionResponse> u=refs.entrySet().stream()
            .map(e->new ResumenUbicacionResponse(e.getValue().getCodigo(),e.getValue().getNombre(),count.getOrDefault(e.getKey(),0L))).toList();
        return new ResumenTrabajadorResponse(t.getId(),t.getCodigo(),t.getNombreCompleto(),u);
    }

    @Transactional(readOnly=true)
    public List<CoberturaUbicacionResponse> cobertura(Long plazaId,int anio,int mes){
        validarAccesoGestionPlaza(plazaId);
        YearMonth ym=yearMonth(anio,mes);
        List<DistribucionPersonal> datos=distribucionRepo.findMes(plazaId,ym.atDay(1),ym.atEndOfMonth());
        Map<Long,Map<LocalDate,Long>> conteos=new HashMap<>();
        for(DistribucionPersonal d:datos){
            conteos.computeIfAbsent(d.getUbicacion().getId(),k->new TreeMap<>())
                .merge(d.getProgramacionTurno().getFecha(),1L,Long::sum);
        }
        return ubicacionRepo.findByPlazaIdAndActivoTrueOrderByOrdenAscCodigoAsc(plazaId).stream()
            .map(u->new CoberturaUbicacionResponse(u.getId(),u.getCodigo(),u.getNombre(),conteos.getOrDefault(u.getId(),Map.of()))).toList();
    }

    @Transactional(readOnly=true)
    public MiHorarioResponse miHorario(LocalDate desde,LocalDate hasta){
        Trabajador actual=currentUser.requireCurrent();
        if(desde==null||hasta==null||hasta.isBefore(desde)) throw bad("Rango de fechas inválido");
        if(ChronoUnit.DAYS.between(desde,hasta)>31) throw bad("El rango máximo permitido es 32 días");

        Map<LocalDate,ProgramacionTurno> turnos=new HashMap<>();
        programacionRepo.findHorario(actual.getId(),desde,hasta).forEach(p->turnos.put(p.getFecha(),p));
        Map<Long,DistribucionPersonal> dist=new HashMap<>();
        distribucionRepo.findByTrabajadorMes(actual.getId(),desde,hasta).forEach(d->dist.put(d.getProgramacionTurno().getId(),d));

        List<HorarioDiaResponse> dias=new ArrayList<>();
        for(LocalDate f=desde;!f.isAfter(hasta);f=f.plusDays(1)){
            ProgramacionTurno p=turnos.get(f);
            if(p==null){ dias.add(new HorarioDiaResponse(f,null,null,null)); continue; }
            DistribucionPersonal d=dist.get(p.getId());
            dias.add(new HorarioDiaResponse(f,p.getEstado(),d==null?null:d.getUbicacion().getCodigo(),d==null?null:d.getUbicacion().getNombre()));
        }
        String lider=liderRepo.findByAgenteIdAndActivoTrue(actual.getId()).map(x->x.getControlador().getNombreCompleto()).orElse(null);
        return new MiHorarioResponse(actual.getId(),actual.getCodigo(),actual.getNombreCompleto(),
            actual.getPlaza()==null?null:actual.getPlaza().getId(),actual.getPlaza()==null?null:actual.getPlaza().getCodigo(),lider,dias);
    }

    @Transactional(readOnly=true)
    public List<GrupoLiderResponse> listarLideres(Long plazaId){
        requireSupervisor();
        return liderRepo.findByPlazaIdAndActivoTrueOrderByAgenteNombreCompletoAsc(plazaId).stream().map(this::toLider).toList();
    }

    @Transactional
    public GrupoLiderResponse asignarLider(GrupoLiderRequest req){
        Trabajador actual=requireSupervisor();
        Plaza plaza=plazaRepo.findById(req.plazaId()).filter(p->Boolean.TRUE.equals(p.getActivo())).orElseThrow(()->bad("Plaza no válida"));
        Trabajador agente=trabajadorRepo.findById(req.agenteId()).filter(t->Boolean.TRUE.equals(t.getActivo())).orElseThrow(()->bad("Agente no encontrado"));
        Trabajador controlador=trabajadorRepo.findById(req.controladorId()).filter(t->Boolean.TRUE.equals(t.getActivo())).orElseThrow(()->bad("Controlador no encontrado"));
        Set<Long> agentes=trabajadorRepo.findAgentesByPlaza(plaza.getId()).stream().map(Trabajador::getId).collect(Collectors.toSet());
        if(!agentes.contains(agente.getId())) throw bad("El trabajador seleccionado no es un agente activo de la plaza");
        if(controlador.getRolSistema()!=RolSistema.CONTROLADOR) throw bad("El líder debe tener rol CONTROLADOR");
        if(controlador.getPlaza()==null||!Objects.equals(controlador.getPlaza().getId(),plaza.getId())) throw bad("El controlador no pertenece a la plaza");

        LocalDate inicio=req.fechaInicio()==null?LocalDate.now():req.fechaInicio();
        liderRepo.findByAgenteIdAndActivoTrue(agente.getId()).ifPresent(anterior->{
            anterior.setActivo(false);
            LocalDate fin=inicio.minusDays(1);
            if(fin.isBefore(anterior.getFechaInicio())) fin=anterior.getFechaInicio();
            anterior.setFechaFin(fin);
            liderRepo.save(anterior);
        });

        AgenteControladorLider n=new AgenteControladorLider();
        n.setAgente(agente); n.setControlador(controlador); n.setPlaza(plaza); n.setFechaInicio(inicio); n.setActivo(true); n.setAsignadoPor(actual);
        return toLider(liderRepo.save(n));
    }

    private Trabajador validarAccesoLecturaPlaza(Long plazaId){
        Trabajador actual=currentUser.requireCurrent();
        if(actual.getRolSistema()==RolSistema.SUPERVISOR) return actual;
        if(actual.getRolSistema()!=RolSistema.CONTROLADOR) throw forbidden("No tienes acceso a la programación mensual");
        if(actual.getPlaza()==null||!Objects.equals(actual.getPlaza().getId(),plazaId)) throw forbidden("Solo puedes consultar tu propia plaza");
        return actual;
    }

    private Trabajador validarAccesoGestionPlaza(Long plazaId){
        Trabajador actual=currentUser.requireCurrent();
        if(actual.getRolSistema()==RolSistema.SUPERVISOR) return actual;
        if(actual.getRolSistema()!=RolSistema.CONTROLADOR) throw forbidden("Solo Supervisor o Controlador puede gestionar la distribución");
        if(actual.getPlaza()==null||!Objects.equals(actual.getPlaza().getId(),plazaId)) throw forbidden("El controlador solo puede gestionar su propia plaza");
        return actual;
    }

    private Trabajador requireSupervisor(){
        Trabajador t=currentUser.requireCurrent();
        if(t.getRolSistema()!=RolSistema.SUPERVISOR) throw forbidden("Solo el Supervisor puede realizar esta operación");
        return t;
    }

    private ProgramacionDiaResponse toTurno(ProgramacionTurno p){
        return new ProgramacionDiaResponse(p.getId(),p.getTrabajador().getId(),p.getTrabajador().getCodigo(),p.getTrabajador().getNombreCompleto(),p.getPlaza().getId(),p.getPlaza().getCodigo(),p.getFecha(),p.getEstado());
    }
    private UbicacionResponse toUbicacion(ProgramacionUbicacion u){
        return new UbicacionResponse(u.getId(),u.getPlaza().getId(),u.getCodigo(),u.getNombre(),u.getTipo(),u.getVia()==null?null:u.getVia().getId(),u.getActivo(),u.getOrden());
    }
    private DistribucionDiaResponse toDistribucion(DistribucionPersonal d){
        ProgramacionTurno p=d.getProgramacionTurno(); ProgramacionUbicacion u=d.getUbicacion();
        return new DistribucionDiaResponse(d.getId(),p.getId(),p.getTrabajador().getId(),p.getTrabajador().getCodigo(),p.getTrabajador().getNombreCompleto(),p.getFecha(),p.getEstado(),u.getId(),u.getCodigo(),u.getNombre(),u.getTipo(),d.getObservacion());
    }
    private GrupoLiderResponse toLider(AgenteControladorLider x){
        return new GrupoLiderResponse(x.getId(),x.getAgente().getId(),x.getAgente().getCodigo(),x.getAgente().getNombreCompleto(),x.getControlador().getId(),x.getControlador().getCodigo(),x.getControlador().getNombreCompleto(),x.getPlaza().getId(),x.getPlaza().getCodigo(),x.getFechaInicio(),x.getFechaFin(),x.getActivo());
    }
    private YearMonth yearMonth(int a,int m){ try{return YearMonth.of(a,m);}catch(Exception e){throw bad("Año o mes inválido");} }
    private ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    private ResponseStatusException notFound(String m){return new ResponseStatusException(HttpStatus.NOT_FOUND,m);}
    private ResponseStatusException forbidden(String m){return new ResponseStatusException(HttpStatus.FORBIDDEN,m);}
}
