# config

## Responsabilidade

Beans de infraestrutura que atravessam mais de um pacote: cliente HTTP, agendamento,
documentacao OpenAPI e CORS. Nao contem regra de negocio nem endpoint.

## Contratos

- `RestClientConfig`: vai expor o `RestClient` usado pelo `natyapi` para falar com a
  Naty API, com timeout e interceptor de token.
- `SchedulerConfig`: vai habilitar `@EnableScheduling` e definir o pool de threads do
  `SincronizacaoScheduler`.
- `OpenApiConfig`: vai customizar titulo, versao e servidores do documento OpenAPI.
- `CorsConfig`: unica classe real do pacote nesta etapa. Le `app.cors.origens` e
  libera `/api/**` para essas origens.

## Decisoes

`SecurityConfig` foi deliberadamente omitido. A fase atual nao tem autenticacao de
usuario final e o Spring Security nao esta nas dependencias do `pom.xml`. Criar uma
`@Configuration` vazia com esse nome convidaria alguem a preenche-la fora de escopo.
Quando autenticacao entrar, ela vira proposta OpenSpec propria, com a dependencia
entrando no mesmo diff.

`CorsConfig` implementa `WebMvcConfigurer` em vez de expor um `CorsFilter`. Sem Spring
Security no classpath, o caminho do `WebMvcConfigurer` e o mais direto e nao precisa
ser reescrito quando Security entrar, so complementado.

CORS so afeta Flutter Web. App mobile nao passa por preflight, entao um erro aqui nao
aparece em teste no celular e so estoura no navegador.

## Armadilhas

`app.cors.origens` e lido como `List<String>` via `@Value`. Valor vazio no
`application.yml` quebra a subida do contexto, entao mantenha sempre pelo menos uma
origem no default.

`allowedOriginPatterns` e usado no lugar de `allowedOrigins` porque o segundo proibe
curinga junto com credenciais. Trocar de volta quebra `http://localhost:*` em dev.

## Estado atual

Stub: `RestClientConfig`, `SchedulerConfig`, `OpenApiConfig`. Sao `@Configuration`
vazias que compilam. `RestClientConfig` e preenchido pela etapa da integracao com a
Naty API, `SchedulerConfig` pela etapa da sincronizacao, `OpenApiConfig` pela etapa
que fechar o contrato REST.

Real: `CorsConfig`.
