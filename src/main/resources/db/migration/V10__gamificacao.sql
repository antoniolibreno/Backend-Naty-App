create table pontuacao_integrante (
    usuario_id uuid primary key references usuario (id) on delete cascade,
    empresa_id uuid not null references empresa (id),
    xp_total integer not null default 0 check (xp_total >= 0),
    pontuacao_atingida_em timestamptz,
    atualizado_em timestamptz not null default now()
);

create index pontuacao_integrante_empresa_xp_idx
    on pontuacao_integrante (empresa_id, xp_total desc, pontuacao_atingida_em);

create table xp_atividade (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references usuario (id) on delete cascade,
    atividade_id uuid not null references atividade (id),
    empresa_id uuid not null references empresa (id),
    xp integer not null check (xp > 0),
    concedido_em timestamptz not null default now()
);

create unique index xp_atividade_usuario_atividade_idx
    on xp_atividade (usuario_id, atividade_id);

create index xp_atividade_empresa_idx on xp_atividade (empresa_id);

create table sequencia_integrante (
    usuario_id uuid primary key references usuario (id) on delete cascade,
    empresa_id uuid not null references empresa (id),
    dias_atuais integer not null default 0 check (dias_atuais >= 0),
    maior_sequencia integer not null default 0 check (maior_sequencia >= 0),
    ultimo_evento_em timestamptz,
    ultimo_dia_local date
);

create table conquista (
    id uuid primary key default gen_random_uuid(),
    codigo varchar(80) not null unique,
    titulo varchar(255) not null,
    descricao text not null,
    tipo varchar(40) not null,
    valor_alvo integer,
    ativa boolean not null default true
);

create table conquista_usuario (
    usuario_id uuid not null references usuario (id) on delete cascade,
    conquista_id uuid not null references conquista (id),
    concedida_em timestamptz not null default now(),
    primary key (usuario_id, conquista_id)
);

create index conquista_usuario_usuario_idx on conquista_usuario (usuario_id, concedida_em);

insert into conquista (id, codigo, titulo, descricao, tipo, valor_alvo) values
    ('00000000-0000-0000-0006-000000000001', 'PRIMEIRA_ATIVIDADE', 'Primeiro passo', 'Conclua sua primeira atividade.', 'PROGRESSO', 1),
    ('00000000-0000-0000-0006-000000000002', 'PRIMEIRA_TRILHA_CONCLUIDA', 'Trilha concluida', 'Conclua uma trilha completa.', 'PROGRESSO', 1),
    ('00000000-0000-0000-0006-000000000003', 'QUIZ_PERFEITO', 'Acerto perfeito', 'Acerte 100% de um quiz.', 'QUIZ', 100),
    ('00000000-0000-0000-0006-000000000004', 'TRES_QUIZZES_APROVADOS', 'Mente afiada', 'Aprove tres quizzes.', 'QUIZ', 3),
    ('00000000-0000-0000-0006-000000000005', 'SEQUENCIA_3_DIAS', 'Constancia', 'Mantenha uma sequencia de tres dias.', 'SEQUENCIA', 3),
    ('00000000-0000-0000-0006-000000000006', 'SEQUENCIA_7_DIAS', 'Uma semana de foco', 'Mantenha uma sequencia de sete dias.', 'SEQUENCIA', 7)
on conflict (codigo) do update set
    titulo = excluded.titulo,
    descricao = excluded.descricao,
    tipo = excluded.tipo,
    valor_alvo = excluded.valor_alvo,
    ativa = excluded.ativa;
