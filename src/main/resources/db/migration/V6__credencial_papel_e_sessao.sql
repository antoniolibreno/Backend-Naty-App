-- default nas colunas novas porque o seed repetivel roda depois das versionadas
alter table usuario
    add column senha_hash text,
    add column papel varchar(20) not null default 'INTEGRANTE',
    add column ativo boolean not null default true;

create table sessao (
    id uuid primary key default gen_random_uuid(),
    -- cascade porque o seed de dev e os testes apagam usuario antes de reinserir
    usuario_id uuid not null references usuario (id) on delete cascade,
    token_hash text not null,
    criado_em timestamptz not null default now(),
    expira_em timestamptz not null,
    ultimo_acesso_em timestamptz,
    revogado_em timestamptz
);

create unique index sessao_token_hash_idx on sessao (token_hash);

create index sessao_usuario_idx on sessao (usuario_id);

drop index usuario_empresa_email_idx;

create unique index usuario_email_idx on usuario (lower(email));
