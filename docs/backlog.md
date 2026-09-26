# Backlog do backend até o MVP

Cada item é uma entrega verificável, pronta para virar card. O título diz o que fazer, a
linha seguinte explica o que a entrega faz e por quê, e a linha `Toca:` aponta o
endpoint, a tabela ou o arquivo afetado.

As regras de produto que estes itens obedecem estão em `regras.md`. Os limites do sistema
construído estão no `CLAUDE.md` da raiz.

## Escopo em funcionamento

Nada desta lista vira card.

- Leitura do conteúdo: `GET /api/v1/trilhas`, `GET /api/v1/trilhas/{trilhaId}`,
  `GET /api/v1/atividades/{atividadeId}` e `GET /api/v1/atividades/{atividadeId}/quiz`
  sem gabarito.
- Autenticação por e-mail e senha em `POST /api/v1/sessoes`, com token opaco em tabela,
  revogação em `DELETE /api/v1/sessoes/atual` e toda a demais rota exigindo
  `Authorization: Bearer`.
- Entidades `Trilha`, `Modulo`, `Atividade`, `Quiz`, `Pergunta`, `Alternativa`,
  `Usuario`, `Empresa`, `Sessao` e `ProgressoAtividade`, com repositórios e mappers.
- Migrations `V1` a `V6`, seed de desenvolvimento com credencial, tratamento global de
  erro em formato único e Docker Compose.
- Progresso na trilha: desbloqueio linear, `GET /api/v1/trilhas/{trilhaId}/progresso` e
  `POST /api/v1/atividades/{atividadeId}/video-assistido`, com o integrante resolvido a
  partir do token da sessão, coberto por testes de integração.

Ganchos que já estão no banco e que o backlog aproveita em vez de recriar:
`atividade.xp` com valor 10, `quiz.nota_minima` com valor 70, `atividade.duracao_segundos`
e `atividade.video_url`.

## Ordem de execução

```
Fase 0  Fundação de identidade
   |
   v
Fase 1  Autenticação e identidade
   |
   +--> Fase 2  Painel administrativo
   |
   +--> Fase 3  MVP funcional

Fora do MVP: acompanhamento.
Em paralelo: endurecimento, observabilidade e dívidas.
```

A autenticação vem antes de tudo que guarda ponto ou expõe escrita. Pontuação, sequência
e ranking construídos sobre um e-mail não verificado nascem fraudáveis, e o CRUD de
conteúdo não pode ficar aberto na internet.

O que a autenticação precisa do painel não é um controller, é uma coluna com hash. Por
isso a Fase 0 existe e o painel inteiro vem depois: administrador inicial nasce do seed
em desenvolvimento e de bootstrap por variável de ambiente em produção, nunca de um
`POST` aberto.

As arestas que fixam a ordem:

- 3.3 antes de 11.7, porque 11.7 não cria cadeia de segurança, só acrescenta uma regra de
  papel à cadeia que nasce em 3.3.
- 3.6 antes de 11.6 e 11.8, porque a empresa vem da identidade da requisição. Sem 3.6 a
  listagem nasceria com um contrato que muda depois.
- 10.2 antes de 11.2, porque `Empresa` não gera id nem preenche carimbo, então persistir
  empresa por código falha.
- 5.9 antes de 6.3, porque 5.9 troca o gatilho de conclusão, e conceder ponto antes é
  escrever sobre um gatilho que muda no diff seguinte.
- 11.2 antes de 6.4, porque a sequência de dias precisa do fuso horário da empresa.
- 8.1 junto de 8.4 e 10.3, porque preencher `video_url` invalida o teste que presume
  vídeo nulo e o assert que procura o texto `true` no corpo.
- 9.1 depois de os testes descobrirem os identificadores de conteúdo pela API, porque
  quatro testes fixam os identificadores do conteúdo de exemplo.
- 10.5 depois de 9.1, porque um perfil de teste sem `classpath:db/seed-dev` derruba todos
  os testes de integração de uma vez.

