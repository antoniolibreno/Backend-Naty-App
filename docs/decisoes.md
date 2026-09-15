# Decisões tomadas

Cada linha da antiga seção "Decisões em aberto" do `tasks.md` está resolvida aqui, com
a escolha, o motivo e o que ela muda no backlog. Decisão escrita aqui é premissa: mudar
uma delas obriga a revisar as tasks que ela libera.

## A integração com a Naty API saiu do projeto

O painel administrativo passa a ser a fonte da verdade de quem existe. O sistema não
consulta a Naty API para nada e não depende dela para subir. Consequências:

- Épicos 1 (Naty API V3) e 2 (Sincronização de integrantes) foram removidos do backlog,
  29 tasks.
- Nasce o épico 11 (Painel administrativo), com o cadastro de empresa, de integrante e
  de credencial, que é o que destrava o login.
- O épico 7 (Acompanhamento) fica para depois do MVP, porque depende de decidir quem
  enxerga mais de uma empresa.
- As decisões que existiam só por causa da integração morreram junto: paginação da Naty
  API, onde o token entra na requisição, o que fazer com `naty.api.token` e o que fazer
  com o integrante que desaparece da Naty.

A regra do `CLAUDE.md` raiz que diz "o sistema nunca cadastra usuário próprio: a fonte
da verdade de quem existe é o Naty App" deixa de valer. Ela precisa ser reescrita junto
com a implementação do épico 11, e o mesmo vale para a descrição dos pacotes `natyapi` e
`sincronizacao`. Enquanto isso não for feito, o documento raiz contradiz o backlog.

## Identidade e acesso

**Autenticação (3.1): credencial nossa, cadastrada pelo painel.** O painel cadastra o
integrante e define a senha, o backend guarda o hash e o login valida contra o nosso
banco.

**Qual empresa no login (3.1): e-mail único no sistema inteiro.** Como o cadastro é
nosso, a unicidade do e-mail é imposta no cadastro e o login resolve a empresa pela
credencial encontrada. O app não envia empresa e não existe tela de escolha. A mesma
pessoa em duas empresas precisa de dois e-mails, e isso é aceito.

**Token de sessão (3.5): token opaco em tabela.** Permite revogar sessão no logout e na
troca de senha, e a mesma linha guarda o último acesso, resolvendo a task 3.11 sem
escrever na tabela `usuario`. Token assinado sem estado não revoga e obrigaria uma
segunda tabela só para o último acesso.

**Papel administrativo (3.9): coluna nossa, na migration 11.1.** Dois papéis no MVP,
`INTEGRANTE` e `ADMIN`. A coluna `usuario.perfil`, que guarda `admin`, `supervisor` e
`user`, descreve o que a pessoa faz no WhatsApp e não vale como permissão no
treinamento. A task 3.9 deixou de existir separada, porque o painel já nasce com o papel.

**Integrante que saiu (2.13): coluna de ativo mais data de desativação.** O registro
nunca é apagado, porque progresso e pontuação apontam para ele. Integrante inativo some
do ranking e não consegue logar, e mantém o histórico. A desativação é manual, pelo
painel, na task 11.5.

## Conteúdo e quiz

**Tentativas de quiz (5.5 e 5.6): ilimitadas, vale a melhor nota.** É treinamento, não
certificação: o atrito de bloquear a retentativa custa mais do que a força bruta
economiza. O histórico completo é guardado, porque é ele que revela quem está travado.

**Gabarito depois da aprovação (5.7): revisão sem resposta certa.** A resposta da
tentativa diz a nota, se aprovou e quais perguntas errou, e nunca qual era a
alternativa correta. Entregar o gabarito faz ele circular entre colegas e esvazia o
quiz para todo mundo que vier depois.

**Hospedagem de vídeo (8.1): YouTube não listado.** Custo zero, player pronto no
Flutter e começa hoje. O preço é que link vazado é assistível por qualquer um. A
validação de URL da task 8.3 passa a recusar o que não for do domínio do YouTube.

## Gamificação

**Fuso da sequência (6.4): fuso da empresa.** Os carimbos são gravados em UTC e a
virada do dia é calculada no fuso do cliente, então a coluna de fuso em `empresa` nasce
na task 11.2, junto com o cadastro, e vira pré-requisito da 6.4.

**Ranking (6.8 e 6.10): calculado a cada consulta, acumulado desde sempre.** Empresa do
tamanho que a Naty atende cabe numa consulta indexada, e tabela materializada traz
invalidação sem ter resolvido problema nenhum ainda. Desempate por quem chegou primeiro
à pontuação, e paginação obrigatória desde a primeira versão.

## Operação

**Empresa nova em produção (2.10): pelo painel.** A mesma tela que cadastra integrante
cadastra empresa, com o fuso horário. Isso absorve a antiga task 2.16, que só existia
para a entidade `Empresa` conseguir ser persistida por código, e vira a task 11.2.

## Ainda em aberto

Só sobraram as duas que o épico 7 carrega, e ele está fora do MVP:

- **Quem enxerga mais de uma empresa (7.5):** o acompanhamento é o único lugar que
  atravessa empresas. Falta decidir se existe um papel acima de `ADMIN` e como essa
  pessoa entra no sistema.
- **Definição de parado (7.1):** quantos dias sem concluir atividade contam como parado.
  Quando o épico voltar, o número nasce como propriedade configurável, não como
  constante no código.
