create table auditoria_conteudo (
    id uuid primary key default gen_random_uuid(),
    tipo_recurso varchar(30) not null,
    recurso_id uuid not null,
    operacao varchar(20) not null,
    autor_id uuid not null references usuario (id) on delete cascade,
    criado_em timestamptz not null default now()
);

create index auditoria_conteudo_recurso_idx on auditoria_conteudo (tipo_recurso, recurso_id, criado_em desc);
