alter table empresa
    add column fuso_horario varchar(64) not null default 'America/Sao_Paulo';

alter table usuario
    add constraint usuario_papel_check check (papel in ('INTEGRANTE', 'ADMIN', 'NATY'));

create index usuario_empresa_idx on usuario (empresa_id);
