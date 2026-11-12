alter table empresa
    add column cnpj varchar(14),
    add column razao_social varchar(255);

alter table empresa
    add constraint empresa_cnpj_formato_check check (cnpj ~ '^[0-9A-Z]{12}[0-9]{2}$');

create unique index empresa_cnpj_idx on empresa (cnpj);

alter table usuario
    add column telefone varchar(13);

alter table usuario
    add constraint usuario_telefone_check check (telefone ~ '^[0-9]{10,13}$');
