## ADDED Requirements

### Requirement: Início de vídeo autenticado
O servidor SHALL registrar o primeiro início de vídeo por integrante e atividade desbloqueada em `POST /api/v1/atividades/{atividadeId}/video-iniciado` e SHALL preservar o instante em chamadas repetidas.

#### Scenario: Início repetido
- **WHEN** o integrante inicia duas vezes o mesmo vídeo
- **THEN** ambas as respostas contêm o mesmo `videoIniciadoEm`

### Requirement: Duração de assistência
O servidor SHALL recusar assistência de vídeo configurado sem início registrado ou antes de decorrer `duracao_segundos`, com HTTP 409 e código específico. Assistência já registrada SHALL ser idempotente.

#### Scenario: Término antecipado
- **WHEN** o integrante registra assistência antes da duração
- **THEN** responde 409 `VIDEO_TEMPO_INSUFICIENTE` sem registrar assistência ou conclusão

#### Scenario: Duração cumprida
- **WHEN** o tempo desde o início é igual ou superior à duração
- **THEN** registra assistência e conclui somente atividade sem quiz
