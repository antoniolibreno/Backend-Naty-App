create table empresa (
    id uuid primary key default gen_random_uuid(),
    nome varchar(255) not null,
    naty_api_token text,
    ativa boolean not null default true,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

drop index usuario_naty_id_idx;

drop index usuario_email_idx;

-- empresa_id nasce not null sem default porque usuario esta vazia em todo ambiente
alter table usuario
    add column empresa_id uuid not null references empresa (id),
    add column perfil varchar(20) not null default 'user',
    add column status varchar(20) not null default 'offline',
    add column ultimo_acesso_naty timestamptz,
    add column sincronizado_em timestamptz;

create unique index usuario_empresa_naty_id_idx on usuario (empresa_id, naty_id);

create unique index usuario_empresa_email_idx on usuario (empresa_id, lower(email));

create table trilha (
    id uuid primary key default gen_random_uuid(),
    titulo varchar(255) not null,
    descricao text,
    ordem integer not null,
    ativa boolean not null default true,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create index trilha_ordem_idx on trilha (ordem);

create table modulo (
    id uuid primary key default gen_random_uuid(),
    trilha_id uuid not null references trilha (id),
    titulo varchar(255) not null,
    descricao text,
    ordem integer not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create unique index modulo_trilha_ordem_idx on modulo (trilha_id, ordem);

create table atividade (
    id uuid primary key default gen_random_uuid(),
    modulo_id uuid not null references modulo (id),
    titulo varchar(255) not null,
    descricao text,
    ordem integer not null,
    imagem_url varchar(500),
    video_url varchar(500),
    duracao_segundos integer,
    xp integer not null default 10,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create unique index atividade_modulo_ordem_idx on atividade (modulo_id, ordem);

create table quiz (
    id uuid primary key default gen_random_uuid(),
    atividade_id uuid not null unique references atividade (id),
    nota_minima integer not null default 70,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create table pergunta (
    id uuid primary key default gen_random_uuid(),
    quiz_id uuid not null references quiz (id),
    enunciado text not null,
    ordem integer not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create unique index pergunta_quiz_ordem_idx on pergunta (quiz_id, ordem);

create table alternativa (
    id uuid primary key default gen_random_uuid(),
    pergunta_id uuid not null references pergunta (id),
    texto text not null,
    correta boolean not null default false,
    ordem integer not null,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create unique index alternativa_pergunta_ordem_idx on alternativa (pergunta_id, ordem);
