# Identidade e sessão autenticada

## Why

`POST /api/v1/sessoes` exige senha e a descarta. Qualquer senha não vazia resolve o
integrante, e nenhuma credencial é emitida. As operações de progresso identificam quem
chama pelo cabeçalho `X-Integrante-Id`, que o cliente preenche com o identificador que
quiser. Qualquer um marca atividade como concluída em nome de outro, e qualquer um lê o
progresso de qualquer empresa.

Tudo que vem depois herda esse buraco. Pontuação, sequência de dias e ranking construídos
sobre identidade não verificada nascem fraudáveis. O cadastro do painel e o CRUD de
conteúdo não podem responder sem saber quem chama, sob pena de nascerem abertos na
internet. A trava não é de tela nem de regra de negócio: é a ausência de uma identidade
que o servidor consiga verificar.

Esta é a menor fatia que destrava o resto. Ela entrega as colunas que o painel escreve, a
cadeia de segurança que o papel administrativo apenas configura, e a identidade
autenticada de que o isolamento por empresa, a gamificação e o acompanhamento dependem.

## What Changes

A migration `V6` cria a credencial do integrante como hash de senha, o papel em coluna
própria com os valores `INTEGRANTE` e `ADMIN`, a marca de integrante ativo e a tabela
`sessao`, que guarda o hash do token opaco, a criação, a expiração, o último acesso e a
revogação. A mesma migration troca o índice único de e-mail por um índice único global
sobre o e-mail em minúsculas.

`POST /api/v1/sessoes` verifica a senha contra o hash guardado e devolve um token opaco
com a sua expiração, junto dos campos que a resposta já tem. `DELETE /api/v1/sessoes/atual`
revoga a sessão. Todo o resto da API exige `Authorization: Bearer`, com exceção da
emissão de sessão, do health e da documentação em desenvolvimento.

A leitura do conteúdo do treinamento passa a exigir token. O conteúdo é material do
cliente e continua idêntico para todas as empresas, mas deixa de responder para quem não
se identificou.

`usuario/IntegranteArgumentResolver` resolve o integrante a partir do contexto de
segurança em vez do cabeçalho. Nenhum controller e nenhuma rota são reescritos: é a troca
de mecanismo já prometida em `CLAUDE.md`.

`config/OpenApiConfig` ganha título, versão, servidores e a declaração do esquema de
segurança, sem a qual o Swagger não testa endpoint protegido e o time do app perde o
contrato de como mandar o token.

O seed de desenvolvimento dá credencial e papel às contas de exemplo, porque sem
credencial válida em banco nenhum teste de login roda.

Fora de escopo, cada um com a sua proposta: renovação de token, o painel administrativo
inteiro, bootstrap de administrador em produção, CRUD de conteúdo, tentativa de quiz,
gamificação, padronização de `Empresa` e perfil de teste próprio.

## Capabilities

### Modified Capabilities

- `sessao-integrante`: a resolução de integrante passa a verificar a senha e a emitir
  token de sessão revogável. O requisito que declara a resolução como não autenticação
  sai, junto do cenário que aceita qualquer senha.
- `progresso-trilha`: a identidade da requisição passa a vir de token verificado em vez
  de cabeçalho informado pelo cliente. Desbloqueio linear e leitura não mudam.
- `conteudo-trilha`: a leitura de conteúdo passa a exigir identidade autenticada.

## Impact

Nova migration `V6`. `V1` a `V5` não são tocadas, porque o Flyway valida o checksum do
que já rodou. `db/seed-dev/R__seed_empresa_exemplo.sql` é editado, porque é repetível e
recalcula o próprio checksum.

`pom.xml` ganha `spring-boot-starter-security`. O token é opaco e mora em tabela, então
nenhuma biblioteca de token entra junto.

`config` ganha `SecurityConfig` e a declaração do esquema de segurança no OpenAPI.
`usuario` ganha a entidade de sessão, o serviço que emite e revoga, e a resolução do
integrante a partir do contexto de segurança. `shared` ganha o tratamento de erro de
credencial inválida.

Mudança de contrato assumida, comunicada ao time do app no mesmo dia: senha errada passa
a ser recusada, e toda chamada sem `Authorization` passa a ser recusada. O cabeçalho
`X-Integrante-Id` deixa de ser lido, sem janela de convivência, porque aceitar os dois
mantém o buraco aberto e cria uma segunda verdade de quem é o integrante.

Os testes de integração existentes passam a autenticar. Dois cenários de sessão saem,
porque o comportamento que eles fixam é o que esta proposta remove.

Os limites do sistema em `CLAUDE.md` perdem a ausência de autenticação, o cabeçalho
forjável e a senha aceita sem verificação.
