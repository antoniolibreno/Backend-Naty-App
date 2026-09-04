create table usuario (
    id uuid primary key default gen_random_uuid(),
    naty_id varchar(100) not null,
    nome varchar(255) not null,
    email varchar(255),
    payload jsonb not null default '{}'::jsonb,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create unique index usuario_naty_id_idx on usuario (naty_id);

create index usuario_email_idx on usuario (email);
