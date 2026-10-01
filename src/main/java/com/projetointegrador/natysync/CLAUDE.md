# natysync (pacote raiz)

## Responsabilidade

Ponto de entrada da aplicação e base dos testes de integração. Não contém regra de negócio: cada funcionalidade mora no subpacote dela.

## Contratos

- `NatySyncApplication`: `@SpringBootApplication` com `@ConfigurationPropertiesScan`.
- `src/test/java/com/projetointegrador/natysync/PostgresTestcontainerConfiguration`: sobe `postgres:15-alpine` pelo Testcontainers e liga no datasource por `@ServiceConnection`.
- `src/test/java/com/projetointegrador/natysync/IntegracaoTest`: base abstrata dos testes HTTP e limpeza das empresas criadas durante o teste.
- A conclusão da atividade com quiz acontece somente por tentativa aprovada; atividade sem quiz conclui ao registrar vídeo assistido.

## Decisões

Teste de integração roda contra PostgreSQL real, sem H2. Flyway e `ddl-auto: validate` precisam do dialeto que roda fora do teste.

## Armadilhas

Todo pacote de funcionalidade fica abaixo de `com.projetointegrador.natysync`.

Testcontainers exige o Docker rodando. Sem ele, `./mvnw -B verify` falha nos testes de integração.
