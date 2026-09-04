# natyapi

## Responsabilidade

Unico ponto do sistema que fala com a Naty API V3. Traduz HTTP e JSON externos em
tipos internos e em excecoes do dominio. Nenhum outro pacote monta URL, header ou
trata status code da Naty.

## Contratos

- `NatyApiProperties`: record com `@ConfigurationProperties(prefix = "naty.api")`,
  carregando `url`, `token` e `timeout`.
- `NatyApiClient`: vai expor os metodos de leitura da Naty API. E o unico consumidor
  do `RestClient` de `config`.
- `dto/`: representacoes do JSON da Naty API, nao do nosso dominio. `NatyUsuarioResponse`
  e `NatyPaginaResponse` viram record quando forem preenchidos.
- `exception/`: `NatyApiException` e a raiz. `NatyAuthenticationException` para 401 e
  403, `NatyRateLimitException` para 429, carregando a espera sugerida pelo header.

## Decisoes

As tres excecoes ja nascem reais, mesmo sem cliente implementado, porque a hierarquia
delas e o contrato que `sincronizacao` e `health` vao capturar. Definir isso agora
evita que cada pacote invente o proprio tratamento de erro.

`NatyApiProperties` e record, nao classe com Lombok. Propriedade de configuracao e
imutavel por natureza, e o binder do Spring Boot suporta record direto.

O registro do `@ConfigurationProperties` vem do `@ConfigurationPropertiesScan` na
`NatySyncApplication`, nao de `@EnableConfigurationProperties` espalhado.

## Armadilhas

DTO deste pacote espelha o JSON da Naty, com os nomes que a Naty usa. Nao renomeie
campo para portugues aqui: a traducao para o dominio e trabalho do mapper em `usuario`.

`NATY_API_TOKEN` aceita valor vazio de proposito, para que a aplicacao suba em ambiente
sem credencial. Quem for implementar `NatyApiClient` precisa falhar com
`NatyAuthenticationException` clara quando o token estiver vazio, e nao com
`NullPointerException`.

## Estado atual

Stub: `NatyApiClient`, `dto/NatyUsuarioResponse`, `dto/NatyPaginaResponse`. Todos
preenchidos pela etapa da integracao com a Naty API, que tambem adiciona Resilience4j
ao `pom.xml`.

Real: `NatyApiProperties` e as tres excecoes.