## Fase 0. Fundação de identidade

É a menor fatia que destrava o resto. Sem estas colunas, nada da Fase 1 sobe, porque
`ddl-auto` está em `validate`.

- [x] **0.1 Escrever a migration de credencial, papel e sessão**
  Cria a senha com hash do integrante, o papel administrativo, a marca de ativo e a
  tabela de sessão que guarda o token opaco, a expiração e o último acesso. Troca o índice
  de e-mail por único global, porque é o e-mail que resolve a empresa no login. A coluna
  `usuario.status` é status de WhatsApp e não serve.
  Toca: nova migration em `db/migration`

- [x] **0.2 Dar credencial às contas de desenvolvimento**
  Sem credencial válida em banco nenhum teste de login roda. O seed é repetível, então
  recalcula o checksum sozinho.
  Toca: `db/seed-dev/R__seed_empresa_exemplo.sql`

## Fase 1. Autenticação e identidade

- [x] **3.1 Escrever a proposta OpenSpec do épico**
  O mecanismo está decidido em `regras.md`: credencial nossa, cadastrada pelo painel,
  validada contra o nosso banco, com token opaco de sessão em tabela. A proposta registra
  o desenho antes do código.
  Toca: proposta OpenSpec do épico

- [x] **3.2 Adicionar Spring Security ao `pom.xml`**
  O projeto não tem nenhuma dependência de segurança. O token é opaco e mora em tabela,
  então nenhuma biblioteca de token entra junto.
  Toca: `pom.xml`

- [x] **3.3 Criar `SecurityConfig`**
  Define o que é público, health e emissão de token, e o que exige identificação.
  Toca: `config/SecurityConfig.java`

- [x] **3.5 Trocar `POST /api/v1/sessoes` por emissão de token**
  O endpoint autentica de verdade e devolve token. A tela de login do app é o consumidor,
  e a senha já chega no corpo.
  Toca: `POST /api/v1/sessoes`

- [x] **3.6 Criar o filtro que resolve integrante e empresa da requisição**
  Cada chamada autenticada sabe quem é o integrante e de qual empresa, sem que o app
  mande isso no corpo ou na URL, onde seria forjável.
  Toca: `config`, `usuario`

- [x] **3.7 Amarrar toda leitura à empresa do integrante autenticado**
  Fecha o buraco de qualquer um consultar qualquer empresa. Progresso, ranking e
  integrante são filtrados pela empresa de quem chamou.
  Toca: todos os controllers de leitura por empresa

- [ ] **3.8 Expirar e renovar token**
  Define validade e caminho de renovação, para o app não exigir login a cada abertura nem
  manter sessão eterna.
  Toca: `POST /api/v1/sessoes`, `usuario`

- [x] **3.10 Testar que integrante de uma empresa não lê dado de outra**
  Cria duas empresas com integrantes e prova que o token de uma não alcança nada da
  outra, em todos os endpoints por empresa.
  Toca: `src/test/java/.../usuario`

- [x] **3.11 Registrar o último acesso na tabela de sessão**
  Só o painel escreve em `usuario`, então o último login do integrante mora na sessão.
  Toca: nova migration em `db/migration`, `usuario`

- [x] **3.12 Guardar os parâmetros de segurança fora do código**
  Tempo de expiração da sessão e parâmetros de hash de senha só por variável de ambiente,
  e a aplicação recusa subir em produção sem eles.
  Toca: `application.yml`, `application-prod.yml`, `.env.example`

- [x] **3.13 Declarar o esquema de segurança no OpenAPI**
  Sem isso o Swagger não deixa testar endpoint protegido e o time do app perde o contrato
  de como mandar o token.
  Toca: `config/OpenApiConfig.java`, `/v3/api-docs`

- [x] **3.14 Atualizar os limites do sistema no `CLAUDE.md` raiz**
  A ausência de autenticação sai da lista de limites, junto com o aviso de que o ranking é
  fraudável.
  Toca: `CLAUDE.md`

