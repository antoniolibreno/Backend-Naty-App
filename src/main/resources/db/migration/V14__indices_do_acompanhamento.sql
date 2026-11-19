create index progresso_atividade_concluida_idx on progresso_atividade (atividade_id)
    where concluido_em is not null;

create index usuario_empresa_considerado_idx on usuario (empresa_id)
    where ativo and papel <> 'NATY';
