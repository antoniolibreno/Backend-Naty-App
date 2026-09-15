# Backlog do backend até o MVP

Cada item abaixo é uma entrega verificável, para virar um card. O título diz o que
fazer, a linha seguinte explica em linguagem simples o que a task faz e por quê, e a
linha `Toca:` aponta o endpoint, a tabela ou o arquivo afetado.

O frontend Flutter já tem Login, Home estilo Duolingo e tela de assistir aula. Este
backlog é o que falta no backend para essas telas funcionarem de verdade e para o
produto fechar o ciclo de trilha, quiz, pontuação e acompanhamento.

## O que já está pronto

Não vire card nada disto, já existe e funciona:

- Conteúdo de treinamento completo em banco e leitura dele: `GET /api/v1/trilhas`,
  `GET /api/v1/trilhas/{trilhaId}`, `GET /api/v1/atividades/{atividadeId}` e
  `GET /api/v1/atividades/{atividadeId}/quiz` sem gabarito.
- `POST /api/v1/sessoes` resolvendo o integrante pelo e-mail digitado, provisório e
  sem autenticação.
- Entidades `Trilha`, `Modulo`, `Atividade`, `Quiz`, `Pergunta`, `Alternativa`,
  `Usuario` e `Empresa`, com repositórios e mappers.
- Migrations `V1` a `V5` mais o seed de desenvolvimento, tratamento global de erro em
  formato único e Docker Compose. A `V5` removeu as colunas que existiam só por causa
  da Naty API.
- Progresso na trilha: migration `V4`, pacote `progresso`, desbloqueio linear,
  `GET /api/v1/trilhas/{trilhaId}/progresso` e
  `POST /api/v1/atividades/{atividadeId}/video-assistido`. O integrante chega no
  cabeçalho `X-Integrante-Id`, resolvido em `usuario/IntegranteArgumentResolver`, e a
  senha já entra no corpo de `POST /api/v1/sessoes` sem ser verificada. Total de 30
  testes de integração.

Ganchos que já estão no banco e que o backlog aproveita em vez de recriar:
`atividade.xp` com valor 10, `quiz.nota_minima` com valor 70,
`atividade.duracao_segundos` e `atividade.video_url` hoje sempre nulo.

## Ordem dos épicos

```
11. Painel administrativo
        |
        +---> 3. Autenticação e identidade
                     |
                     +---> 4. Progresso na trilha ---> 5. Quiz: tentativa e correção
                     |            |                              |
                     |            |                              v
                     |            +----------------------> 6. Gamificação
                     |            |
                     |            +---> 8. Vídeo na atividade
                     |
                     +---> 9. Administração de conteúdo

7. Acompanhamento: depois do MVP, precisa de quem administra estar definido
10. Endurecimento e observabilidade: em paralelo, a qualquer momento
```

A autenticação vem antes do progresso de propósito. Pontuação, sequência e ranking
construídos sobre um e-mail não verificado nascem fraudáveis, e o CRUD de conteúdo
não pode ficar aberto na internet.

## 3. Autenticação e identidade

- [ ] **3.1 Escrever a proposta OpenSpec do épico**
  O mecanismo já está decidido em `decisoes.md`: credencial nossa, cadastrada pelo
  painel, validada contra o nosso banco, com token opaco de sessão em tabela. A
  proposta registra o desenho antes do código, como manda o `CLAUDE.md` raiz.
  Toca: proposta OpenSpec do épico

- [ ] **3.2 Adicionar Spring Security e a biblioteca de token ao `pom.xml`**
  Traz a segurança para o projeto, que hoje não tem nenhuma dependência disso.
  Toca: `pom.xml`

- [ ] **3.3 Criar `SecurityConfig`**
  A classe está deliberadamente ausente hoje, para ninguém preenchê-la fora de hora.
  Nasce agora, definindo o que é público (health e a emissão de token) e o que exige
  identificação.
  Toca: `config/SecurityConfig.java`

