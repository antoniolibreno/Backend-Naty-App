# Proposal

## Why

O conteúdo de treinamento só muda por migration, então trocar um título, ajustar o XP de uma
atividade ou publicar uma trilha nova exige um deploy. O conteúdo é global e vale para todas as
empresas, por isso a escrita precisa ser restrita à conta `NATY` e rastreável.

## What Changes

- Pacote `conteudo`, único a escrever em `trilha`, `modulo` e `atividade`, com rotas de
  escrita restritas ao papel `NATY`.
- CRUD de trilha, módulo e atividade, com validação de vídeo do YouTube, duração e XP.
- Ordem definida por reordenação: criação acrescenta ao fim, e a reordenação troca a ordem de
  todos os irmãos de uma vez, sem colidir com o índice único.
- Ativação e desativação de trilha. Trilha inativa some da listagem e mantém o progresso.
- Remoção recusada para conteúdo com progresso, tentativa ou ponto registrado.
- Registro de quem alterou o conteúdo e quando, com leitura paginada para a `NATY`.

## Capabilities

### New Capabilities

- `administracao-de-conteudo`: escrita de trilha, módulo e atividade, ordem, ativação,
  proteção do histórico e auditoria das alterações, tudo restrito à conta `NATY`.

### Modified Capabilities

## Impact

- Pacote novo `conteudo` com `CLAUDE.md`, nova migration em `db/migration`,
  `config/SecurityConfig.java`, `RegrasDeDependenciaTest`.
- `trilha/CLAUDE.md`, `CLAUDE.md` raiz, `docs/regras.md` e `docs/backlog.md`: itens 9.2, 9.3,
  9.7, 9.8 e 9.10, e a parte de trilha, módulo e atividade de 9.9 e 9.11.
- Sem dependência nova no `pom.xml`. O quiz fica para a change `administracao-de-quiz`.