## Fase 2. Painel administrativo

O cadastro é nosso. As rotas de escrita de integrante nascem depois da autenticação,
para o painel nunca passar por um estado aberto na internet.

- [x] **11.2 Expor o CRUD de empresa com fuso horário**
  A entidade `Empresa` não gera id nem preenche carimbo de data, então persistir por
  código falha. A empresa nasce pelo painel, com o fuso horário que a sequência de dias
  exige.
  Toca: `empresa/Empresa.java`, tabela `empresa`, `/api/v1/empresas`

- [ ] **11.3 Expor o cadastro de integrante vinculado à empresa**
  Cria o integrante no nosso banco, com nome, e-mail e empresa. O e-mail é único no
  sistema inteiro, porque é ele que resolve a empresa no login.
  Toca: `/api/v1/integrantes`, tabela `usuario`

- [ ] **11.4 Definir e resetar a senha do integrante**
  Quem administra define a senha no cadastro e consegue resetá-la. A senha é guardada
  como hash e não volta em resposta nem aparece em log.
  Toca: `/api/v1/integrantes/{integranteId}/senha`

- [ ] **11.5 Desativar integrante sem apagar progresso**
  Desativado some do ranking e não entra no sistema, e o progresso e a pontuação dele
  continuam de pé, porque o histórico da empresa aponta para ele.
  Toca: tabela `usuario`, `progresso`, `gamificacao`

- [ ] **11.6 Listar integrantes da empresa com busca e paginação**
  É a tela principal de quem administra. Sempre filtrada por empresa, nunca devolvendo
  lista inteira sem página.
  Toca: `GET /api/v1/integrantes`

- [ ] **11.7 Proteger todo o painel pelo papel administrativo**
  Nenhuma rota de cadastro responde para token de integrante comum. O papel vem da
  migration 0.1, e a coluna `usuario.perfil` não vale como permissão.
  Toca: `config/SecurityConfig.java`, `usuario`

- [ ] **11.8 Testar que administrador de uma empresa não enxerga outra**
  Cria duas empresas com integrantes e prova que o cadastro, a listagem e o reset de
  senha de uma nunca alcançam a outra.
  Toca: `src/test/java/.../painel`

- [ ] **11.9 Escrever `painel/CLAUDE.md`**
  Registra que este pacote é o único que escreve em `usuario` e por que a credencial é
  nossa.
  Toca: `painel/CLAUDE.md`

## Fase 3. MVP funcional

A ordem interna é progresso, quiz, vídeo, gamificação e conteúdo.

### Progresso na trilha

- [ ] **4.7 Expor o resumo de progresso do integrante**
  Devolve percentual concluído, atividades feitas e qual é a próxima atividade, para o app
  mostrar o "continuar de onde parou" sem pedir a trilha inteira.
  Toca: `GET /api/v1/integrantes/{integranteId}/progresso`

### Quiz: tentativa e correção

- [ ] **5.1 Escrever a migration de `tentativa_quiz` e `resposta_tentativa`**
  Cria as tabelas que guardam cada tentativa e a alternativa escolhida em cada pergunta,
  com a nota calculada.
  Toca: nova migration em `db/migration`

- [ ] **5.2 Expor `POST /api/v1/atividades/{atividadeId}/quiz/tentativas`**
  Recebe as respostas do integrante e devolve o resultado. É o fecho da aula.
  Toca: `POST /api/v1/atividades/{atividadeId}/quiz/tentativas`

- [ ] **5.3 Corrigir a tentativa só no servidor**
  A nota é calculada comparando as respostas com a coluna `alternativa.correta`, que nunca
  sai em DTO. Nota enviada pelo cliente não existe como campo de entrada.
  Toca: `progresso`, coluna `alternativa.correta`