- [ ] **3.4 Escrever a migration de credencial e sessão**
  Cria as tabelas que o mecanismo escolhido precisa, por Flyway. Adicionar campo em
  entidade sem migration derruba a aplicação na subida, e isso é proposital.
  Toca: nova migration em `db/migration`

- [ ] **3.5 Trocar `POST /api/v1/sessoes` por emissão de token**
  O endpoint que hoje só devolve identificadores passa a autenticar de verdade e
  devolver token. A tela de login do app já está pronta e é o consumidor. A senha já
  chega no corpo desde a etapa de progresso, exigida e descartada sem verificação: o
  que falta aqui é verificar e emitir token.
  Toca: `POST /api/v1/sessoes`

- [ ] **3.6 Criar o filtro que resolve integrante e empresa da requisição**
  Cada chamada autenticada passa a saber quem é o integrante e de qual empresa, sem
  que o app precise mandar isso no corpo ou na URL, onde poderia ser forjado.
  Toca: `config`, `usuario`

- [ ] **3.7 Amarrar toda leitura à empresa do integrante autenticado**
  Fecha o buraco de qualquer um consultar qualquer empresa. Progresso, ranking e
  integrante passam a ser sempre filtrados pela empresa de quem chamou.
  Toca: todos os controllers de leitura por empresa

- [ ] **3.8 Expirar e renovar token**
  Define validade e o caminho de renovação, para o app não obrigar login a cada
  abertura nem manter sessão eterna.
  Toca: `POST /api/v1/sessoes`, `usuario`

- [ ] **3.10 Testar que integrante de uma empresa não lê dado de outra**
  Cria duas empresas com integrantes e prova que o token de uma não alcança nada da
  outra, em todos os endpoints por empresa.
  Toca: `src/test/java/.../usuario`

- [ ] **3.11 Registrar o último acesso sem escrever em `usuario`**
  Só o painel escreve na tabela `usuario`. O último login do integrante precisa morar
  na tabela de sessão, ou a regra tem que ser reescrita de propósito no `CLAUDE.md` do
  pacote.
  Toca: `usuario/CLAUDE.md`, nova migration em `db/migration`

- [ ] **3.12 Guardar a chave de assinatura fora do código**
  Chave e tempo de expiração só por variável de ambiente, e a aplicação recusa subir
  em produção sem elas, para não existir chave padrão em repositório.
  Toca: `application.yml`, `application-prod.yml`, `.env.example`

- [ ] **3.13 Declarar o esquema de segurança no OpenAPI**
  Sem isso o Swagger não deixa testar endpoint protegido e o time do app perde o
  contrato de como mandar o token.
  Toca: `config/OpenApiConfig.java`, `/v3/api-docs`

- [ ] **3.14 Atualizar a seção Dívidas conhecidas do `CLAUDE.md` raiz**
  A dívida de "não há autenticação" deixa de existir. O documento precisa refletir
  isso, senão o próximo a ler assume que o ranking continua fraudável.
  Toca: `CLAUDE.md`

## 4. Progresso na trilha

- [x] **4.1 Escrever a migration de `progresso_atividade`**
  Cria a tabela que guarda o estado de cada integrante em cada atividade, com quando
  assistiu o vídeo e quando concluiu, com unicidade por integrante e atividade.
  Toca: nova migration em `db/migration`

- [x] **4.2 Criar o pacote `progresso` com entidades e repositórios**
  Nasce o pacote previsto no `CLAUDE.md` raiz, com seu próprio `CLAUDE.md`, ligando
  progresso a `Usuario` e `Atividade`.
  Toca: `progresso`

- [x] **4.3 Implementar a regra de desbloqueio linear**
  A primeira atividade da trilha começa disponível e cada atividade seguinte só abre
  quando a anterior é concluída. É o que faz a trilha parecer o Duolingo.
  Toca: `progresso`

