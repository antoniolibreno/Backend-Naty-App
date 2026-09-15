# config

## Responsabilidade

Beans de infraestrutura que atravessam mais de um pacote: documentação OpenAPI, CORS e
resolvedor de argumento de controller. Não contém regra de negocio nem endpoint.

## Contratos

- `OpenApiConfig`: vai customizar título, versão e servidores do documento OpenAPI.
- `CorsConfig`: única classe real do pacote nesta etapa. Lê `app.cors.origens` e
  libera `/api/**` para essas origens.

## Decisões

`SecurityConfig` foi deliberadamente omitido. A fase atual não tem autenticação de
usuário final e o Spring Security não está nas dependências do `pom.xml`. Criar uma
`@Configuration` vazia com esse nome convidaria alguém a preenchê-la fora de escopo.
Quando autenticação entrar, ela vira proposta OpenSpec própria, com a dependência
entrando no mesmo diff.

`CorsConfig` implementa `WebMvcConfigurer` em vez de expor um `CorsFilter`. Sem Spring
Security no classpath, o caminho do `WebMvcConfigurer` é o mais direto e não precisa
ser reescrito quando Security entrar, só complementado.

CORS só afeta Flutter Web. App mobile não passa por preflight, então um erro aqui não
aparece em teste no celular e só estoura no navegador.

## Armadilhas

`app.cors.origens` é lido como `List<String>` via `@Value`. Valor vazio no
`application.yml` quebra a subida do contexto, então mantenha sempre pelo menos uma
origem no default.

`allowedOriginPatterns` é usado no lugar de `allowedOrigins` porque o segundo proíbe
curinga junto com credenciais. Trocar de volta quebra `http://localhost:*` em dev.

## Estado atual

Stub: `OpenApiConfig`. É uma `@Configuration` vazia que compila, preenchida pela etapa
que fechar o contrato REST.

Real: `CorsConfig` e `WebMvcResolverConfig`.

`WebMvcResolverConfig` registra `usuario/IntegranteArgumentResolver` como resolvedor de
argumento de controller. Ele existe aqui, e não no pacote `usuario`, porque registrar
resolvedor é configuração de infraestrutura web. O resolvedor em si mora em `usuario`,
que é quem sabe resolver integrante.
