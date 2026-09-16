# Regras do produto

Cada regra aqui é premissa de desenho: o código a obedece, e mudar uma delas obriga a
revisar tudo o que ela sustenta. A regra vem com o motivo, porque motivo esquecido vira
regra revogada por engano.

Os limites do sistema estão no `CLAUDE.md` da raiz. O que falta construir está em
`backlog.md`.

## Identidade e acesso

**A credencial é nossa, cadastrada pelo painel.** O painel cadastra o integrante e
define a senha, o backend guarda o hash e o login valida contra o nosso banco. Nenhum
serviço externo responde por quem existe.

**O e-mail é único no sistema inteiro.** A unicidade é imposta no cadastro, e o login
resolve a empresa pela credencial encontrada. O app não envia empresa e não existe tela
de escolha. A mesma pessoa em duas empresas precisa de dois e-mails.

**O token de sessão é opaco e mora em tabela.** Isso permite revogar sessão no logout e
na troca de senha, e a mesma linha guarda o último acesso. Token assinado sem estado não
revoga e exigiria uma segunda tabela só para o último acesso.

**O último acesso do integrante mora na tabela de sessão, nunca em `usuario`.** Só o
painel escreve em `usuario`, e a regra não abre exceção para um carimbo de data.

**Os papéis são `INTEGRANTE` e `ADMIN`, em coluna própria.** A coluna `usuario.perfil`,
que guarda `admin`, `supervisor` e `user`, descreve o que a pessoa faz no WhatsApp e não
vale como permissão no treinamento.

**Integrante desativado mantém o histórico.** O registro nunca é apagado, porque
progresso e pontuação apontam para ele. Desativado não entra no sistema e some do
ranking, e o progresso dele continua contando para o acompanhamento.

**O tempo de expiração da sessão e os parâmetros de hash de senha vivem em variável de
ambiente.** A aplicação recusa subir em produção sem eles, para não existir valor padrão
de segurança em repositório. O token é opaco e não é assinado, então não existe chave de
assinatura.

## Isolamento por empresa

**Todo dado de pessoa é filtrado pela empresa de quem chamou.** Integrante, progresso,
pontuação e ranking. A empresa vem da identidade da requisição, nunca do corpo nem da
URL, onde o cliente poderia trocá-la.

**O acompanhamento é o único lugar que atravessa empresas**, e só o papel administrativo
entra nele.

**O conteúdo de treinamento é global.** Nenhuma tabela de conteúdo tem `empresa_id`,
porque todas as empresas fazem o mesmo treinamento sobre o mesmo produto. Conteúdo
exclusivo, se existir, entra como tabela de associação entre empresa e trilha, sem
alterar o que já existe.

## Conteúdo e quiz

**O gabarito não sai em leitura.** O DTO de alternativa não tem o campo de resposta
correta: um campo que não existe no record não vaza, enquanto uma anotação de ocultação
esquecida em campo novo vaza em silêncio. O DTO de escrita, que carrega o gabarito, é um
record separado do DTO de leitura.

**A tentativa de quiz é ilimitada e vale a melhor nota.** É treinamento, não
certificação: o atrito de bloquear a retentativa custa mais do que a força bruta
economiza.

**O histórico completo de tentativas é guardado**, não só a última, porque é ele que
revela quem está travado em uma atividade.

**A correção acontece só no servidor**, comparando as respostas com a coluna
`alternativa.correta`. Nota enviada pelo cliente não existe como campo de entrada.

**A revisão não revela a alternativa correta.** O resultado diz a nota, se aprovou e
quais perguntas errou. Entregar o gabarito faz ele circular entre colegas e esvazia o
quiz para todo mundo que vier depois.

**A nota mínima é lida de `quiz.nota_minima`**, nunca de constante em Java. Duplicar
esse valor em código cria duas verdades.

**Cada pergunta tem exatamente uma alternativa correta.** Zero ou duas quebram a
correção, e a regra vale na escrita, não só no conteúdo semeado.

**Conteúdo com progresso registrado não é removido.** Apagar atividade que já tem
progresso ou tentativa joga fora o histórico de quem estudou: o caminho é desativar.
Trilha inativa some da listagem, e o progresso de quem a fez continua contando.

## Trilha e progresso

**O desbloqueio é linear.** A primeira atividade da sequência nasce disponível, e cada
atividade seguinte abre quando a anterior é concluída. A sequência é a ordem do módulo
e, dentro dele, a ordem da atividade.

**Progresso em atividade bloqueada é recusado com conflito**, nunca aceito em silêncio.
Sem essa recusa o app pula a trilha inteira.

**O estado é calculado a cada leitura, não guardado.** A tabela guarda fatos: quando o
vídeo foi assistido e quando a atividade foi concluída. Estado guardado é uma segunda
verdade que precisa ser reescrita em cascata a cada conclusão e que fica errada em
silêncio quando o conteúdo é reordenado.

**A ordem é coluna explícita, com índice único no par pai mais ordem.** Ordem implícita
por data de criação ou por identificador quebra no primeiro conteúdo reordenado, e
reordenar é operação própria, para não colidir com o índice.

## Vídeo

**O vídeo fica no YouTube, como não listado.** Custo zero e player pronto no Flutter. O
preço é que link vazado é assistível por qualquer um, e esse preço está aceito.

**A URL de vídeo é validada pelo domínio de hospedagem.** Fora dele, recusada, para o app
não receber link quebrado ou de origem inesperada.

## Gamificação

**O ponto vem de `atividade.xp`**, concedido na conclusão da atividade. Não existe outra
fonte de pontuação.

**O ponto é concedido uma única vez por atividade.** Concluir de novo, ou refazer um quiz
já aprovado, não soma de novo, senão o ranking vira farsa.

**A sequência de dias é calculada no fuso da empresa.** Os carimbos são gravados em UTC,
e a virada do dia acontece no fuso do cliente. Sem isso o mesmo integrante ganha ou perde
sequência dependendo do servidor.

**As conquistas são conteúdo global**, iguais para todas as empresas, e entram por
migration como o resto do conteúdo. Cada conquista é concedida uma única vez por
integrante.

**O ranking é calculado a cada consulta, acumulado desde sempre.** Empresa do tamanho que
a Naty atende cabe numa consulta indexada, e tabela materializada traz invalidação sem
ter resolvido problema nenhum. O desempate é por quem chegou primeiro à pontuação, e a
paginação é obrigatória.

**Ranking entre empresas não existe.**

## Consumidores

**O app Flutter tem Login, Home no estilo Duolingo e tela de assistir aula.** São as três
telas que consomem a API, e o contrato delas não muda sem aviso ao time do app: resolução
de sessão, leitura da trilha com progresso e registro de vídeo assistido.

**O painel administrativo é a fonte da verdade de quem existe.** Empresa, integrante e
credencial nascem lá, e nenhum outro caminho cadastra pessoa no sistema.

## Escrita e schema

**Só o pacote `painel` escreve em `usuario`.** Concentrar a escrita em um lugar é o que
mantém a regra auditável.

**O schema do banco é do Flyway.** `ddl-auto` fica em `validate`: adicionar campo em
entidade sem a migration correspondente derruba a aplicação na subida, e isso é
proposital.

**Migration aplicada não é editada nem renomeada.** O Flyway valida o checksum e a
descrição do que já rodou. Correção de conteúdo semeado entra em migration nova.

**Senha, hash de senha e token de sessão nunca aparecem em log nem em mensagem de erro.**

**Listagem é paginada.** Ranking, integrantes e acompanhamento crescem com a empresa, e
a primeira empresa grande não pode descobrir isso em produção.