- [x] **4.4 Expor `GET /api/v1/trilhas/{trilhaId}/progresso` para a Home**
  Devolve a trilha inteira com módulos e atividades em ordem, cada nó marcado como
  bloqueado, disponível ou concluído. É a chamada única que a Home do app faz para se
  desenhar.
  Toca: `GET /api/v1/trilhas/{trilhaId}/progresso`

- [x] **4.5 Expor `POST /api/v1/atividades/{atividadeId}/video-assistido`**
  Registra que o integrante assistiu o vídeo da aula. É o que a tela de assistir aula
  chama ao terminar.
  Toca: `POST /api/v1/atividades/{atividadeId}/video-assistido`

- [x] **4.6 Implementar a regra de conclusão de atividade**
  Atividade com quiz só conclui com quiz aprovado. Atividade sem quiz conclui com o
  vídeo assistido. Sem essa regra o desbloqueio linear não tem gatilho.
  Toca: `progresso`

- [ ] **4.7 Expor o resumo de progresso do integrante**
  Devolve percentual concluído, atividades feitas e qual é a próxima atividade, para
  o app mostrar o "continuar de onde parou".
  Toca: `GET /api/v1/integrantes/{integranteId}/progresso`

- [x] **4.8 Impedir progresso em atividade bloqueada**
  Recusa tentativa de concluir ou marcar vídeo de atividade que ainda não abriu,
  senão o app consegue pular a trilha inteira.
  Toca: `progresso`, `shared/exception/ApiExceptionHandler.java`

- [x] **4.9 Testar a trilha inteira do primeiro nó ao último**
  Percorre as seis atividades do conteúdo semeado, provando que cada conclusão abre a
  seguinte e que a atividade seguinte estava bloqueada antes.
  Toca: `src/test/java/.../progresso`

- [x] **4.10 Escrever `progresso/CLAUDE.md`**
  Registra responsabilidade, contratos, decisões, armadilhas e estado atual, como os
  outros pacotes. Pacote sem `CLAUDE.md` está incompleto.
  Toca: `progresso/CLAUDE.md`

## 5. Quiz: tentativa e correção

- [ ] **5.1 Escrever a migration de `tentativa_quiz` e `resposta_tentativa`**
  Cria as tabelas que guardam cada tentativa e a alternativa escolhida em cada
  pergunta, com a nota calculada.
  Toca: nova migration em `db/migration`

- [ ] **5.2 Expor `POST /api/v1/atividades/{atividadeId}/quiz/tentativas`**
  Recebe as respostas do integrante e devolve o resultado. É o fecho da aula.
  Toca: `POST /api/v1/atividades/{atividadeId}/quiz/tentativas`

- [ ] **5.3 Corrigir a tentativa só no servidor**
  A nota é calculada comparando as respostas com a coluna `alternativa.correta`, que
  nunca sai em DTO. Nota enviada pelo cliente é ignorada, não existe como campo de
  entrada.
  Toca: `progresso`, coluna `alternativa.correta`

- [ ] **5.4 Aplicar a nota mínima do quiz**
  Usa `quiz.nota_minima`, que já está no banco com valor 70, para decidir aprovado ou
  reprovado, em vez de fixar o número no código.
  Toca: coluna `quiz.nota_minima`

- [ ] **5.5 Definir e aplicar a política de retentativa**
  Decide se há limite de tentativas e se existe espera entre elas, e implementa.
  Sem isso o integrante acerta o quiz por força bruta.
  Toca: `progresso`

- [ ] **5.6 Guardar o histórico de tentativas**
  Mantém todas as tentativas, não só a última, porque o acompanhamento precisa saber
  quem está travado em uma atividade específica.
  Toca: tabela `tentativa_quiz`

- [ ] **5.7 Devolver o resultado sem entregar o gabarito inteiro**
  Diz a nota, se aprovou e quantas erradas. Se vai revelar a resposta certa depois de
  aprovar, isso é decisão explícita, não acidente de serialização.
  Toca: `POST /api/v1/atividades/{atividadeId}/quiz/tentativas`

