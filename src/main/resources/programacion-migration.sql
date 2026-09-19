-- SIGO - Programación / Distribución de Personal
-- Ejecutar en SIGO-TEST antes de probar el backend local.

create table if not exists programacion_grupo (
  id bigint generated always as identity primary key,
  plaza_id bigint not null references plazas(id),
  nombre varchar(80) not null,
  controlador_id bigint not null references trabajadores(id),
  activo boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_programacion_grupo_plaza
  on programacion_grupo(plaza_id);

create table if not exists programacion_grupo_miembro (
  id bigint generated always as identity primary key,
  grupo_id bigint not null references programacion_grupo(id) on delete cascade,
  trabajador_id bigint not null references trabajadores(id),
  constraint uq_programacion_miembro_trabajador unique(trabajador_id)
);

create index if not exists idx_programacion_grupo_miembro_grupo
  on programacion_grupo_miembro(grupo_id);

create table if not exists programacion_dia (
  id bigint generated always as identity primary key,
  trabajador_id bigint not null references trabajadores(id),
  plaza_id bigint not null references plazas(id),
  fecha date not null,
  jornada_codigo varchar(10) not null,
  via_id bigint references vias(id),
  posicion_especial varchar(30),
  actualizado_por_id bigint references trabajadores(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint uq_programacion_trabajador_fecha unique(trabajador_id, fecha),
  constraint chk_programacion_jornada
    check (jornada_codigo in ('A','B','C','D','V','COM','DM','LIC')),
  constraint chk_programacion_posicion_unica
    check (not (via_id is not null and posicion_especial is not null)),
  constraint chk_programacion_no_operativa_sin_caseta
    check (
      jornada_codigo in ('A','B','C')
      or (via_id is null and posicion_especial is null)
    )
);

create index if not exists idx_programacion_dia_plaza_fecha
  on programacion_dia(plaza_id, fecha);

create index if not exists idx_programacion_dia_trabajador_fecha
  on programacion_dia(trabajador_id, fecha);
