drop index usuario_empresa_naty_id_idx;

alter table usuario
    drop column naty_id,
    drop column ultimo_acesso_naty,
    drop column sincronizado_em,
    drop column payload;

alter table empresa
    drop column naty_api_token;
