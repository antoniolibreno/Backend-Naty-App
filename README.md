# Backend Naty App

Plataforma de treinamento para os clientes da Naty. Os integrantes da empresa que
contratou a Naty percorrem uma trilha de módulos e atividades, cada uma com imagem, vídeo
e quiz, avançando por desbloqueio linear. O backend expõe REST para o app Flutter e para
um painel administrativo, que é a fonte da verdade de quem existe.

A API entrega a leitura do conteúdo da trilha, a resolução de qual integrante está usando
o aplicativo e o progresso de cada um, com o estado de cada atividade numa chamada só. O
painel cadastra empresas e integrantes sob `/api/v1/painel/**`. O
alvo completo do produto está em `docs/regras.md`, e o que falta construir está em
`docs/backlog.md`.

Stack: Java 21, Spring Boot 4.1.1, Maven, PostgreSQL, Flyway, Docker Compose.

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
