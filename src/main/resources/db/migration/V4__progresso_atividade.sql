create table progresso_atividade (
    id uuid primary key default gen_random_uuid(),
    -- cascade porque o seed de dev e os testes apagam usuario antes de reinserir
    usuario_id uuid not null references usuario (id) on delete cascade,
    atividade_id uuid not null references atividade (id),
    video_assistido_em timestamptz,
    concluido_em timestamptz,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create unique index progresso_atividade_usuario_atividade_idx
    on progresso_atividade (usuario_id, atividade_id);

create index progresso_atividade_usuario_idx on progresso_atividade (usuario_id);
