# Tarefas

## 1. Perfil de teste

- [x] 1.1 Criar `src/test/resources/application-test.yml` com locations de migration e seed.
- [x] 1.2 Anotar `IntegracaoTest` com `@ActiveProfiles("test")`.
- [x] 1.3 Rodar a suíte completa e confirmar verde.

## 2. E-mail aleatório

- [x] 2.1 Criar `emailUnico` em `IntegracaoTest`.
- [x] 2.2 Trocar e-mails fixos em `IsolamentoPorEmpresaTest`, `SessaoApiTest` e `AutenticacaoApiTest`.
- [x] 2.3 Rodar os testes de `usuario` e confirmar verde.

## 3. Regras de arquitetura

- [x] 3.1 Adicionar ArchUnit em escopo `test` no `pom.xml`.
- [x] 3.2 Escrever `RegrasDeDependenciaTest` com as três regras.
- [x] 3.3 Provar que cada regra falha ao violar (violação temporária, revertida).

## 4. Regra de escrita no build

- [x] 4.1 Escrever `RegrasDeEscritaTest` para código e documentos.
- [x] 4.2 Limpar os acusados em código e documentos.
- [x] 4.3 Confirmar que a regra roda em `./mvnw -B verify`.

## 5. Integração contínua

- [x] 5.1 Criar `.github/workflows/ci.yml`.

## 6. Fechamento

- [x] 6.1 Marcar 10.3, 10.5, 10.9, 10.10, 10.11 e 10.18 em `docs/backlog.md`.
- [x] 6.2 Rodar `./mvnw -B test-compile` e `./mvnw -B verify`.
