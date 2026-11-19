# Design: identidade e sessão autenticada

## Context

O conteúdo do treinamento, a resolução de integrante e o progresso com desbloqueio linear
estão construídos e cobertos por testes de integração. O que falta é o servidor saber
quem chama.

A identidade chega de duas formas, e nenhuma vale como prova. `POST /api/v1/sessoes`
recebe e-mail e senha, resolve pelo e-mail e descarta a senha sem compará-la com nada. As
operações de progresso leem o cabeçalho `X-Integrante-Id`, que o cliente monta com
qualquer identificador.

`docs/regras.md` já fixa o mecanismo: a credencial é nossa, cadastrada pelo painel, o
backend guarda o hash e o login valida contra o nosso banco. O token de sessão é opaco e
mora em tabela, para permitir revogação no logout e na troca de senha. Esta proposta
constrói esse mecanismo e não o redecide.

O app Flutter tem Login, Home no estilo Duolingo e tela de assistir aula, e as três
consomem a API. O corpo do Login já manda e-mail e senha, então a tela não muda de
formato: muda o que acontece quando a senha está errada.

## Goals / Non-Goals

**Goals:**

- A senha ser verificada contra hash guardado no nosso banco.
- Cada chamada autenticada saber o integrante e a empresa sem que o cliente os informe.
- A sessão ser revogável, e a revogação valer na chamada seguinte.
- O isolamento por empresa deixar de depender de boa fé do cliente.
- O Swagger continuar utilizável contra endpoint protegido.
- Nenhuma rota e nenhum corpo de resposta existentes mudarem de formato.

**Non-Goals:**

- Renovação de token e sessão de vida longa.
- Cadastro de empresa, cadastro de integrante e reset de senha pelo painel.
- Bootstrap de administrador em produção.
- Papel administrativo protegendo rota, porque nenhuma rota administrativa existe.
- Tentativa de quiz, gamificação e acompanhamento.

## Decisions

### Token opaco em tabela, e não token assinado

`docs/regras.md` escolhe o token opaco pela revogação: token assinado sem estado não
revoga, e exigiria uma segunda tabela só para guardar o último acesso. A consequência que
esta proposta assume é que toda chamada autenticada faz uma leitura na tabela `sessao`,
com índice único sobre o hash do token.

O token não é assinado, então não existe chave de assinatura. O que vive em variável de
ambiente é o tempo de expiração da sessão e os parâmetros de hash de senha.

O valor guardado na tabela é o hash do token, não o token. Vazamento do banco não vira
sessão ativa.

### Corte seco do cabeçalho, sem janela de convivência

`X-Integrante-Id` deixa de ser lido no mesmo diff em que `Authorization` passa a ser
exigido. Aceitar os dois mantém o buraco aberto pelo tempo da janela e cria uma segunda
verdade sobre quem é o integrante, que alguém acaba usando.

O app não está em produção. O custo é combinar a data com o time do Flutter, e isso é
tarefa desta proposta.

### A leitura de conteúdo exige token

O conteúdo é material do cliente e não fica aberto na internet. Ele continua global e
idêntico para todas as empresas: o que muda é que o servidor exige identidade antes de
responder, não que a resposta varie por quem pergunta.

A consequência para o app é que nenhuma tela carrega conteúdo antes do login.

### O e-mail passa a ser único no sistema inteiro

O índice único sobre o par de empresa e e-mail permite o mesmo e-mail em duas empresas, e
nesse caso o login não sabe qual empresa resolver sem o app informar uma, o que
`docs/regras.md` proíbe. O índice único global sobre o e-mail em minúsculas é o que
sustenta o login sem tela de escolha.

A mesma pessoa em duas empresas precisa de dois e-mails. A migration falha alto se já
existir e-mail repetido entre empresas, e não deduplica em silêncio.

### O papel mora em coluna própria, separado de `usuario.perfil`

`usuario.perfil` guarda `admin`, `supervisor` e `user` e descreve o que a pessoa faz no
WhatsApp. Usá-lo como permissão no treinamento amarra autorização a um dado que o painel
da Naty controla por outro motivo. A coluna nova guarda `INTEGRANTE` e `ADMIN`.

A coluna `usuario.status` guarda status de WhatsApp e não serve para dizer se o
integrante entra. A marca de ativo é coluna própria.

### Administrador inicial nasce do seed, nunca de rota aberta

Nenhuma rota de escrita de integrante entra nesta proposta. Em desenvolvimento a conta
administrativa vem do seed repetível. Em produção o caminho é bootstrap por variável de
ambiente, e ele pertence à proposta do painel.

## Risks / Trade-offs

Todos os testes de integração passam a precisar de token, então eles quebram juntos e a
proposta não é revisável em pedaços. É consequência direta do corte seco, e o preço de
não manter duas identidades válidas ao mesmo tempo.

`ProgressoApiTest` e `SessaoApiTest` criam integrante direto pelo repositório. As colunas
novas nascem com valor padrão no banco para essa escrita continuar válida, e os testes
passam a criar integrante com senha conhecida.

A leitura na tabela `sessao` a cada chamada é custo por requisição. O índice único sobre
o hash do token e a ausência de qualquer varredura mantêm o custo em uma busca por
chave.

## Migration Plan

A migration vem antes da entidade, sempre. `ddl-auto` está em `validate`, e campo em
entidade sem coluna no banco derruba a subida com erro que não aponta para a causa.

`V6` cria as colunas de `usuario` com valor padrão, porque o seed repetível roda depois
das migrations versionadas: coluna obrigatória sem padrão quebra a subida antes de o seed
ter chance de preencher.

Validar contra banco limpo. `baseline-on-migrate` está ligado e mascara divergência de
histórico num banco de desenvolvimento já migrado.

## Open Questions

Nenhuma. O mecanismo está fixado em `docs/regras.md` e as decisões desta proposta estão
acima.