- [ ] **5.4 Aplicar a nota mínima do quiz**
  Usa `quiz.nota_minima` para decidir aprovado ou reprovado, em vez de fixar o número no
  código.
  Toca: coluna `quiz.nota_minima`

- [ ] **5.5 Implementar a política de retentativa**
  Tentativa ilimitada valendo a melhor nota, conforme `regras.md`. O resultado guardado no
  progresso é o melhor, não o último.
  Toca: `progresso`

- [ ] **5.6 Guardar o histórico de tentativas**
  Mantém todas as tentativas, não só a melhor, porque o acompanhamento precisa saber quem
  está travado em uma atividade específica.
  Toca: tabela `tentativa_quiz`

- [ ] **5.7 Devolver o resultado sem entregar o gabarito**
  Diz a nota, se aprovou e quais perguntas errou, e nunca qual era a alternativa correta.
  Toca: `POST /api/v1/atividades/{atividadeId}/quiz/tentativas`

- [ ] **5.8 Recusar tentativa mal formada**
  Tentativa com pergunta de outro quiz, pergunta repetida, pergunta faltando ou
  alternativa que não pertence à pergunta volta como erro de validação, não como nota zero
  silenciosa.
  Toca: `progresso`, `shared/exception/ApiExceptionHandler.java`

- [ ] **5.9 Trocar a regra de conclusão de atividade**
  Atividade com quiz conclui com aprovação no quiz, e atividade sem quiz conclui com o
  vídeo assistido. O registro de vídeo assistido não grava a conclusão sozinho.
  Toca: `progresso`, `CLAUDE.md` raiz, `progresso/CLAUDE.md`

- [ ] **5.10 Testar que o gabarito não vaza na tentativa**
  Verifica que nenhum campo da resposta da tentativa, nem da leitura do quiz, revela a
  alternativa correta.
  Toca: `src/test/java/.../progresso`

### Vídeo na atividade

- [ ] **8.1 Preencher `video_url` do conteúdo por migration**
  Os vídeos entram em migration nova, hospedados no YouTube como não listado. `V3` não
  pode ser editada, porque o Flyway valida o checksum do que já rodou.
  Toca: nova migration em `db/migration`, coluna `atividade.video_url`

- [ ] **8.2 Validar a URL do vídeo**
  Recusa URL fora do domínio de hospedagem, para o app não receber link quebrado ou de
  origem inesperada.
  Toca: `trilha`

- [ ] **8.3 Usar `duracao_segundos` na regra de vídeo assistido**
  A coluna existe e serve para o backend recusar um "assisti" que chegou antes de ser
  plausível assistir.
  Toca: coluna `atividade.duracao_segundos`, `progresso`

- [ ] **8.4 Ajustar o teste que presume vídeo nulo**
  O teste que garante que atividade sem vídeo não é erro afirma que o campo de vídeo vem
  nulo no conteúdo semeado. Preencher vídeo no seed quebra esse teste, então ele muda no
  mesmo diff.
  Toca: `src/test/java/.../trilha/QuizApiTest.java`

### Gamificação

- [ ] **6.1 Escrever a migration de pontuação, sequência e conquista**
  Cria as tabelas de pontos do integrante, sequência de dias e conquistas obtidas, sempre
  ligadas à empresa.
  Toca: nova migration em `db/migration`

- [ ] **6.2 Criar o pacote `gamificacao` com entidades e repositórios**
  Nasce com o seu próprio `CLAUDE.md`.
  Toca: `gamificacao`

- [ ] **6.3 Conceder o XP da atividade na conclusão**
  Usa a coluna `atividade.xp` para somar pontos quando a atividade conclui. Não inventa
  outra fonte de pontuação.
  Toca: `gamificacao`, coluna `atividade.xp`

- [ ] **6.4 Calcular a sequência de dias no fuso da empresa**
  Os carimbos são gravados em UTC, e a virada do dia acontece no fuso do cliente. Depende
  da coluna de fuso criada em 11.2.
  Toca: `gamificacao`