- [ ] **5.8 Recusar tentativa mal formada**
  Tentativa com pergunta de outro quiz, pergunta repetida, pergunta faltando ou
  alternativa que não pertence à pergunta volta como erro de validação, não como nota
  zero silenciosa.
  Toca: `progresso`, `shared/exception/ApiExceptionHandler.java`

- [ ] **5.9 Testar que o gabarito não vaza na tentativa**
  Verifica que nenhum campo da resposta da tentativa, nem da leitura do quiz, revela
  a alternativa correta antes da correção.
  Toca: `src/test/java/.../progresso`

## 6. Gamificação

- [ ] **6.1 Escrever a migration de pontuação, sequência e conquista**
  Cria as tabelas de pontos do integrante, sequência de dias e conquistas
  conquistadas, sempre ligadas à empresa.
  Toca: nova migration em `db/migration`

- [ ] **6.2 Criar o pacote `gamificacao` com entidades e repositórios**
  Nasce o pacote previsto, com seu próprio `CLAUDE.md`.
  Toca: `gamificacao`

- [ ] **6.3 Conceder o XP da atividade na conclusão**
  Usa a coluna `atividade.xp`, que já existe com valor 10, para somar pontos quando a
  atividade conclui. Não inventa outra fonte de pontuação.
  Toca: `gamificacao`, coluna `atividade.xp`

- [ ] **6.4 Calcular a sequência de dias em fuso definido**
  Precisa estar decidido e escrito de qual fuso é a meia-noite que quebra a
  sequência, senão o mesmo integrante ganha ou perde streak dependendo do servidor.
  Toca: `gamificacao`

- [ ] **6.5 Adicionar o fuso horário da empresa**
  A sequência de dias precisa saber quando vira o dia para aquele cliente. Os
  carimbos são gravados em UTC, então sem o fuso a virada do dia erra quem estuda de
  noite.
  Toca: nova migration em `db/migration`, tabela `empresa`

- [ ] **6.6 Semear o catálogo de conquistas por migration**
  As conquistas são conteúdo global, iguais para todas as empresas, e entram por
  migration como o resto do conteúdo, sem CRUD nesta etapa.
  Toca: nova migration em `db/migration`

- [ ] **6.7 Implementar a concessão de conquista**
  Avalia as regras quando algo muda no progresso e concede a conquista uma única vez
  por integrante.
  Toca: `gamificacao`

- [ ] **6.8 Expor `GET /api/v1/ranking`**
  Devolve a classificação dos integrantes dentro da empresa de quem chamou. Ranking
  entre empresas não existe e não deve existir.
  Toca: `GET /api/v1/ranking`

- [ ] **6.9 Expor `GET /api/v1/integrantes/{integranteId}/gamificacao`**
  Devolve pontos, sequência atual e conquistas do integrante, para o app montar o
  perfil.
  Toca: `GET /api/v1/integrantes/{integranteId}/gamificacao`

- [ ] **6.10 Definir desempate e paginação do ranking**
  Define o critério quando dois integrantes têm a mesma pontuação e pagina o
  resultado, para empresa grande não devolver lista infinita.
  Toca: `GET /api/v1/ranking`

- [ ] **6.11 Testar que ponto não é concedido duas vezes pela mesma atividade**
  Concluir de novo, ou refazer o quiz já aprovado, não pode somar XP de novo, senão o
  ranking vira farsa.
  Toca: `src/test/java/.../gamificacao`

- [ ] **6.12 Escrever `gamificacao/CLAUDE.md`**
  Registra onde mora a sequência, em que fuso, como o ranking é calculado e por que
  ponto é idempotente.
  Toca: `gamificacao/CLAUDE.md`

## 7. Acompanhamento

