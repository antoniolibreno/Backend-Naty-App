# Design

## Modelo

A tabela `sessao` ganha `iniciada_em timestamptz not null default now()`, preenchida nas
linhas existentes com `criado_em`. Login grava `iniciada_em = criado_em`. A renovação cria uma
linha nova e copia `iniciada_em` da linha anterior, então a duração máxima conta sempre do
login e não da última renovação. Revogação em lote continua alcançando a cadeia porque age
por integrante e por empresa.

Cada renovação é uma sessão nova com token novo, e não a extensão da mesma linha. Isso
mantém o token em claro existindo uma única vez, na resposta, e preserva o hash único por
linha.

## Endpoint

`SessaoController` ganha `POST /api/v1/sessoes/atual/renovacao`. Só `POST /api/v1/sessoes` é
público na cadeia de segurança, então a rota nova cai em `anyRequest().authenticated()`, e o
`TokenSessaoFiltro` já valida o token e o integrante antes do controller. O controller lê o
cabeçalho `Authorization` como `DELETE /atual` faz.

## Serviço

`SessaoService.renovar(token)`, transacional:

1. Busca a sessão por hash do token com `buscarComIntegrantePorTokenHash`, e exige
   `estaValidaEm(agora)` e `usuario.podeEntrar()`. Falha lança `CredencialInvalidaException`.
2. Exige `agora < iniciadaEm + duracaoMaxima`. Falha lança a mesma exceção.
3. Revoga o token apresentado com `revogarPorTokenHash`, que é um update condicionado a
   `revogado_em is null`. Retorno zero significa que outra renovação ou revogação chegou
   antes, e lança a mesma exceção. Esse update é o que garante no máximo um token novo em
   renovações concorrentes, sem trava de linha.
4. Cria a sessão nova com `expira_em = min(agora + expiracao, iniciada_em + duracaoMaxima)`,
   `iniciada_em` herdado e `ultimo_acesso_em = agora`, e devolve `SessaoResponse`.

Toda recusa usa a mesma mensagem fixa, para a resposta não distinguir expirado, revogado e
inexistente.

## Configuração

`app.sessao.duracao-maxima` lê `APP_SESSAO_DURACAO_MAXIMA`, padrão `30d` fora de produção e
sem padrão em `application-prod.yml`. `.env.example` ganha a variável. O padrão de 30 dias
iguala o prazo de retenção de sessão e é muito maior que os 7 dias de expiração.

Decisão: duração máxima existe. Sem ela, renovar indefinidamente mantém a sessão eterna e
anula a revogação por prazo.

## Riscos

- Renovação cria uma linha por chamada. A limpeza por retenção já remove as encerradas.
- Token vazado renova até a duração máxima. A revogação por troca de senha e desativação
  continua cortando a cadeia inteira.
