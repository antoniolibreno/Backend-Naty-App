# Proposal

## Why

O quiz não tem administração pela API, e é onde o gabarito
mora. Uma pergunta com zero ou duas alternativas corretas quebra a correção, e um DTO único
para leitura e escrita é o jeito de vazar o gabarito por esquecimento. A escrita precisa de
regras de integridade, de um canal de leitura do gabarito só para quem administra e de
registro de quem o alterou.

## What Changes

- CRUD de quiz, pergunta e alternativa no pacote `conteudo`, restrito ao papel `NATY`.
- `GET /api/v1/quizzes/{quizId}`: leitura administrativa do quiz com o gabarito, único canal em
  que `correta` sai da API.
- Dois contratos separados para a alternativa: a escrita carrega `correta`, a leitura do
  integrante não tem o campo, e a leitura administrativa tem o campo em tipo próprio.
- Toda pergunta tem exatamente uma alternativa correta, em toda escrita e em toda transição de
  estado.
- Remoção de quiz, pergunta e alternativa recusada quando há histórico de tentativa.
- Toda alteração de quiz entra no registro de alterações de conteúdo.

## Capabilities

### New Capabilities

- `administracao-de-quiz`: escrita de quiz, pergunta e alternativa, integridade do gabarito e
  leitura administrativa do gabarito.

### Modified Capabilities

## Impact

- Pacote `conteudo` (controllers, serviço, DTOs), `config/SecurityConfig.java`,
  `RegrasDeDependenciaTest`.
- `trilha/CLAUDE.md`, `conteudo/CLAUDE.md`, `docs/regras.md` e `docs/backlog.md`: itens 9.4,
  9.5, 9.6, 9.9 e 9.11.
- Sem migration e sem dependência nova. Depende da change `administracao-de-conteudo`.
