# Backend Naty App

Plataforma de treinamento gamificada para os clientes da Naty. Os integrantes da
empresa que contratou a Naty percorrem uma trilha de atividades, cada uma com um video
e um quiz, com pontuacao, sequencia de dias, conquistas e ranking. O backend expoe REST
para o app Flutter e le usuarios da Naty API V3 para saber quem sao os integrantes de
cada empresa.

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

Contexto de desenvolvimento e convencoes: ver `CLAUDE.md` na raiz e o `CLAUDE.md`
de cada pacote.
