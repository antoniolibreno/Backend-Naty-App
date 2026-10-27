alter table progresso_atividade add column video_iniciado_em timestamptz;

alter table atividade add constraint atividade_video_duracao_check
    check (video_url is null or (duracao_segundos is not null and duracao_segundos > 0));

alter table atividade add constraint atividade_video_dominio_check
    check (video_url is null or video_url ~* '^https://(youtube\.com|www\.youtube\.com|m\.youtube\.com|youtu\.be)/');