Fora do MVP. Falta decidir quem enxerga mais de uma empresa, e a decisão está
registrada como pendente em `decisoes.md`.

- [ ] **7.1 Definir o que é "parou"**
  Sem um número de dias sem concluir atividade, "quem parou" não é consultável.
  Define o limite e onde ele é configurado.
  Toca: proposta OpenSpec do épico

- [ ] **7.2 Criar o pacote `acompanhamento`**
  Nasce o pacote previsto, com seu próprio `CLAUDE.md`, lendo progresso e gamificação
  sem escrever neles.
  Toca: `acompanhamento`

- [ ] **7.3 Expor a listagem de integrantes com quem avançou e quem parou**
  Devolve, por empresa, cada integrante com onde está na trilha e desde quando não se
  move. É a tela principal da Naty.
  Toca: `GET /api/v1/acompanhamento/empresas/{empresaId}/integrantes`

- [ ] **7.4 Expor as métricas agregadas da empresa**
  Devolve adesão, percentual médio de conclusão e quantos pararam, para a Naty
  comparar clientes sem abrir integrante por integrante.
  Toca: `GET /api/v1/acompanhamento/empresas/{empresaId}/metricas`

- [ ] **7.5 Restringir o acompanhamento ao papel administrativo**
  Este é o único lugar que lê dado de mais de uma empresa. Só o papel definido na
  migration 11.1 entra aqui.
  Toca: `acompanhamento`, `config/SecurityConfig.java`

- [ ] **7.6 Mostrar onde as pessoas travam**
  Devolve, por atividade, quantos chegaram e quantos passaram. É o que revela a
  atividade ou a pergunta que está barrando a empresa inteira.
  Toca: `GET /api/v1/acompanhamento/atividades`

- [ ] **7.7 Exportar a lista de integrantes em CSV**
  Quem acompanha vai querer mandar a lista para o time que fala com o cliente, sem
  copiar da tela.
  Toca: `GET /api/v1/acompanhamento/empresas/{empresaId}/integrantes.csv`

- [ ] **7.8 Criar os índices que o acompanhamento exige**
  As consultas cruzam progresso e tentativa por empresa e por data. Sem índice, o
  painel varre tabela cheia a cada abertura.
  Toca: nova migration em `db/migration`

- [ ] **7.9 Testar que integrante comum não acessa o acompanhamento**
  Prova que token de integrante recebe negação de acesso em todos os endpoints deste
  pacote.
  Toca: `src/test/java/.../acompanhamento`

- [ ] **7.10 Escrever `acompanhamento/CLAUDE.md`**
  Registra que este pacote só lê, que ele atravessa empresas de propósito e qual a
  definição de parado.
  Toca: `acompanhamento/CLAUDE.md`

## 8. Vídeo na atividade

- [ ] **8.1 Decidir a hospedagem do vídeo**
  `atividade.video_url` existe e é sempre nulo hoje, e a atividade mostra só a
  imagem. Escolher entre plataforma pública, plataforma privada ou armazenamento
  próprio muda o que o app precisa fazer para tocar o vídeo.
  Toca: proposta OpenSpec do épico

- [ ] **8.2 Preencher `video_url` do conteúdo por migration**
  Os vídeos entram em migration nova. `V3` não pode ser editada, porque o Flyway
  valida o checksum do que já rodou.
  Toca: nova migration em `db/migration`, coluna `atividade.video_url`

- [ ] **8.3 Validar a URL do vídeo**
  Recusa URL fora do domínio de hospedagem escolhido, para o app não receber link
  quebrado ou de origem inesperada.
  Toca: `trilha`

- [ ] **8.4 Usar `duracao_segundos` na regra de vídeo assistido**
  A coluna já existe. Serve para o backend recusar um "assisti" que chegou antes de
  ser plausível assistir.
  Toca: coluna `atividade.duracao_segundos`, `progresso`

