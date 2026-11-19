# Design

## Perfil de teste

`application-test.yml` fixa `spring.flyway.locations` em `classpath:db/migration,classpath:db/seed-dev`,
desliga `show-sql` e reduz o log. `IntegracaoTest` ganha `@ActiveProfiles({"dev", "test"})`, porque `TravaDaContaDeSeed` recusa subir com conta de seed fora do perfil `dev`. Propriedades do perfil `test` vencem as do `dev`. Sem o
seed no perfil de teste, todos os testes de integração caem de uma vez, então o perfil
declara o seed de forma explícita e não por herança do perfil `dev`.
`TravaDaContaDeSeedTest` e `BootstrapNatyTest` controlam perfis por conta própria e seguem
intactos.

## E-mail aleatório

`IntegracaoTest` ganha o helper `emailUnico(String prefixo)`, que devolve
`prefixo + "-" + UUID + "@teste.com.br"`. As três classes com e-mail fixo
(`IsolamentoPorEmpresaTest`, `SessaoApiTest`, `AutenticacaoApiTest`) guardam o e-mail em
campo de instância criado no `@BeforeEach`. E-mail de teste que prova recusa de conta
inexistente também fica aleatório.

## Regras de arquitetura

Decisão: ArchUnit como dependência de escopo `test`. Alternativa descartada: varrer
imports por texto, que não enxerga herança nem tipo de campo. As regras:

- classes de `trilha` não dependem de `progresso`;
- só classes de `painel` e `usuario` chamam métodos de escrita de `UsuarioRepository`;
- classes `*Response` de `trilha` não declaram campo `correta`.

Se a regra de escrita em `usuario` não for expressável por ArchUnit sem falso positivo, o
teste restringe a verificação a `save`, `delete` e `saveAll`.

## Regra de comentário e de escrita no build

Teste JUnit `RegrasDeEscritaTest`, sem plugin novo, roda em `verify`. Em `src/main/java` e
`src/test/java` reprova: bloco `/* */` e Javadoc, comentário `//` com mais de uma linha
seguida, e os marcadores `TODO`, `FIXME`, `@since`, `@author`, "alterado por". Em `*.md` de
`docs/`, `openspec/specs/`, `CLAUDE.md` e `README.md` reprova os verbos de mudança e os
marcadores de tempo listados no `CLAUDE.md` raiz. Documentos de change em
`openspec/changes/` ficam fora da varredura porque descrevem o trabalho em curso.
Os verbos entram como lista fechada, em palavra inteira, para limitar falso positivo.

## Integração contínua

`.github/workflows/ci.yml` dispara em push e pull request, usa JDK 21 e executa
`./mvnw -B verify`. O runner Ubuntu traz Docker, que os Testcontainers exigem. Cache do
repositório Maven por `actions/setup-java`.

## Riscos

- A regra de comentário pode acusar código existente. A migração limpa os acusados no mesmo
  diff, sem relaxar a regra.
- Seed e Testcontainers lentos em CI. Aceito.
