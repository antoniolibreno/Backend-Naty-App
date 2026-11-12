# Design

## Decisões

**Anotação direta no record.** `@Schema` entra nos componentes do record, sem classe auxiliar
nem arquivo de exemplos. Um exemplo mora ao lado do campo que descreve, e some junto com ele.

**Português com acento.** Todo texto do Swagger (descrição, resumo, exemplo em linguagem natural)
é escrito em português correto, com acento. Identificador, código de erro, URL, valor de enum e
UUID ficam como no código. O texto não usa travessão, verbo de mudança nem marcador de tempo,
conforme o `CLAUDE.md` raiz.

**Interface.** O Swagger UI abre com as tags recolhidas (`doc-expansion: none`), em ordem
alfabética (`tags-sorter: alpha`) e sem validador externo (`validator-url` vazio). O servidor e
cada tag carregam descrição.

**Texto breve.** A descrição diz o que a rota faz e a regra que o cliente precisa saber, em uma ou
duas frases. Operação: até cerca de 220 caracteres e resumo de até 35. Tag: até cerca de 100.
Campo e schema: até cerca de 60. Resposta: até cerca de 50, com o motivo e sem repetir o status.
Detalhe de implementação (formato interno de arquivo, nome de propriedade de configuração,
critério de desempate) fica fora do Swagger e mora no `CLAUDE.md` do pacote.

**Exemplos coerentes entre si.** Os mesmos valores aparecem em todo DTO que referencia a
mesma entidade: um UUID fixo por entidade, a empresa `Acme Treinamentos`, o fuso
`America/Sao_Paulo`, o e-mail `ana.souza@acme.com.br`. O exemplo de uma resposta é aceito pela
requisição correspondente.

**Exemplo de senha é fictício e fraco de propósito.** Nenhum exemplo usa valor que se pareça
com credencial real, nem a senha do seed.

**Erros por rota.** Cada controller declara os erros que a rota devolve com `@ApiResponse`
apontando para `ErroResposta`. A `description` de cada resposta diz o motivo específico da
recusa, como "Papel ausente ou inválido" ou "A troca deixaria o escritório sem administrador".
O `codigo` estável é documentado no schema de `ErroResposta`. A fonte dos motivos é o código
existente, não texto inventado.

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
