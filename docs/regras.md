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

**O CNPJ é único entre as empresas.** Toda empresa cadastrada pelo painel tem CNPJ e razão
social, e o CNPJ é guardado normalizado, numérico ou alfanumérico, com os dígitos
verificadores conferidos. O responsável pela empresa é a conta `ADMIN` dela, com telefone
informado no cadastro, e o contratante é essa mesma pessoa.

**O token de sessão é opaco e mora em tabela.** Isso permite revogar sessão no logout e
na troca de senha, e a mesma linha guarda o último acesso. Token assinado sem estado não
revoga e exigiria uma segunda tabela só para o último acesso.

**O último acesso do integrante mora na tabela de sessão, nunca em `usuario`.** Só o
painel escreve em `usuario`, e a regra não abre exceção para um carimbo de data.

**Os papéis são `INTEGRANTE`, `ADMIN` e `NATY`, em coluna própria e sem hierarquia.**
`ADMIN` administra as contas `INTEGRANTE` da própria empresa. `NATY` é o time da Naty e
administra empresas e as contas `INTEGRANTE` e `ADMIN` de qualquer uma. A coluna `usuario.perfil`, que guarda `admin`, `supervisor` e
`user`, descreve o que a pessoa faz no WhatsApp e não vale como permissão no treinamento.

**`NATY` nasce só do bootstrap ou do seed, nunca de endpoint.** Um papel que atravessa
empresas atribuível pela API é escalada de privilégio a um `PUT` de distância. O primeiro
`NATY` de produção vem de variável de ambiente, e produção recusa subir sem nenhum.

**Conta `ADMIN` é administrada só pelo `NATY`, e conta `NATY` não é administrada pelo
painel.** Um `ADMIN` que redefine a senha de outro assume a conta dele, e um token roubado
viraria credencial permanente. A mesma regra impede qualquer um de alterar, redefinir ou
desativar a própria conta pelo painel.

**Empresa com conta `NATY` não é desativada nem excluída.** Senão um `NATY` tranca outro,
e produção fica sem ninguém que administre.

**A recusa do login custa o mesmo tempo em todo caso.** E-mail inexistente, conta inativa
e senha errada passam pelo mesmo bcrypt, senão o tempo de resposta entrega a lista de
quem tem conta.

**Integrante desativado mantém o histórico.** O registro nunca é apagado, porque
progresso e pontuação apontam para ele. Desativado não entra no sistema e some do
ranking, e o progresso dele continua contando para o acompanhamento.

**Empresa inativa tranca os integrantes dela.** Login recusado e sessão aberta recusada na
chamada seguinte, com o histórico intacto. Empresa com integrante não é excluída, só
desativada.

**Trocar senha e desativar revogam as sessões.** Reativar não ressuscita token antigo.

**O tempo de expiração da sessão e os parâmetros de hash de senha vivem em variável de
ambiente.** A aplicação recusa subir em produção sem eles, para não existir valor padrão
de segurança em repositório. O token é opaco e não é assinado, então não existe chave de
assinatura.

**A sessão renova até uma duração máxima contada do login.** Com token válido, a renovação
emite token novo e revoga o apresentado, então o app não pede senha a cada abertura. A
expiração do token novo nunca passa do fim de `app.sessao.duracao-maxima`, e depois dele o
app autentica de novo. Sem teto, renovar indefinidamente mantém a sessão eterna e anula o
prazo de expiração. A duração máxima vive em variável de ambiente, e a produção recusa subir
sem ela.

**A sessão encerrada é removida depois de um prazo de retenção.** Sessão expirada ou
revogada há mais que `app.sessao.retencao` sai da tabela por limpeza periódica, com
periodicidade em `app.sessao.limpeza.cron`. O prazo vive em variável de ambiente e a
produção recusa subir sem ele. A remoção de sessão não toca em progresso, tentativa nem
ponto.

## Isolamento por empresa

**Todo dado de pessoa é filtrado pela empresa de quem chamou.** Integrante, progresso,
pontuação e ranking. A empresa vem da identidade da requisição, nunca do corpo nem da
URL, onde o cliente poderia trocá-la.

**O cadastro de empresas e o acompanhamento são os únicos lugares que atravessam
empresas**, e só o papel `NATY` entra neles. O `ADMIN` recebe a empresa da identidade da
requisição, e integrante de outra empresa responde como inexistente.

**O conteúdo de treinamento é global.** Nenhuma tabela de conteúdo tem `empresa_id`,
porque todas as empresas fazem o mesmo treinamento sobre o mesmo produto. Conteúdo
exclusivo, se existir, entra como tabela de associação entre empresa e trilha, sem
alterar o que já existe.

## Conteúdo e quiz

**Só o papel `NATY` escreve conteúdo.** O conteúdo é global, e um `ADMIN` de cliente que
editasse a trilha mudaria o treinamento de todas as empresas.

