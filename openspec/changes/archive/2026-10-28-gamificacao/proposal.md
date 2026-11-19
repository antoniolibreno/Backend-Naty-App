# Gamificação

## Why

O treinamento se apresenta como estilo Duolingo, mas o integrante conclui atividade sem
receber ponto, sem sequência de dias, sem conquista e sem lugar no ranking da empresa.
`atividade.xp` é lido e nada o soma. As regras de gamificação estão fixadas em
`docs/regras.md`, e a conclusão de atividade tem gatilho estável (tentativa aprovada ou
vídeo assistido), então a pontuação tem onde se apoiar.

## What Changes

- Pacote novo `gamificacao`, com o seu `CLAUDE.md`, que reage aos fatos de estudo do
  `progresso` sem que o `progresso` dependa dele.
- O `progresso` publica um fato de estudo a cada tentativa de quiz, a cada primeiro
  registro de vídeo assistido e a cada primeira conclusão de atividade. O contrato HTTP
  do `progresso` não muda.
- Concessão de `atividade.xp` na primeira conclusão de cada atividade, uma única vez por
  integrante e atividade, com o valor congelado no momento da concessão.
- Sequência de dias de estudo calculada no fuso da empresa. Conta como dia de estudo o dia
  com tentativa de quiz, vídeo assistido ou conclusão de atividade.
- Catálogo global de conquistas semeado por migration, com critério e limiar por linha, e
  concessão única por integrante. Catálogo inicial: primeira atividade concluída, quiz com
  nota 100, módulo concluído, trilha concluída, sequência de 3 dias, sequência de 7 dias e
  50 pontos.
- `GET /api/v1/ranking`: classificação paginada das contas ativas da empresa de quem
  chamou, de qualquer papel e inclusive sem ponto, com desempate por quem chegou primeiro
  à pontuação.
- `GET /api/v1/integrantes/{integranteId}/gamificacao`: pontos, posição no ranking,
  sequência atual, maior sequência e catálogo de conquistas com o instante de obtenção, para
  o próprio integrante e para colega ativo da mesma empresa.
- Migration que cria as tabelas, carrega pontos e dias de estudo a partir do progresso
  registrado e semeia o catálogo de conquistas.
- `shared/util/DataUtil` sai do projeto: o cálculo de data da sequência mora em
  `gamificacao`, único consumidor.
- Documentação: limites do sistema no `CLAUDE.md` raiz, `README.md`, `shared/CLAUDE.md`,
  `progresso/CLAUDE.md` e `docs/backlog.md`.

## Capabilities

### New Capabilities

- `gamificacao-integrante`: pontos por conclusão de atividade, sequência de dias de estudo
  no fuso da empresa, concessão de conquistas e leitura do perfil de gamificação de um
  integrante.
- `ranking-empresa`: classificação dos integrantes ativos dentro da empresa de quem chamou,
  paginada e com desempate determinístico.

### Modified Capabilities

Nenhuma. A conclusão de atividade e o contrato das rotas de progresso ficam como estão; a
gamificação observa os mesmos fatos.

## Impact

- Código: pacote novo `gamificacao`; `progresso/ProgressoService` publica o fato de
  estudo; `progresso` ganha o record do fato e consultas de contagem de conclusão;
  `usuario/UsuarioRepository` pode ganhar busca de conta ativa por empresa.
- Banco: migrations `V10` (tabelas `ponto_concedido`, `dia_de_estudo`, `conquista`,
  `conquista_obtida` e carga a partir do progresso) e `V11` (catálogo de conquistas). Toda
  FK para `usuario` com `on delete cascade`.
- API: duas rotas de leitura autenticadas, sem papel exigido. Rotas existentes inalteradas.
- App Flutter: consome ranking e perfil. A celebração de ponto e conquista após uma
  conclusão vem da leitura do perfil.
- Dependências: nenhuma nova no `pom.xml`.