- [ ] **8.5 Ajustar o teste que presume vídeo nulo**
  O teste que garante que atividade sem vídeo não é erro afirma que o campo de vídeo
  vem nulo no conteúdo semeado. Preencher vídeo no seed quebra esse teste, então ele
  muda no mesmo diff.
  Toca: `src/test/java/.../trilha/QuizApiTest.java`

- [ ] **8.6 Registrar a decisão de vídeo em `trilha/CLAUDE.md`**
  O documento hoje diz que hospedagem e reprodução são decisão futura. Passa a dizer
  qual foi a decisão.
  Toca: `trilha/CLAUDE.md`

## 9. Administração de conteúdo

- [ ] **9.1 Substituir o conteúdo de exemplo pelo real por migration**
  O conteúdo em banco é exemplo, com UUID fixo escrito à mão. A migration nova apaga
  essas linhas pelos UUIDs fixos e insere o conteúdo verdadeiro, sem tocar em `V3`.
  Toca: nova migration em `db/migration`

- [ ] **9.2 Expor o CRUD de trilha**
  Permite criar e editar trilha pela API em vez de por migration. Só nasce depois da
  autenticação: sem login, um CRUD administrativo fica aberto na internet.
  Toca: `POST`, `PUT` e `DELETE` em `/api/v1/trilhas`

- [ ] **9.3 Expor o CRUD de módulo e atividade**
  Mesma ideia, um nível abaixo, incluindo os campos de imagem, vídeo, duração e XP.
  Toca: `/api/v1/modulos`, `/api/v1/atividades`

- [ ] **9.4 Expor o CRUD de quiz, pergunta e alternativa**
  Este é o único lugar onde o gabarito entra e sai da API, e só para quem administra.
  A leitura do quiz pelo integrante continua sem `correta`.
  Toca: `/api/v1/quizzes`, `/api/v1/perguntas`, `/api/v1/alternativas`

- [ ] **9.5 Separar o DTO de escrita do DTO de leitura da alternativa**
  A escrita precisa do campo de alternativa correta e a leitura não pode nem ter o
  campo. Dois records distintos, nunca um só com o campo anulado, que é o jeito de
  vazar gabarito por esquecimento.
  Toca: `trilha/dto`

- [ ] **9.6 Validar exatamente uma alternativa correta por pergunta**
  Pergunta com zero ou duas corretas quebra a correção do quiz. A regra vale na
  escrita, não só no seed.
  Toca: `trilha`

- [ ] **9.7 Permitir reordenar módulo e atividade**
  A ordem define a trilha e tem unicidade por pai no banco. Reordenar precisa ser
  operação própria, senão qualquer troca de posição colide com o índice único.
  Toca: `trilha`, índices `modulo(trilha_id, ordem)` e `atividade(modulo_id, ordem)`

- [ ] **9.8 Ativar e desativar trilha sem apagar progresso**
  Trilha inativa desaparece da listagem, mas o progresso de quem já a fez continua
  contando para pontuação e acompanhamento.
  Toca: coluna `trilha.ativa`, `progresso`

- [ ] **9.9 Impedir remoção de conteúdo com progresso registrado**
  Apagar atividade que já tem progresso ou tentativa joga fora o histórico de quem
  estudou. Oferece desativação em vez de remoção.
  Toca: `trilha`, `progresso`

- [ ] **9.10 Registrar quem alterou o conteúdo**
  Guarda autor e momento de cada alteração, para dar para descobrir quem trocou o
  gabarito quando as notas caírem de repente.
  Toca: nova migration em `db/migration`, `trilha`

- [ ] **9.11 Testar que o CRUD exige papel administrativo**
  Prova que token de integrante comum recebe negação em toda escrita de conteúdo.
  Toca: `src/test/java/.../trilha`

## 10. Endurecimento, observabilidade e dívidas

- [ ] **10.1 Preencher `OpenApiConfig`**
  Hoje é uma classe de configuração vazia. Passa a definir título, versão, servidores
  e agrupamento de tags, para o Swagger servir de contrato para o time do app.
  Toca: `config/OpenApiConfig.java`, `/v3/api-docs`

