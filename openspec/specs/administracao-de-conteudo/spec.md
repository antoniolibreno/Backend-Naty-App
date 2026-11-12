# administracao-de-conteudo Specification

## Purpose
Permite à conta NATY escrever o conteúdo de treinamento global (trilha, módulo e atividade),
com ordem, ativação, proteção do histórico de quem estudou e registro de quem alterou o quê.

## Requirements

### Requirement: Escrita de conteúdo restrita à conta NATY

Toda rota de escrita de trilha, módulo e atividade, e a leitura das alterações de conteúdo,
SHALL exigir token de sessão de conta `NATY`. Conta `ADMIN` e conta `INTEGRANTE` SHALL
receber 403 `ACESSO_NEGADO`, e chamada sem token SHALL receber 401 `CREDENCIAL_INVALIDA`. As
rotas de leitura do conteúdo e as rotas de progresso do integrante NÃO SHALL mudar de
exigência.

#### Scenario: Integrante tenta escrever

- **WHEN** uma conta `INTEGRANTE` ou `ADMIN` chama qualquer rota de escrita de conteúdo
- **THEN** o sistema responde 403 `ACESSO_NEGADO` e NÃO altera o conteúdo

#### Scenario: Sem token

- **WHEN** o cliente chama uma rota de escrita de conteúdo sem token
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`

#### Scenario: Rotas do integrante intactas

- **WHEN** um integrante registra vídeo assistido ou lê uma atividade
- **THEN** o sistema atende normalmente, sem exigir o papel `NATY`

### Requirement: Cadastro de trilha

`POST /api/v1/trilhas` SHALL criar uma trilha ativa com título e descrição, com ordem
acrescentada depois da última, e responder 201 com o identificador, o título, a descrição, a
ordem e a indicação de ativa. `PUT /api/v1/trilhas/{trilhaId}` SHALL alterar título e
descrição, e `DELETE /api/v1/trilhas/{trilhaId}` SHALL remover a trilha com seus módulos e
atividades e responder 204. O título SHALL ser obrigatório e ter no máximo 255 caracteres. Id
inexistente SHALL responder 404 `RECURSO_NAO_ENCONTRADO`.

#### Scenario: Trilha criada

- **WHEN** a conta `NATY` cria uma trilha com título válido
- **THEN** o sistema responde 201 com a trilha ativa e a lista de trilhas a inclui

#### Scenario: Título ausente

- **WHEN** a conta `NATY` cria uma trilha sem título
- **THEN** o sistema responde 400 `FALHA_DE_VALIDACAO`

#### Scenario: Trilha inexistente

- **WHEN** a conta `NATY` altera ou remove uma trilha que não existe
- **THEN** o sistema responde 404 `RECURSO_NAO_ENCONTRADO`

### Requirement: Cadastro de módulo e atividade

`POST /api/v1/modulos` SHALL criar um módulo em uma trilha existente, e
`POST /api/v1/atividades` SHALL criar uma atividade em um módulo existente, ambos com ordem
acrescentada depois da última do pai. `PUT` e `DELETE` em `/api/v1/modulos/{moduloId}` e em
`/api/v1/atividades/{atividadeId}` SHALL alterar e remover, com 200 e 204. A atividade SHALL
aceitar título, descrição, imagem, vídeo, duração em segundos e XP. O vídeo SHALL ser URL
HTTPS do YouTube, a duração SHALL ser positiva quando há vídeo e SHALL ser ausente quando não há
vídeo, e o XP SHALL ser inteiro não
negativo e valer 10 quando omitido. Pai inexistente e id inexistente SHALL responder 404
`RECURSO_NAO_ENCONTRADO`. Violação de campo SHALL responder 400 `FALHA_DE_VALIDACAO`.

#### Scenario: Atividade com vídeo

- **WHEN** a conta `NATY` cria uma atividade com vídeo do YouTube e duração positiva
- **THEN** o sistema responde 201 e a leitura da atividade devolve o vídeo e a duração

#### Scenario: Vídeo de outro domínio

- **WHEN** a conta `NATY` cria ou altera uma atividade com vídeo fora do YouTube
- **THEN** o sistema responde 400 `FALHA_DE_VALIDACAO` e NÃO grava a atividade

#### Scenario: Vídeo sem duração

- **WHEN** a conta `NATY` cria uma atividade com vídeo e sem duração positiva
- **THEN** o sistema responde 400 `FALHA_DE_VALIDACAO`

#### Scenario: Duração sem vídeo

- **WHEN** a conta `NATY` cria uma atividade com duração e sem vídeo
- **THEN** o sistema responde 400 `FALHA_DE_VALIDACAO`

#### Scenario: XP padrão

- **WHEN** a conta `NATY` cria uma atividade sem informar o XP
- **THEN** a atividade nasce com XP 10

#### Scenario: Módulo em trilha inexistente

- **WHEN** a conta `NATY` cria um módulo em uma trilha que não existe
- **THEN** o sistema responde 404 `RECURSO_NAO_ENCONTRADO`

### Requirement: Ordem por reordenação

A criação de módulo e de atividade SHALL acrescentar o item depois do último irmão. A
reordenação SHALL receber a lista completa dos identificadores dos irmãos na ordem desejada:
`PUT /api/v1/trilhas/{trilhaId}/modulos/ordem` para os módulos e
`PUT /api/v1/modulos/{moduloId}/atividades/ordem` para as atividades, com resposta 204. A
lista SHALL conter exatamente os irmãos atuais, sem falta, sem sobra e sem repetição, e a
ordem resultante SHALL ser 1 a n sem lacuna. Lista diferente SHALL responder 409
`ORDEM_INVALIDA` e NÃO SHALL alterar a ordem.

#### Scenario: Troca de posição

- **WHEN** a conta `NATY` reordena dois módulos invertendo a posição
- **THEN** a leitura da trilha devolve os módulos na ordem nova

#### Scenario: Lista incompleta

- **WHEN** a conta `NATY` envia uma lista sem um dos irmãos
- **THEN** o sistema responde 409 `ORDEM_INVALIDA` e a ordem fica como estava

#### Scenario: Identificador de outro pai

- **WHEN** a conta `NATY` envia o identificador de uma atividade de outro módulo
- **THEN** o sistema responde 409 `ORDEM_INVALIDA`

#### Scenario: Criação no fim

- **WHEN** a conta `NATY` cria uma atividade em um módulo com atividades
- **THEN** a atividade nova aparece depois das existentes

### Requirement: Ativação de trilha

`PUT /api/v1/trilhas/{trilhaId}/ativa` SHALL ativar ou desativar a trilha. A trilha inativa
NÃO SHALL aparecer em `GET /api/v1/trilhas`, e o progresso, a tentativa e o ponto de quem a
estudou NÃO SHALL ser apagados nem alterados.

#### Scenario: Desativar trilha

- **WHEN** a conta `NATY` desativa uma trilha
- **THEN** a listagem de trilhas não a inclui e o progresso dos integrantes permanece
  gravado

#### Scenario: Reativar trilha

- **WHEN** a conta `NATY` reativa uma trilha desativada
- **THEN** a listagem volta a incluí-la

### Requirement: Conteúdo com histórico não é removido

O sistema SHALL recusar com 409 `CONTEUDO_COM_PROGRESSO` a remoção de trilha, módulo ou
atividade quando alguma atividade atingida tem progresso, tentativa de quiz ou ponto
registrado, e NÃO SHALL remover nada nesse caso. A mensagem SHALL indicar a desativação da
trilha como alternativa. A remoção de conteúdo sem histórico SHALL remover também os itens
abaixo dele.

#### Scenario: Atividade com progresso

- **WHEN** a conta `NATY` remove uma atividade que um integrante já estudou
- **THEN** o sistema responde 409 `CONTEUDO_COM_PROGRESSO` e a atividade continua existindo

#### Scenario: Trilha com atividade estudada

- **WHEN** a conta `NATY` remove uma trilha que tem uma atividade com progresso
- **THEN** o sistema responde 409 `CONTEUDO_COM_PROGRESSO` e a trilha, os módulos e as
  atividades continuam existindo

#### Scenario: Conteúdo sem histórico

- **WHEN** a conta `NATY` remove uma trilha que ninguém estudou
- **THEN** o sistema responde 204 e a trilha, os módulos e as atividades somem

### Requirement: Registro das alterações de conteúdo

Toda criação, alteração, remoção, ativação, desativação e reordenação de conteúdo SHALL
gravar, na mesma transação da alteração, a conta que a fez, a entidade, o identificador, a
ação e o momento. A alteração recusada NÃO SHALL gerar registro. `GET /api/v1/alteracoes-de-conteudo`
SHALL listar os registros em página, do mais recente para o mais antigo, com filtro opcional
por `entidadeId`.

#### Scenario: Alteração registrada

- **WHEN** a conta `NATY` altera o XP de uma atividade
- **THEN** a listagem de alterações traz um registro com a conta, a entidade `ATIVIDADE`, o
  identificador, a ação `ALTERACAO` e o momento

#### Scenario: Filtro por entidade

- **WHEN** a conta `NATY` lista as alterações com `entidadeId` de uma atividade
- **THEN** a resposta traz só os registros dessa atividade

#### Scenario: Alteração recusada

- **WHEN** a conta `NATY` tenta uma alteração que o sistema recusa
- **THEN** nenhum registro é gravado

#### Scenario: Remoção registrada

- **WHEN** a conta `NATY` remove uma atividade sem histórico
- **THEN** o registro com a ação `REMOCAO` continua na listagem depois da remoção