- [ ] **6.5 Semear o catálogo de conquistas por migration**
  As conquistas são conteúdo global, iguais para todas as empresas, e entram por migration
  como o resto do conteúdo.
  Toca: nova migration em `db/migration`

- [ ] **6.6 Implementar a concessão de conquista**
  Avalia as regras quando algo muda no progresso e concede a conquista uma única vez por
  integrante.
  Toca: `gamificacao`

- [ ] **6.7 Expor `GET /api/v1/ranking`**
  Devolve a classificação dos integrantes dentro da empresa de quem chamou, com desempate
  por quem chegou primeiro à pontuação e paginação obrigatória.
  Toca: `GET /api/v1/ranking`

- [ ] **6.8 Expor `GET /api/v1/integrantes/{integranteId}/gamificacao`**
  Devolve pontos, sequência atual e conquistas do integrante, para o app montar o perfil.
  Toca: `GET /api/v1/integrantes/{integranteId}/gamificacao`

- [ ] **6.9 Testar que ponto não é concedido duas vezes pela mesma atividade**
  Concluir de novo, ou refazer o quiz já aprovado, não soma XP de novo, senão o ranking
  vira farsa.
  Toca: `src/test/java/.../gamificacao`

- [ ] **6.10 Escrever `gamificacao/CLAUDE.md`**
  Registra onde mora a sequência, em que fuso, como o ranking é calculado e por que ponto
  é idempotente.
  Toca: `gamificacao/CLAUDE.md`

### Administração de conteúdo

- [ ] **9.1 Substituir o conteúdo de exemplo pelo real por migration**
  O conteúdo em banco é exemplo, com UUID fixo escrito à mão, e a descrição da trilha
  semeada diz ao app que ela é exemplo. A migration nova apaga essas linhas pelos UUIDs
  fixos e insere o conteúdo verdadeiro, sem tocar em `V3`.
  Toca: nova migration em `db/migration`

- [ ] **9.2 Expor o CRUD de trilha**
  Permite criar e editar trilha pela API em vez de por migration. Depende da autenticação:
  sem login, um CRUD administrativo fica aberto na internet.
  Toca: `POST`, `PUT` e `DELETE` em `/api/v1/trilhas`

- [ ] **9.3 Expor o CRUD de módulo e atividade**
  Mesma ideia, um nível abaixo, incluindo os campos de imagem, vídeo, duração e XP.
  Toca: `/api/v1/modulos`, `/api/v1/atividades`

- [ ] **9.4 Expor o CRUD de quiz, pergunta e alternativa**
  É o único lugar onde o gabarito entra e sai da API, e só para quem administra. A leitura
  do quiz pelo integrante continua sem `correta`.
  Toca: `/api/v1/quizzes`, `/api/v1/perguntas`, `/api/v1/alternativas`

- [ ] **9.5 Separar o DTO de escrita do DTO de leitura da alternativa**
  A escrita precisa do campo de alternativa correta e a leitura não pode nem ter o campo.
  Dois records distintos, nunca um só com o campo anulado, que é o jeito de vazar gabarito
  por esquecimento.
  Toca: `trilha/dto`

- [ ] **9.6 Validar exatamente uma alternativa correta por pergunta**
  Pergunta com zero ou duas corretas quebra a correção do quiz. A regra vale na escrita,
  não só no conteúdo semeado.
  Toca: `trilha`

- [ ] **9.7 Permitir reordenar módulo e atividade**
  A ordem define a trilha e tem unicidade por pai no banco. Reordenar é operação própria,
  senão qualquer troca de posição colide com o índice único.
  Toca: `trilha`, índices `modulo(trilha_id, ordem)` e `atividade(modulo_id, ordem)`

- [ ] **9.8 Ativar e desativar trilha sem apagar progresso**
  Trilha inativa desaparece da listagem, e o progresso de quem já a fez continua contando
  para pontuação e acompanhamento.
  Toca: coluna `trilha.ativa`, `progresso`

