create table tentativa_quiz (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references usuario (id) on delete cascade,
    quiz_id uuid not null references quiz (id),
    nota integer not null check (nota between 0 and 100),
    aprovado boolean not null,
    criada_em timestamptz not null default now()
);

create index tentativa_quiz_usuario_quiz_idx on tentativa_quiz (usuario_id, quiz_id, criada_em desc);

create table resposta_tentativa (
    id uuid primary key default gen_random_uuid(),
    tentativa_id uuid not null references tentativa_quiz (id) on delete cascade,
    pergunta_id uuid not null references pergunta (id),
    alternativa_id uuid not null references alternativa (id),
    correta boolean not null,
    constraint resposta_tentativa_pergunta_idx unique (tentativa_id, pergunta_id)
);

create index resposta_tentativa_pergunta_idx on resposta_tentativa (pergunta_id);


alter table progresso_atividade add column melhor_nota integer check (melhor_nota between 0 and 100);
