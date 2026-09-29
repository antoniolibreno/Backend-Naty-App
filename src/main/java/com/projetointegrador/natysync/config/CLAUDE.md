# config

## Responsabilidade

Beans de infraestrutura que atravessam mais de um pacote: documentação OpenAPI, CORS e
resolvedor de argumento de controller. Não contém regra de negócio nem endpoint.

## Contratos

- `CorsConfig`: lê `app.cors.origens` e libera `/api/**` para essas origens.
- `WebMvcResolverConfig`: registra `usuario/IntegranteArgumentResolver` como resolvedor de
  argumento de controller.
- `SecurityConfig`: cadeia de segurança, codificador de senha e regras de papel.
- `RecusaDeAcesso` e `NegacaoDeAcesso`: corpo do 401 e do 403.

## Decisões

`SecurityConfig` define a cadeia inteira: sessão sem estado, CSRF desligado por ser API de
token, e `usuario/TokenSessaoFiltro` antes do filtro de usuário e senha. Público é só a
emissão de sessão, o health e a documentação.

A regra de papel é por prefixo de rota, do mais específico para o mais geral:
`/api/v1/painel/empresas/**` exige `NATY`, `/api/v1/painel/integrantes/**` exige `ADMIN`,
e o resto de `/api/v1/painel/**` é negado. Rota administrativa nova entra com o seu
matcher antes do `anyRequest()`.

`RecusaDeAcesso` escreve `ErroResposta` no corpo do 401, e `NegacaoDeAcesso` no corpo do
403 com código `ACESSO_NEGADO`. Sem eles o Spring Security devolve um corpo próprio, e o app
teria dois formatos de erro para fazer parse.

O codificador de senha é `DelegatingPasswordEncoder` com bcrypt como padrão. O prefixo
`{bcrypt}` guardado junto do hash é o que permite trocar de algoritmo sem invalidar
credencial existente.

`CorsConfig` implementa `WebMvcConfigurer` em vez de expor um `CorsFilter`. O preflight
`OPTIONS` é liberado em `SecurityConfig`, porque a cadeia de segurança roda antes do MVC e
recusaria a requisição antes de o CORS do MVC responder.

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

`hasRole` compara com a autoridade `ROLE_<papel>` que `usuario/TokenSessaoFiltro` põe no
contexto. Matcher escrito com o prefixo `ROLE_` dentro de `hasRole` nunca casa.

Chamada sem token numa rota negada recebe 401, não 403: a cadeia pede autenticação antes
de avaliar o papel.
