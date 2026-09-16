# Etapa 3: progresso na trilha

## Why

O app Flutter já tem três telas prontas e nenhuma delas funciona de verdade contra o
backend. A tela de Login manda e-mail e senha, mas `POST /api/v1/sessoes` só aceita
e-mail e recusa o corpo com erro de validação. A Home estilo Duolingo precisa saber
qual atividade está bloqueada, qual está disponível e qual foi concluída, e nada disso
existe: não há tabela de progresso nem pacote que calcule estado. A tela de assistir
aula não tem para onde avisar que o integrante terminou o vídeo.

Hoje o sistema sabe qual é o conteúdo e sabe quem são os integrantes, mas não guarda
uma única linha sobre quem estudou o quê. Sem isso a trilha não avança, o app não
desenha a jornada e nada do que vem depois, pontuação e acompanhamento, tem em que se
apoiar.

## What Changes

Nasce o pacote `progresso`, previsto no `CLAUDE.md` raiz e até agora inexistente, com
a tabela `progresso_atividade` criada pela migration `V4`, uma linha por par de
integrante e atividade, guardando quando o vídeo foi assistido e quando a atividade foi
concluída.

A leitura da Home passa a existir em `GET /api/v1/trilhas/{trilhaId}/progresso`, que
devolve a trilha inteira com módulos e atividades em ordem, cada nó marcado como
bloqueado, disponível ou concluído, mais o total de atividades, quantas foram
concluídas, o percentual e qual é a próxima. É uma chamada só e a Home se desenha
inteira com ela.

A tela de aula passa a ter `POST /api/v1/atividades/{atividadeId}/video-assistido`, que
registra o vídeo assistido, conclui a atividade e libera a seguinte. A chamada é
idempotente e recusa atividade que ainda não abriu.

O desbloqueio é linear: a primeira atividade da trilha nasce disponível e cada atividade
seguinte só abre quando a anterior é concluída. Nesta etapa a conclusão vem do vídeo
assistido, porque a tentativa de quiz é de outra etapa e exigir aprovação agora travaria
a trilha no primeiro nó.

`POST /api/v1/sessoes` passa a receber senha além do e-mail, para a tela de Login já
mandar o corpo definitivo. A senha é exigida e descartada, nunca verificada, nunca
guardada e nunca registrada em log. Continua sem emitir credencial.

Enquanto não existe autenticação, cada chamada de progresso informa o integrante no
cabeçalho `X-Integrante-Id`, resolvido em um único ponto que injeta integrante e empresa
no controller. Quando a autenticação real entrar, só esse ponto muda: nenhuma rota e
nenhuma chamada do app são reescritas.

Fora de escopo, cada um com sua própria proposta: tentativa e correção de quiz,
pontuação, sequência de dias, conquistas e ranking, hospedagem de vídeo, autenticação
real e substituição do conteúdo de exemplo pelo verdadeiro.

## Capabilities

### New Capabilities

- `progresso-trilha`: estado de cada integrante em cada atividade, desbloqueio linear,
  leitura da trilha com progresso e registro de vídeo assistido.

### Modified Capabilities

- `sessao-integrante`: o corpo da resolução de integrante passa a exigir senha, aceita
  sem verificação, e a limitação passa a estar documentada também para a senha.

## Impact

Nova migration `V4__progresso_atividade.sql`. `V3` não é tocada, porque o Flyway valida
o checksum do que já rodou.

Pacote novo `progresso`, com seu próprio `CLAUDE.md`, contendo entidade, repositório,
serviço, montagem dos DTOs e um controller com os dois endpoints. O pacote lê `trilha` e
`usuario` e nenhum dos dois passa a conhecê-lo.

`usuario` ganha a resolução do integrante da requisição a partir do cabeçalho, e
`SessaoRequest` ganha o campo de senha. `config` ganha o registro do resolvedor.
`shared` ganha dois tratamentos de erro explícitos, para integrante não informado e para
atividade bloqueada, porque não existe tratamento genérico de propósito.

Mudança de contrato assumida: corpo de `POST /api/v1/sessoes` sem senha passa a ser
recusado com erro de validação. Os testes de sessão existentes são ajustados no mesmo
diff.

A dívida de ausência de autenticação cresce e fica registrada: senha aceita sem
verificação e cabeçalho de integrante forjável. Ambas caem na etapa de autenticação.