- [ ] **9.9 Impedir remoção de conteúdo com progresso registrado**
  Apagar atividade que tem progresso ou tentativa joga fora o histórico de quem estudou.
  Oferece desativação em vez de remoção.
  Toca: `trilha`, `progresso`

- [ ] **9.10 Registrar quem alterou o conteúdo**
  Guarda autor e momento de cada alteração, para dar para descobrir quem trocou o gabarito
  quando as notas caírem de repente.
  Toca: nova migration em `db/migration`, `trilha`

- [ ] **9.11 Testar que o CRUD exige papel administrativo**
  Prova que token de integrante comum recebe negação em toda escrita de conteúdo.
  Toca: `src/test/java/.../trilha`

## Fora do MVP. Acompanhamento

Fora do MVP. Depende de decidir quem enxerga mais de uma empresa.

Decisões que o épico carrega e que precisam sair antes do código:

- **Quem enxerga mais de uma empresa:** o acompanhamento é o único lugar que atravessa
  empresas. Falta decidir se existe um papel acima de `ADMIN` e como essa pessoa entra no
  sistema.
- **Definição de parado:** quantos dias sem concluir atividade contam como parado. O
  número nasce como propriedade configurável, não como constante no código.

- [ ] **7.1 Criar o pacote `acompanhamento`**
  Nasce com o seu próprio `CLAUDE.md`, lendo progresso e gamificação sem escrever neles.
  Toca: `acompanhamento`

- [ ] **7.2 Expor a listagem de integrantes com quem avançou e quem parou**
  Devolve, por empresa, cada integrante com onde está na trilha e desde quando não se
  move. É a tela principal da Naty.
  Toca: `GET /api/v1/acompanhamento/empresas/{empresaId}/integrantes`

- [ ] **7.3 Expor as métricas agregadas da empresa**
  Devolve adesão, percentual médio de conclusão e quantos pararam, para a Naty comparar
  clientes sem abrir integrante por integrante.
  Toca: `GET /api/v1/acompanhamento/empresas/{empresaId}/metricas`

- [ ] **7.4 Restringir o acompanhamento ao papel administrativo**
  É o único lugar que lê dado de mais de uma empresa.
  Toca: `acompanhamento`, `config/SecurityConfig.java`

- [ ] **7.5 Mostrar onde as pessoas travam**
  Devolve, por atividade, quantos chegaram e quantos passaram. É o que revela a atividade
  ou a pergunta que está barrando a empresa inteira.
  Toca: `GET /api/v1/acompanhamento/atividades`

- [ ] **7.6 Exportar a lista de integrantes em CSV**
  Quem acompanha vai querer mandar a lista para o time que fala com o cliente, sem copiar
  da tela.
  Toca: `GET /api/v1/acompanhamento/empresas/{empresaId}/integrantes.csv`

- [ ] **7.7 Criar os índices que o acompanhamento exige**
  As consultas cruzam progresso e tentativa por empresa e por data. Sem índice, o painel
  varre tabela cheia a cada abertura.
  Toca: nova migration em `db/migration`

- [ ] **7.8 Testar que integrante comum não acessa o acompanhamento**
  Prova que token de integrante recebe negação de acesso em todos os endpoints deste
  pacote.
  Toca: `src/test/java/.../acompanhamento`

- [ ] **7.9 Escrever `acompanhamento/CLAUDE.md`**
  Registra que este pacote só lê, que ele atravessa empresas de propósito e qual é a
  definição de parado.
  Toca: `acompanhamento/CLAUDE.md`

## Em paralelo. Endurecimento, observabilidade e dívidas

- [x] **10.1 Preencher `OpenApiConfig`**
  A classe não define título, versão, servidores nem agrupamento de tags, e o Swagger é o
  contrato do time do app.
  Toca: `config/OpenApiConfig.java`, `/v3/api-docs`

