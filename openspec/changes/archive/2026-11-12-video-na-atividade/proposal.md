# Vídeo na atividade

## Motivo

A atividade precisa entregar uma URL do YouTube e recusar registros de assistência anteriores à duração do vídeo.

## Escopo

Validação de URL HTTPS do YouTube, início de assistência autenticado, carimbo persistido por integrante e atividade, conferência de duração no servidor e testes com conteúdo próprio. Os links e durações do conteúdo dependem dos vídeos fornecidos pelo responsável pelo treinamento.

## Impacto

O app chama `POST /api/v1/atividades/{atividadeId}/video-iniciado` ao iniciar a reprodução e `POST /api/v1/atividades/{atividadeId}/video-assistido` após a duração. O servidor mede tempo decorrido; ele não verifica reprodução efetiva no player. Conteúdo sem vídeo mantém seu contrato de leitura.
