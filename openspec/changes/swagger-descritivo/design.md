# Design

## Decisões

**Anotação direta no record.** `@Schema` entra nos componentes do record, sem classe auxiliar
nem arquivo de exemplos. Um exemplo mora ao lado do campo que descreve, e some junto com ele.

**Texto em ASCII.** O Swagger existente não usa acento nas descrições. O texto novo segue o
mesmo padrão, sem travessão, sem verbo de mudança e sem marcador de tempo, conforme o
`CLAUDE.md` raiz.

**Exemplos coerentes entre si.** Os mesmos valores aparecem em todo DTO que referencia a
mesma entidade: um UUID fixo por entidade, a empresa `Acme Treinamentos`, o fuso
`America/Sao_Paulo`, o e-mail `ana.souza@acme.com.br`. O exemplo de uma resposta é aceito pela
requisição correspondente.

**Exemplo de senha é fictício e fraco de propósito.** Nenhum exemplo usa valor que se pareça
com credencial real, nem a senha do seed.

**Erros por constante.** Cada controller declara os erros que a rota devolve com
`@ApiResponse` apontando para `ErroResposta`. O `codigo` estável aparece na `description` da
resposta. A fonte dos códigos é o código existente (`CodigoDeConflito`, exceções de `shared`),
não texto inventado.

**401 e 403 globais.** `OpenApiConfig` registra um `OpenApiCustomizer` que acrescenta 401 em
toda operação protegida. O 403 `ACESSO_NEGADO` entra por operação, só nas rotas de papel, pois a
cadeia de segurança só o devolve ali.

**Sem mudança de formato.** `@Schema` não altera serialização. Anotações não validam: a
restrição continua em Bean Validation, e `@Schema` apenas a espelha em `minLength`, `maxLength` e
`requiredMode` quando o valor já existe na validação.

## Riscos

`@Schema(requiredMode)` pode divergir da validação real e enganar o time do app. Mitigação: só
declarar o que a anotação de Bean Validation já afirma, e o teste de `/v3/api-docs` confere
campos `required` contra `@NotNull` e `@NotBlank` dos records.

Valor de exemplo que a validação recusaria quebra o "Try it out". Mitigação: exemplo escolhido
dentro das restrições do campo.

## Ausências deliberadas

Nenhum agrupamento em `GroupedOpenApi`, nenhuma exportação de `openapi.json` versionada e
nenhuma habilitação do Swagger em produção.
