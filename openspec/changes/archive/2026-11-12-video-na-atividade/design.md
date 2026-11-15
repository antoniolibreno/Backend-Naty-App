# Desenho

`progresso_atividade.video_iniciado_em` guarda o primeiro início informado pela sessão autenticada. Repetições preservam esse instante e o registro assistido. As escritas de vídeo adquirem bloqueio pessimista na linha do integrante para serializar criação e atualização do progresso.

Vídeo configurado exige URL HTTPS de vídeo em `youtube.com`, `www.youtube.com`, `m.youtube.com` ou `youtu.be` e duração positiva. O domínio é comparado por igualdade, sem aceitar subdomínios arbitrários, credencial embutida ou porta própria. A entidade valida na atribuição, persistência e leitura; a migration restringe domínio e duração no banco.

O término exige início persistido e tempo decorrido maior ou igual a `duracao_segundos`. Recusas são conflitos com códigos `VIDEO_NAO_INICIADO` e `VIDEO_TEMPO_INSUFICIENTE`. Conteúdo sem vídeo não recebe a restrição temporal. Atividade com quiz só conclui por aprovação, e atividade sem quiz conclui pelo registro de assistência.

Migration aplicada é imutável. A carga de URLs e durações reais usa migration própria, com associação explícita entre atividade e vídeo.
