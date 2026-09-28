# natysync (pacote raiz)

## Responsabilidade

Ponto de entrada da aplicação e base dos testes de integração. Não contém regra de
negócio: cada funcionalidade mora no subpacote dela.

## Contratos

- `NatySyncApplication`: `@SpringBootApplication` com `@ConfigurationPropertiesScan`.
- `src/test/java/com/projetointegrador/natysync/PostgresTestcontainerConfiguration`: sobe
  `postgres:15-alpine` pelo Testcontainers e liga no datasource por `@ServiceConnection`.
- `src/test/java/com/projetointegrador/natysync/IntegracaoTest`: base abstrata dos testes
  HTTP. Sobe em porta aleatória, cria empresa e integrante padrão sob demanda, autentica
  em `POST /api/v1/sessoes` e remove os dois depois de cada teste.
- `src/test/java/com/projetointegrador/natysync/NatySyncApplicationTests`: prova que o
  contexto sobe contra o banco real.

## Decisões

Teste de integração roda contra PostgreSQL real, sem H2. Flyway e `ddl-auto: validate`
precisam do dialeto que roda fora do teste.

A major version do Postgres é a mesma no Testcontainers, no `docker-compose.yml` e no
Postgres local de desenvolvimento: 15.

## Armadilhas

Todo pacote de funcionalidade fica abaixo de `com.projetointegrador.natysync`. Classe fora
dessa raiz não entra na varredura de componente nem na de `@ConfigurationProperties`, e o
contexto sobe sem ela, sem erro.

Trocar a versão do Postgres em um lugar só abre divergência silenciosa: o teste passa em
uma versão e a aplicação roda em outra. `PostgresTestcontainerConfiguration` e
`docker-compose.yml` mudam juntos.

Testcontainers exige o Docker rodando. Sem ele, `./mvnw -B verify` falha nos testes de
integração, mesmo com o Postgres local de pé.

O `@AfterEach` de `IntegracaoTest` limpa só a empresa e o integrante padrão. O que o teste
cria com `criarEmpresa` e `criarIntegrante` fica no banco, e o banco é compartilhado entre
as classes de teste que reaproveitam o mesmo contexto Spring.
