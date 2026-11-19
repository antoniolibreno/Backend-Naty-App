# Tarefas

## 1. Base do pacote

- [x] 1.1 Ler `progresso/CLAUDE.md`, `usuario/CLAUDE.md`, `empresa/CLAUDE.md` e `config/CLAUDE.md`.
- [x] 1.2 Criar a migration dos dois índices parciais.
- [x] 1.3 Adicionar `app.acompanhamento.dias-parado` em `application.yml`.
- [x] 1.4 Adicionar o matcher `NATY` de `/api/v1/acompanhamento/**` em `SecurityConfig`.
- [x] 1.5 Criar o pacote `acompanhamento` com os repositórios de leitura.

## 2. Cálculo

- [x] 2.1 Criar `SituacaoDoIntegrante`, a enumeração das situações e `CalculadoraDeAcompanhamento`.
- [x] 2.2 Criar `AcompanhamentoService` com listagem, métricas e lista completa.

## 3. Rotas

- [x] 3.1 Criar `GET /api/v1/acompanhamento/empresas/{empresaId}/integrantes` com filtro e paginação.
- [x] 3.2 Criar `GET /api/v1/acompanhamento/empresas/{empresaId}/metricas`.
- [x] 3.3 Criar `CsvDeAcompanhamento` e `GET .../integrantes.csv`.
- [x] 3.4 Criar `GET /api/v1/acompanhamento/atividades` com o funil e o filtro `empresaId`.

## 4. Testes

- [x] 4.1 Testar que `ADMIN`, `INTEGRANTE` e sem token são recusados em toda rota.
- [x] 4.2 Testar a calculadora de situação nos limites do prazo, nos quatro estados e no fuso.
- [x] 4.3 Testar a listagem: ordem, filtro, paginação, outra empresa, desativado e `NATY` fora.
- [x] 4.4 Testar as métricas e que as contagens coincidem com a listagem.
- [x] 4.5 Testar o CSV: cabeçalho, ordem, fórmula, vírgula e cabeçalhos HTTP.
- [x] 4.6 Testar o funil, o filtro por empresa e empresa inexistente.
- [x] 4.7 Testar o prazo configurado por propriedade.
- [x] 4.8 Atualizar `RegrasDeDependenciaTest`: `acompanhamento` só lê e nada depende dele.

## 5. Documentação e fechamento

- [x] 5.1 Escrever `acompanhamento/CLAUDE.md`, atualizar `CLAUDE.md` raiz e `docs/regras.md`.
- [x] 5.2 Marcar 7.1 a 7.9 em `docs/backlog.md`.
- [x] 5.3 Rodar `./mvnw -B test-compile` e `./mvnw -B verify`.
