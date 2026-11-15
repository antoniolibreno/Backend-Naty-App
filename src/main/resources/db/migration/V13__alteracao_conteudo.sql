create table alteracao_conteudo (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references usuario (id) on delete cascade,
    entidade varchar(20) not null,
    entidade_id uuid not null,
    acao varchar(20) not null,
    ocorrida_em timestamptz not null default now(),
    constraint alteracao_conteudo_entidade_check
        check (entidade in ('TRILHA', 'MODULO', 'ATIVIDADE', 'QUIZ', 'PERGUNTA', 'ALTERNATIVA')),
    constraint alteracao_conteudo_acao_check
        check (acao in ('CRIACAO', 'ALTERACAO', 'REMOCAO', 'ATIVACAO', 'DESATIVACAO', 'REORDENACAO'))
);

create index alteracao_conteudo_entidade_idx on alteracao_conteudo (entidade_id, ocorrida_em desc);

create index alteracao_conteudo_ocorrida_idx on alteracao_conteudo (ocorrida_em desc);

create index alteracao_conteudo_usuario_idx on alteracao_conteudo (usuario_id);
