-- Dados demonstrativos de gamificacao para o usuario user@user.com.
-- Mantem o perfil pronto para o app testar XP, nivel, sequencia e conquistas.

insert into usuario (id, empresa_id, nome, email, perfil, status, papel, ativo, senha_hash)
values
    ('00000000-0000-0000-1001-000000000004', '00000000-0000-0000-1000-000000000001',
     'Maria Ranking', 'maria@user.com', 'supervisor', 'offline', 'INTEGRANTE', true,
     '{bcrypt}$2a$10$UEcBm/s9mBcpaHu9u.w.FunMf78e0MCSZOQZ1Kja7S4wJH37FMxQa'),
    ('00000000-0000-0000-1001-000000000005', '00000000-0000-0000-1000-000000000001',
     'Joao Ranking', 'joao@user.com', 'user', 'offline', 'INTEGRANTE', true,
     '{bcrypt}$2a$10$UEcBm/s9mBcpaHu9u.w.FunMf78e0MCSZOQZ1Kja7S4wJH37FMxQa'),
    ('00000000-0000-0000-1001-000000000006', '00000000-0000-0000-1000-000000000001',
     'Ana Ranking', 'ana@user.com', 'user', 'offline', 'INTEGRANTE', true,
     '{bcrypt}$2a$10$UEcBm/s9mBcpaHu9u.w.FunMf78e0MCSZOQZ1Kja7S4wJH37FMxQa')
on conflict (id) do update set
    empresa_id = excluded.empresa_id,
    nome = excluded.nome,
    email = excluded.email,
    perfil = excluded.perfil,
    status = excluded.status,
    papel = excluded.papel,
    ativo = excluded.ativo,
    senha_hash = excluded.senha_hash;

insert into pontuacao_integrante (usuario_id, empresa_id, xp_total, pontuacao_atingida_em)
values (
    '00000000-0000-0000-1001-000000000002',
    '00000000-0000-0000-1000-000000000001',
    50,
    now()
)
on conflict (usuario_id) do update set
    empresa_id = excluded.empresa_id,
    xp_total = excluded.xp_total,
    pontuacao_atingida_em = excluded.pontuacao_atingida_em,
    atualizado_em = now();

insert into xp_atividade (usuario_id, atividade_id, empresa_id, xp)
values
    ('00000000-0000-0000-1001-000000000002', '00000000-0000-0000-0002-000000000101', '00000000-0000-0000-1000-000000000001', 10),
    ('00000000-0000-0000-1001-000000000002', '00000000-0000-0000-0002-000000000102', '00000000-0000-0000-1000-000000000001', 10),
    ('00000000-0000-0000-1001-000000000002', '00000000-0000-0000-0002-000000000103', '00000000-0000-0000-1000-000000000001', 10),
    ('00000000-0000-0000-1001-000000000002', '00000000-0000-0000-0002-000000000104', '00000000-0000-0000-1000-000000000001', 10),
    ('00000000-0000-0000-1001-000000000002', '00000000-0000-0000-0002-000000000105', '00000000-0000-0000-1000-000000000001', 10)
on conflict (usuario_id, atividade_id) do update set
    empresa_id = excluded.empresa_id,
    xp = excluded.xp;

insert into sequencia_integrante (
    usuario_id, empresa_id, dias_atuais, maior_sequencia, ultimo_evento_em, ultimo_dia_local
)
values (
    '00000000-0000-0000-1001-000000000002',
    '00000000-0000-0000-1000-000000000001',
    3,
    3,
    now(),
    (now() at time zone 'America/Sao_Paulo')::date
)
on conflict (usuario_id) do update set
    empresa_id = excluded.empresa_id,
    dias_atuais = excluded.dias_atuais,
    maior_sequencia = excluded.maior_sequencia,
    ultimo_evento_em = excluded.ultimo_evento_em,
    ultimo_dia_local = excluded.ultimo_dia_local;

insert into conquista_usuario (usuario_id, conquista_id)
select '00000000-0000-0000-1001-000000000002', id
from conquista
where codigo in ('PRIMEIRA_ATIVIDADE', 'QUIZ_PERFEITO', 'SEQUENCIA_3_DIAS')
on conflict (usuario_id, conquista_id) do nothing;

insert into pontuacao_integrante (usuario_id, empresa_id, xp_total, pontuacao_atingida_em)
values
    ('00000000-0000-0000-1001-000000000004', '00000000-0000-0000-1000-000000000001', 350, now() - interval '3 days'),
    ('00000000-0000-0000-1001-000000000005', '00000000-0000-0000-1000-000000000001', 120, now() - interval '2 days'),
    ('00000000-0000-0000-1001-000000000006', '00000000-0000-0000-1000-000000000001', 20, now() - interval '1 day')
on conflict (usuario_id) do update set
    empresa_id = excluded.empresa_id,
    xp_total = excluded.xp_total,
    pontuacao_atingida_em = excluded.pontuacao_atingida_em,
    atualizado_em = now();

insert into sequencia_integrante (
    usuario_id, empresa_id, dias_atuais, maior_sequencia, ultimo_evento_em, ultimo_dia_local
)
values
    ('00000000-0000-0000-1001-000000000004', '00000000-0000-0000-1000-000000000001', 7, 7, now(), (now() at time zone 'America/Sao_Paulo')::date),
    ('00000000-0000-0000-1001-000000000005', '00000000-0000-0000-1000-000000000001', 3, 5, now(), (now() at time zone 'America/Sao_Paulo')::date),
    ('00000000-0000-0000-1001-000000000006', '00000000-0000-0000-1000-000000000001', 1, 2, now(), (now() at time zone 'America/Sao_Paulo')::date)
on conflict (usuario_id) do update set
    empresa_id = excluded.empresa_id,
    dias_atuais = excluded.dias_atuais,
    maior_sequencia = excluded.maior_sequencia,
    ultimo_evento_em = excluded.ultimo_evento_em,
    ultimo_dia_local = excluded.ultimo_dia_local;

insert into conquista_usuario (usuario_id, conquista_id)
select usuarios.id, conquistas.id
from (values
    ('00000000-0000-0000-1001-000000000004'::uuid, 'SEQUENCIA_7_DIAS'),
    ('00000000-0000-0000-1001-000000000005'::uuid, 'SEQUENCIA_3_DIAS'),
    ('00000000-0000-0000-1001-000000000006'::uuid, 'PRIMEIRA_ATIVIDADE')
) as usuarios(id, codigo)
join conquista conquistas on conquistas.codigo = usuarios.codigo
on conflict (usuario_id, conquista_id) do nothing;