- [ ] **10.2 Padronizar `Empresa` com id gerado e timestamps automáticos**
  É a única entidade sem geração de id e sem os timestamps automáticos que as outras
  têm, então hoje o id precisa ser preenchido à mão. Alinhar evita bug em quem for
  criar empresa por código.
  Toca: `empresa/Empresa.java`

- [ ] **10.3 Trocar o assert frágil de `QuizApiTest`**
  O teste garante que o gabarito não vaza procurando o texto `true` no corpo da
  resposta. Qualquer campo booleano verdadeiro no futuro quebra o teste sem nenhum
  vazamento real. Trocar por verificação da estrutura da resposta.
  Toca: `src/test/java/.../trilha/QuizApiTest.java`

- [x] **10.4 Tratar conflito de regra de negócio no `ApiExceptionHandler`**
  Progresso e quiz vão recusar operação inválida, como concluir atividade bloqueada.
  Cada exceção nova precisa do seu tratamento explícito, porque não existe tratamento
  genérico de propósito.
  Toca: `shared/exception/ApiExceptionHandler.java`

- [ ] **10.5 Paginar as listagens**
  Ranking, integrantes e acompanhamento crescem com a empresa. Definir paginação
  padrão antes de a primeira empresa grande entrar.
  Toca: controllers de listagem

- [ ] **10.6 Criar `src/test/resources` com `application-test.yml`**
  Não existe nenhum recurso de teste hoje, então os testes herdam o perfil de
  desenvolvimento. Um perfil de teste próprio deixa explícito o que roda no teste.
  Toca: `src/test/resources/application-test.yml`

- [ ] **10.7 Decidir o destino de `DataUtil`**
  É uma classe utilitária vazia em `shared`. Ou ganha o cálculo de data que a
  sequência de dias precisa, ou sai do projeto.
  Toca: `shared/util/DataUtil.java`

- [ ] **10.8 Expor métricas da aplicação**
  Publica métricas de requisição e de banco, para dar para responder "está lento onde"
  sem adivinhar.
  Toca: `application.yml`, `/actuator`

- [ ] **10.9 Padronizar log sem vazar credencial**
  Define o que é logado em cada camada e garante que senha, hash de senha e token de
  sessão nunca apareçam em log nem em mensagem de erro.
  Toca: `painel`, `usuario`, `shared`

- [ ] **10.11 Testar as regras de dependência entre pacotes**
  Faz o build falhar se `trilha` passar a depender de `progresso`, se algum pacote
  além de `painel` escrever em `usuario`, ou se um DTO de leitura ganhar campo de
  gabarito. Essas setas são fáceis de inverter por conveniência.
  Toca: `src/test/java`

- [ ] **10.12 Verificar a regra de comentários no build**
  A busca por comentário explicativo e por marcador temporal hoje é item manual de
  checklist. Vira passo do build, senão depende de disciplina.
  Toca: `pom.xml`

- [ ] **10.13 Configurar integração contínua**
  Roda `./mvnw -B verify` em cada envio, com Docker disponível para os
  Testcontainers, para formatação e testes não dependerem de quem lembrou de rodar.
  Toca: configuração de CI

- [ ] **10.14 Correlacionar log por requisição**
  Injeta um identificador por chamada no log e devolve no cabeçalho, para conseguir
  seguir uma chamada do app até o erro.
  Toca: `config`, `shared`

- [ ] **10.15 Fechar o detalhe do Actuator em produção**
  O arquivo base mostra detalhe de saúde sempre, incluindo o estado do banco. Revisa o
  que fica exposto fora de desenvolvimento.
  Toca: `application.yml`, `application-prod.yml`

- [ ] **10.16 Definir retenção de dado operacional**
  Decide o que fazer com tentativa de quiz velha e com sessão expirada, para tabela de
  histórico não crescer para sempre.
  Toca: `progresso`, `usuario`