- [ ] **10.2 Padronizar `Empresa` com id gerado e timestamps automáticos**
  É a única entidade sem geração de id e sem os timestamps automáticos que as outras têm,
  então o id precisa ser preenchido à mão. Alinhar evita bug em quem criar empresa por
  código.
  Toca: `empresa/Empresa.java`

- [ ] **10.3 Trocar o assert frágil de `QuizApiTest`**
  O teste garante que o gabarito não vaza procurando o texto `true` no corpo da resposta.
  Qualquer campo booleano verdadeiro futuro quebra o teste sem nenhum vazamento real.
  Verificar a estrutura da resposta no lugar.
  Toca: `src/test/java/.../trilha/QuizApiTest.java`

- [ ] **10.4 Paginar as listagens**
  Ranking, integrantes e acompanhamento crescem com a empresa. Definir paginação padrão
  antes de a primeira empresa grande entrar.
  Toca: controllers de listagem

- [ ] **10.5 Criar `src/test/resources` com `application-test.yml`**
  Não existe recurso de teste, então os testes herdam o perfil de desenvolvimento. Um
  perfil de teste próprio deixa explícito o que roda no teste.
  Toca: `src/test/resources/application-test.yml`

- [ ] **10.6 Decidir o destino de `DataUtil`**
  É uma classe utilitária vazia em `shared`. Ou ganha o cálculo de data que a sequência de
  dias precisa, ou sai do projeto.
  Toca: `shared/util/DataUtil.java`

- [ ] **10.7 Expor métricas da aplicação**
  Publica métricas de requisição e de banco, para dar para responder "está lento onde" sem
  adivinhar.
  Toca: `application.yml`, `/actuator`

- [ ] **10.8 Padronizar log sem vazar credencial**
  Define o que é logado em cada camada e garante que senha, hash de senha e token de
  sessão não apareçam em log nem em mensagem de erro.
  Toca: `painel`, `usuario`, `shared`

- [ ] **10.9 Testar as regras de dependência entre pacotes**
  Faz o build falhar se `trilha` passar a depender de `progresso`, se algum pacote além de
  `painel` escrever em `usuario`, ou se um DTO de leitura ganhar campo de gabarito. Essas
  setas são fáceis de inverter por conveniência.
  Toca: `src/test/java`

- [ ] **10.10 Verificar a regra de comentários e a de escrita de documento no build**
  A busca por comentário explicativo, por marcador temporal no código e por narrativa de
  mudança nos documentos é item manual de checklist. Vira passo do build, senão depende de
  disciplina.
  Toca: `pom.xml`

- [ ] **10.11 Configurar integração contínua**
  Roda `./mvnw -B verify` em cada envio, com Docker disponível para os Testcontainers,
  para formatação e testes não dependerem de quem lembrou de rodar.
  Toca: configuração de CI

- [ ] **10.12 Correlacionar log por requisição**
  Injeta um identificador por chamada no log e devolve no cabeçalho, para conseguir seguir
  uma chamada do app até o erro.
  Toca: `config`, `shared`

- [ ] **10.13 Fechar o detalhe do Actuator em produção**
  O arquivo base mostra detalhe de saúde sempre, incluindo o estado do banco. Revisa o que
  fica exposto fora de desenvolvimento.
  Toca: `application.yml`, `application-prod.yml`

- [ ] **10.14 Definir retenção de dado operacional**
  Decide o que fazer com tentativa de quiz velha e com sessão expirada, para tabela de
  histórico não crescer para sempre.
  Toca: `progresso`, `usuario`

- [ ] **10.15 Expor a leitura de integrante em `UsuarioController`**
  O controller tem o caminho declarado e nenhum método mapeado, e `UsuarioResponse` e
  `UsuarioFiltro` são classes vazias. A tela de ranking e o acompanhamento precisam ler
  integrante, sempre filtrado por empresa. A escrita mora no painel.
  Toca: `GET /api/v1/usuarios`, `usuario/UsuarioController.java`