**Escrita de conteúdo cria no fim e reordena em operação própria.** A ordem entre irmãos tem
índice único por pai, então criar e editar nunca recebem a posição. A reordenação recebe a
lista completa dos irmãos e renumera de 1 a n, e lista diferente dos irmãos atuais é recusada.

**Trilha inativa some da listagem e mantém o histórico.** Progresso, tentativa e ponto de quem
a estudou continuam gravados. Conteúdo com progresso, tentativa ou ponto registrado não é
removido, e a alternativa é desativar a trilha.

**Toda alteração de conteúdo é registrada.** O registro guarda a conta, a entidade, o
identificador, a ação e o momento, na mesma transação da alteração, e sobrevive à remoção do
conteúdo. É ele que responde quem trocou um gabarito quando as notas mudam de repente.

**Duração de vídeo só existe com vídeo**, e vídeo exige URL HTTPS do YouTube e duração
positiva.

**Toda pergunta tem exatamente uma alternativa correta e ao menos duas alternativas.** A regra
vale na entrada e em toda transição do estado gravado: marcar uma correta move a marca, e
desmarcar a única correta, remover a correta ou deixar menos de duas alternativas é recusado. Quiz
sem pergunta quebra a correção, então a última pergunta não é removida.

**O gabarito sai por um único canal, a leitura administrativa do quiz.** Ela exige `NATY`. A
leitura do quiz pelo integrante e a resposta de tentativa não têm o campo `correta`.

**O gabarito não sai em leitura.** O DTO de alternativa não tem o campo de resposta
correta: um campo que não existe no record não vaza, enquanto uma anotação de ocultação
esquecida em campo novo vaza em silêncio. O DTO de escrita, que carrega o gabarito, é um
record separado do DTO de leitura.

**A tentativa de quiz é ilimitada e vale a melhor nota.** É treinamento, não
certificação: o atrito de bloquear a retentativa custa mais do que a força bruta
economiza.

**O histórico completo de tentativas é guardado**, não só a última, porque é ele que
revela quem está travado em uma atividade. Tentativa não tem prazo de retenção: o
acompanhamento depende do histórico inteiro.

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

## Acompanhamento

**A Naty acompanha quem avança e quem parou, e só a conta `NATY` lê o acompanhamento.** É o
único ponto, junto do cadastro de empresas, que atravessa empresas, e nunca escreve em progresso,
pontuação nem cadastro.

**Parado é o integrante que passou o prazo configurado sem concluir atividade.** O prazo vive em
`app.acompanhamento.dias-parado`, padrão 3 dias de calendário no fuso da empresa, e a comparação é
`dias >= prazo`. Conta nova sem estudo está sem início até passar o prazo, e quem concluiu tudo
nunca está parado.

**O funil por atividade conta quem chegou e quem passou.** Chegou quem tem a atividade liberada,
passou quem a concluiu, e é essa diferença que revela a atividade que barra a empresa inteira. A
conta `NATY` e o integrante desativado ficam fora de toda contagem.

## Consumidores

**O app Flutter tem Login, Home no estilo Duolingo e tela de assistir aula.** São as três
telas que consomem a API, e o contrato delas não muda sem aviso ao time do app: resolução
de sessão, leitura da trilha com progresso e registro de vídeo assistido.

**O painel administrativo é a fonte da verdade de quem existe.** Empresa, integrante e
credencial nascem lá, e nenhum outro caminho cadastra pessoa no sistema. A exceção é a
conta `NATY`, que nasce do bootstrap ou do seed.

## Escrita e schema

**Só o pacote `painel` escreve em `usuario` e em `empresa`, e só o pacote `conteudo` escreve
nas entidades de `trilha`.** Concentrar a escrita em um lugar é o que mantém a regra auditável.

**O schema do banco é do Flyway.** `ddl-auto` fica em `validate`: adicionar campo em
entidade sem a migration correspondente derruba a aplicação na subida, e isso é
proposital.

**Migration aplicada não é editada nem renomeada.** O Flyway valida o checksum e a
descrição do que já rodou. Correção de conteúdo semeado entra em migration nova.

**Senha, hash de senha e token de sessão nunca aparecem em log nem em mensagem de erro.**
Nenhuma camada registra corpo de requisição nem o cabeçalho `Authorization`, e o
`org.hibernate` fica em INFO porque em DEBUG ele imprime o hash da entidade.

**Toda resposta carrega `X-Request-Id`, e toda linha de log da chamada carrega o mesmo
valor.** O cliente pode enviar o identificador, aceito entre 8 e 64 caracteres de letra,
dígito e hífen. Qualquer outro valor recebe um identificador gerado.

**O Actuator exige a conta `NATY`, exceto a saúde sem detalhe.** Métricas, estado de banco
e demais endpoints são operacionais do sistema inteiro, não de uma empresa.

**Listagem é paginada.** Ranking, integrantes e acompanhamento crescem com a empresa, e
a primeira empresa grande não pode descobrir isso em produção.