- [ ] **10.17 Expor a leitura de integrante em `UsuarioController`**
  O controller existe com o caminho declarado e nenhum método mapeado, e
  `UsuarioResponse` e `UsuarioFiltro` são classes vazias. A tela de ranking e o
  acompanhamento precisam ler integrante, sempre filtrado por empresa. A escrita mora
  no painel, e só lá.
  Toca: `GET /api/v1/usuarios`, `usuario/UsuarioController.java`

## 11. Painel administrativo

Nasce com a decisão de que o cadastro é nosso. É o épico que destrava o login, e por
isso vem antes do épico 3.

- [ ] **11.1 Escrever a migration de credencial, papel e sessão**
  Cria a senha com hash do integrante, o papel administrativo nosso e a tabela de
  sessão que guarda o token opaco e o último acesso.
  Toca: nova migration em `db/migration`

- [ ] **11.2 Expor o CRUD de empresa com fuso horário**
  A entidade `Empresa` não gera id nem preenche carimbo de data, então persistir por
  código falha hoje. Passa a nascer pelo painel, com o fuso horário que a sequência de
  dias precisa.
  Toca: `empresa/Empresa.java`, tabela `empresa`, `/api/v1/empresas`

- [ ] **11.3 Expor o cadastro de integrante vinculado à empresa**
  Cria o integrante direto no nosso banco, com nome, e-mail e empresa. O e-mail é
  único no sistema inteiro, porque é ele que resolve a empresa no login.
  Toca: `/api/v1/integrantes`, tabela `usuario`

- [ ] **11.4 Definir e resetar a senha do integrante**
  Quem administra define a senha no cadastro e consegue resetá-la depois. A senha é
  guardada como hash e nunca volta em resposta nem aparece em log.
  Toca: `/api/v1/integrantes/{integranteId}/senha`

- [ ] **11.5 Desativar integrante sem apagar progresso**
  Desativado some do ranking e não consegue logar, mas o progresso e a pontuação
  continuam de pé, porque o histórico da empresa aponta para ele.
  Toca: tabela `usuario`, `progresso`, `gamificacao`

- [ ] **11.6 Listar integrantes da empresa com busca e paginação**
  É a tela principal de quem administra. Sempre filtrada por empresa, nunca devolvendo
  lista inteira sem página.
  Toca: `GET /api/v1/integrantes`

- [ ] **11.7 Proteger todo o painel pelo papel administrativo**
  Nenhuma rota de cadastro pode responder para token de integrante comum. O papel vem
  da migration 11.1, e a coluna `usuario.perfil` não vale como permissão.
  Toca: `config/SecurityConfig.java`, `usuario`

- [ ] **11.8 Testar que administrador de uma empresa não enxerga outra**
  Cria duas empresas com integrantes e prova que o cadastro, a listagem e o reset de
  senha de uma nunca alcançam a outra.
  Toca: `src/test/java/.../painel`

- [ ] **11.9 Escrever `painel/CLAUDE.md`**
  Registra que este pacote é o único que escreve em `usuario` e por que a credencial é
  nossa.
  Toca: `painel/CLAUDE.md`

## Decisões tomadas

As dezesseis decisões que travavam o backlog estão resolvidas em `decisoes.md`, com a
escolha, o motivo e o que cada uma muda aqui. Decisão registrada lá é premissa: mudar
uma delas obriga a revisar as tasks que ela libera.

O resumo do que elas mudaram neste documento: a integração com a Naty API saiu do
projeto e levou junto os épicos 1 e 2, o épico 11 nasceu no lugar deles, o épico 7
ficou para depois do MVP, o quiz aceita tentativa ilimitada valendo a melhor nota e sem
revelar gabarito, o vídeo vai para o YouTube não listado, a sequência de dias usa o
fuso da empresa e o ranking é calculado a cada consulta.
