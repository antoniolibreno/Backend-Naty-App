# acompanhamento-de-atividades Specification

## Purpose
Mostra, por atividade, quantos integrantes chegaram e quantos passaram, para a Naty achar a
atividade que barra a empresa inteira.

## Requirements

### Requirement: Funil por atividade

`GET /api/v1/acompanhamento/atividades` SHALL listar, na ordem das trilhas ativas, dos módulos e
das atividades, cada atividade com identificador, título, trilha, quantos integrantes chegaram,
quantos passaram e o percentual de passagem. Chegaram SHALL ser os integrantes ativos de papel
diferente de `NATY` para quem a atividade está liberada: todos para a primeira atividade de uma
trilha, e quem concluiu a atividade anterior da mesma trilha nas demais. Passaram SHALL ser os que a
concluíram. O percentual SHALL ser a divisão inteira de passaram por chegaram vezes cem, e zero
quando ninguém chegou. O parâmetro opcional `empresaId` SHALL restringir a contagem a uma empresa,
e sem ele a contagem SHALL somar todas as empresas. Trilha inativa NÃO SHALL aparecer. A rota SHALL
exigir conta `NATY`.

#### Scenario: Funil com gargalo

- **WHEN** quatro integrantes concluíram a primeira atividade e um concluiu a segunda
- **THEN** a segunda atividade tem 4 que chegaram e 1 que passou, com percentual 25

#### Scenario: Primeira atividade

- **WHEN** a empresa tem três integrantes ativos
- **THEN** a primeira atividade de uma trilha tem 3 que chegaram

#### Scenario: Filtro por empresa

- **WHEN** a conta `NATY` informa `empresaId`
- **THEN** só integrantes dessa empresa entram nas contagens

#### Scenario: Integrante comum

- **WHEN** uma conta `INTEGRANTE` ou `ADMIN` chama a rota
- **THEN** o sistema responde 403 `ACESSO_NEGADO`

#### Scenario: Empresa inexistente no filtro

- **WHEN** a conta `NATY` informa um `empresaId` que não existe
- **THEN** o sistema responde 404 `RECURSO_NAO_ENCONTRADO`
