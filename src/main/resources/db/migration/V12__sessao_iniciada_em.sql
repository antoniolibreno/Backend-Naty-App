alter table sessao
    add column iniciada_em timestamptz not null default now();

update sessao set iniciada_em = criado_em;
