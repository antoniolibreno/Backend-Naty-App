# config

## Responsabilidade

Beans de infraestrutura que atravessam mais de um pacote: documentação OpenAPI, CORS e
resolvedor de argumento de controller. Não contém regra de negócio nem endpoint.

## Contratos

- `CorsConfig`: lê `app.cors.origens` e libera `/api/**` para essas origens.
- `WebMvcResolverConfig`: registra `usuario/IntegranteArgumentResolver` como resolvedor de
  argumento de controller.

## Decisões

`SecurityConfig` está deliberadamente ausente. O projeto não tem autenticação de usuário
final e o Spring Security não está nas dependências do `pom.xml`. Uma `@Configuration`
vazia com esse nome convidaria alguém a preenchê-la fora de escopo. A classe nasce em
proposta OpenSpec própria, com a dependência entrando no mesmo diff.

`CorsConfig` implementa `WebMvcConfigurer` em vez de expor um `CorsFilter`. Sem Spring
Security no classpath, o caminho do `WebMvcConfigurer` é o mais direto e aceita
complemento sem reescrita.

O registro do resolvedor mora aqui, e não no pacote `usuario`, porque registrar resolvedor
é configuração de infraestrutura web. O resolvedor em si mora em `usuario`, que é quem
sabe resolver integrante.

CORS só afeta Flutter Web. App mobile não passa por preflight, então um erro aqui não
aparece em teste no celular e só estoura no navegador.

## Armadilhas

`app.cors.origens` é lido como `List<String>` via `@Value`. Valor vazio no
`application.yml` quebra a subida do contexto, então mantenha sempre pelo menos uma origem
no default.

`allowedOriginPatterns` é usado no lugar de `allowedOrigins` porque o segundo proíbe
curinga junto com credenciais. `allowedOrigins` quebra `http://localhost:*` em
desenvolvimento.

## Ausências deliberadas

`OpenApiConfig` é uma `@Configuration` sem corpo: não define título, versão, servidores
nem agrupamento de tags do documento OpenAPI.

Não existe `SecurityConfig`.
