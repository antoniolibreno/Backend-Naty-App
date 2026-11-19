# Backend Naty App

Plataforma de treinamento para os clientes da Naty. Os integrantes da empresa que
contratou a Naty percorrem uma trilha de módulos e atividades, cada uma com imagem, vídeo
e quiz, avançando por desbloqueio linear. O backend expõe REST para o app Flutter e para
um painel administrativo, que é a fonte da verdade de quem existe.

A API entrega a leitura do conteúdo da trilha, a resolução de qual integrante está usando
o aplicativo e o progresso de cada um, com o estado de cada atividade numa chamada só. A
conclusão de atividade rende pontos, sequência de dias e conquistas, e cada empresa tem o
seu ranking. O painel cadastra empresas e integrantes sob `/api/v1/painel/**`. O
alvo completo do produto está em `docs/regras.md`, e o que falta construir está em
`docs/backlog.md`.

Stack: Java 21, Spring Boot 4.1.1, Maven, PostgreSQL, Flyway, Docker Compose.

## Assistência de vídeo

Para atividade com `videoUrl`, o app chama
`POST /api/v1/atividades/{atividadeId}/video-iniciado` ao iniciar a reprodução, com
`Authorization: Bearer`, sem corpo. A resposta contém `atividadeId`, `videoIniciadoEm`
e `duracaoSegundos`. Chamadas repetidas preservam o início.

O término usa `POST /api/v1/atividades/{atividadeId}/video-assistido`, com a mesma
sessão e sem corpo. O servidor exige tempo decorrido igual ou superior à duração.
Sem início responde 409 `VIDEO_NAO_INICIADO`; antes da duração responde 409
`VIDEO_TEMPO_INSUFICIENTE`. Assistência repetida preserva os instantes. Atividade
com quiz conclui por aprovação; atividade sem quiz conclui pela assistência.

## Gamificação

A primeira conclusão de cada atividade concede o `xp` dela, uma única vez. Tentativa de
quiz, vídeo assistido e conclusão marcam o dia de estudo no fuso da empresa, e a sequência
conta dias seguidos até o dia corrente ou o anterior. As conquistas são um catálogo global
semeado por migration e saem uma única vez por integrante.

`GET /api/v1/ranking` classifica as contas ativas da empresa de quem chama, paginado por
`pagina` e `tamanho`, com desempate por quem chegou primeiro ao total.
`GET /api/v1/integrantes/{integranteId}/gamificacao` devolve pontos, posição, sequência e
o catálogo de conquistas com o instante de obtenção, para a própria conta e para colega
ativo da mesma empresa. O app lê o perfil depois de uma conclusão para celebrar ponto e
conquista novos.

## Empresas e acesso

O painel cadastra a empresa com CNPJ (numérico ou alfanumérico, com ou sem máscara) e razão
social, e cria junto o primeiro `ADMIN`, que é o responsável pelo contrato, com nome, e-mail,
senha e telefone. `GET /api/v1/painel/empresas` aceita `busca` por nome, razão social ou CNPJ.
Uma empresa nunca fica sem `ADMIN` ativo: desativar ou rebaixar o último responde 409
`ULTIMO_ADMINISTRADOR`.

`POST /api/v1/sessoes` e a renovação devolvem o `papel` da conta (`INTEGRANTE`, `ADMIN` ou
`NATY`), para o cliente escolher entre o painel da Naty, o painel do contratante e o app.

A primeira conta `NATY` nasce do bootstrap por variável de ambiente, e a empresa interna da Naty
recebe `APP_BOOTSTRAP_NATY_CNPJ` e `APP_BOOTSTRAP_NATY_RAZAO_SOCIAL`, obrigatórias onde o
bootstrap é obrigatório.

## Rodar

```bash
docker compose up --build -d
curl -s localhost:8080/actuator/health
```

## Build local

```bash
./mvnw -B verify
```

Contexto de desenvolvimento e convenções: ver `CLAUDE.md` na raiz e o `CLAUDE.md` de cada
pacote. Regras de produto em `docs/regras.md` e backlog em `docs/backlog.md`.
