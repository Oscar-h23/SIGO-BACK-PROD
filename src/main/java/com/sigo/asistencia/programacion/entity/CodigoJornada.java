package com.sigo.asistencia.programacion.entity;

public enum CodigoJornada {
    A("Turno A", true),
    B("Turno B", true),
    C("Turno C", true),
    D("Descanso", false),
    V("Vacaciones", false),
    COM("Compensación", false),
    DM("Descanso médico", false),
    LIC("Licencia", false);

    private final String descripcion;
    private final boolean operativo;

    CodigoJornada(String descripcion, boolean operativo) {
        this.descripcion = descripcion;
        this.operativo = operativo;
    }

    public String getDescripcion() { return descripcion; }
    public boolean isOperativo() { return operativo; }
}
