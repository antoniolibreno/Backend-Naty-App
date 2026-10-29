# ranking-empresa Specification

## Purpose
Classifica as contas ativas de uma empresa pelos pontos acumulados desde sempre, para o app
mostrar quem lidera o treinamento entre colegas, sem nunca comparar empresas.

## Requirements

### Requirement: Ranking da empresa de quem chamou

`GET /api/v1/ranking` SHALL exigir token de sessão válido e SHALL classificar as contas
ativas da empresa do dono do token, de qualquer papel, inclusive as que não têm ponto. A
empresa SHALL vir da identidade da requisição e NÃO SHALL ser aceita do cliente por
parâmetro, corpo ou cabeçalho. Ranking entre empresas NÃO SHALL existir.

#### Scenario: Conta sem ponto aparece

- **WHEN** uma empresa tem um integrante com 20 pontos e um `ADMIN` sem ponto
- **THEN** o ranking traz os dois, o integrante em primeiro e o `ADMIN` em segundo

#### Scenario: Integrante de outra empresa não aparece

- **WHEN** o integrante consulta o ranking e outra empresa tem integrante com mais pontos
- **THEN** o ranking traz somente contas da empresa do dono do token

#### Scenario: Desativado some do ranking

- **WHEN** um integrante com pontos é desativado
- **THEN** ele não aparece no ranking da empresa, e os pontos dele continuam gravados

#### Scenario: Sem token

- **WHEN** o cliente consulta o ranking sem token
- **THEN** o sistema responde 401 `CREDENCIAL_INVALIDA`

### Requirement: Ordem e desempate do ranking

O ranking SHALL ordenar pelo total de pontos, do maior para o menor. No empate, SHALL vir
antes quem alcançou o próprio total primeiro, medido pelo instante da última concessão de
ponto. Contas sem ponto SHALL vir depois de todas as que têm ponto, por nome, e o
identificador SHALL desempatar o que restar.

#### Scenario: Mais pontos primeiro

- **WHEN** um integrante tem 30 pontos e outro tem 20
- **THEN** o de 30 pontos vem antes

#### Scenario: Empate por quem chegou primeiro

- **WHEN** dois integrantes têm 20 pontos e um deles recebeu a última concessão antes do
  outro
- **THEN** quem recebeu a última concessão antes vem antes

#### Scenario: Sem ponto por nome

- **WHEN** duas contas sem ponto se chamam Bruna e Ana
- **THEN** Ana vem antes de Bruna, e as duas vêm depois de todas as contas com ponto

### Requirement: Posição no ranking

Cada item do ranking SHALL trazer a posição, o identificador do integrante, o nome e o
total de pontos. A posição SHALL ser ordinal, sem repetição, começando em 1 na primeira
página e seguindo a numeração nas páginas seguintes.

#### Scenario: Posição na segunda página

- **WHEN** o cliente pede a segunda página com tamanho 2
- **THEN** os itens trazem as posições 3 e 4

#### Scenario: Empate não repete posição

- **WHEN** dois integrantes têm o mesmo total de pontos
- **THEN** eles ocupam posições diferentes, na ordem do desempate

### Requirement: Ranking paginado

O ranking SHALL ser sempre paginado pelos parâmetros `pagina` e `tamanho`, com tamanho
padrão 20 e máximo 100, e SHALL devolver o corpo único de listagem com `itens`, `pagina`,
`tamanho`, `totalItens` e `totalPaginas`. A ordem enviada pelo cliente SHALL ser ignorada.

#### Scenario: Sem parâmetros

- **WHEN** o cliente consulta o ranking sem `pagina` e sem `tamanho`
- **THEN** o sistema devolve a primeira página com tamanho 20

#### Scenario: Tamanho acima do máximo

- **WHEN** o cliente pede `tamanho` 500
- **THEN** o sistema devolve a página com tamanho 100

#### Scenario: Ordem do cliente ignorada

- **WHEN** o cliente envia `sort=nome,asc`
- **THEN** o ranking mantém a ordem por pontos e desempate
