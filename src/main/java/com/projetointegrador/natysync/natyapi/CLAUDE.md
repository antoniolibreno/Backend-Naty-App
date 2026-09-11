# natyapi

## Responsabilidade

Único ponto do sistema que fala com a Naty API V3. Traduz HTTP e JSON externos em
tipos internos e em exceções do domínio. Nenhum outro pacote monta URL, header ou
trata status code da Naty.

## Contratos

- `NatyApiProperties`: record com `@ConfigurationProperties(prefix = "naty.api")`,
  carregando `url`, `token` e `timeout`.
- `NatyApiClient`: vai expor os métodos de leitura da Naty API. É o único consumidor
  do `RestClient` de `config`.
- `dto/`: representações do JSON da Naty API, não do nosso domínio. `NatyUsuarioResponse`
  e `NatyPaginaResponse` viram record quando forem preenchidos.
- `exception/`: `NatyApiException` é a raiz. `NatyAuthenticationException` para 401 e
  403, `NatyRateLimitException` para 429, carregando a espera sugerida pelo header.

## Decisões

As três exceções já nascem reais, mesmo sem cliente implementado, porque a hierarquia
delas é o contrato que `sincronizacao` e `health` vão capturar. Definir isso agora
evita que cada pacote invente o próprio tratamento de erro.

`NatyApiProperties` é record, não classe com Lombok. Propriedade de configuração é
imutável por natureza, e o binder do Spring Boot suporta record direto.

O registro do `@ConfigurationProperties` vem do `@ConfigurationPropertiesScan` na
`NatySyncApplication`, não de `@EnableConfigurationProperties` espalhado.

## Armadilhas

DTO deste pacote espelha o JSON da Naty, com os nomes que a Naty usa. Não renomeie
campo para português aqui: a tradução para o domínio é trabalho do mapper em `usuario`.

`NATY_API_TOKEN` aceita valor vazio de propósito, para que a aplicação suba em ambiente
sem credencial. Quem for implementar `NatyApiClient` precisa falhar com
`NatyAuthenticationException` clara quando o token estiver vazio, e não com
`NullPointerException`.

## Estado atual

Stub: `NatyApiClient`, `dto/NatyUsuarioResponse`, `dto/NatyPaginaResponse`. Todos
preenchidos pela etapa da integração com a Naty API, que também adiciona Resilience4j
ao `pom.xml`.

Real: `NatyApiProperties` e as três exceções.
