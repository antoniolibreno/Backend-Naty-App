create table ponto_concedido (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references usuario (id) on delete cascade,
    atividade_id uuid not null references atividade (id),
    xp integer not null check (xp >= 0),
    concedido_em timestamptz not null
);

create unique index ponto_concedido_usuario_atividade_idx on ponto_concedido (usuario_id, atividade_id);

create table dia_de_estudo (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references usuario (id) on delete cascade,
    dia date not null,
    registrado_em timestamptz not null default now()
);

create unique index dia_de_estudo_usuario_dia_idx on dia_de_estudo (usuario_id, dia);

create table conquista (
    id uuid primary key default gen_random_uuid(),
    codigo varchar(64) not null unique,
    nome varchar(255) not null,
    descricao text not null,
    criterio varchar(32) not null,
    limiar integer not null check (limiar > 0),
    ordem integer not null unique,
    constraint conquista_criterio_check check (criterio in (
        'ATIVIDADES_CONCLUIDAS', 'MAIOR_NOTA_EM_QUIZ', 'MODULOS_CONCLUIDOS',
        'TRILHAS_CONCLUIDAS', 'MAIOR_SEQUENCIA', 'PONTOS'))
);

create table conquista_obtida (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references usuario (id) on delete cascade,
    conquista_id uuid not null references conquista (id),
    obtida_em timestamptz not null
);

create unique index conquista_obtida_usuario_conquista_idx on conquista_obtida (usuario_id, conquista_id);

insert into ponto_concedido (usuario_id, atividade_id, xp, concedido_em)
select p.usuario_id, p.atividade_id, a.xp, p.concluido_em
from progresso_atividade p
join atividade a on a.id = p.atividade_id
where p.concluido_em is not null;

insert into dia_de_estudo (usuario_id, dia)
select distinct fato.usuario_id, (fato.ocorrido_em at time zone e.fuso_horario)::date
from (
    select usuario_id, concluido_em as ocorrido_em from progresso_atividade where concluido_em is not null
    union all
    select usuario_id, video_assistido_em from progresso_atividade where video_assistido_em is not null
    union all
    select usuario_id, criada_em from tentativa_quiz
) fato
join usuario u on u.id = fato.usuario_id
join empresa e on e.id = u.empresa_id;
