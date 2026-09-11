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
- Migrations `V1`, `V2` e `V3` mais o seed de desenvolvimento, tratamento global de
  erro em formato único, Docker Compose e 15 testes de integração.
- Progresso na trilha: migration `V4`, pacote `progresso`, desbloqueio linear,
  `GET /api/v1/trilhas/{trilhaId}/progresso` e
  `POST /api/v1/atividades/{atividadeId}/video-assistido`. O integrante chega no
  cabeçalho `X-Integrante-Id`, resolvido em `usuario/IntegranteArgumentResolver`, e a
  senha já entra no corpo de `POST /api/v1/sessoes` sem ser verificada. Total de 30
  testes de integração.

Ganchos que já estão no banco e que o backlog aproveita em vez de recriar:
`atividade.xp` com valor 10, `quiz.nota_minima` com valor 70,
`atividade.duracao_segundos`, `atividade.video_url` hoje sempre nulo e
`usuario.payload` em jsonb.

## Ordem dos épicos

```
1. Naty API V3
        |
        +---> 2. Sincronização de integrantes
        |
        +---> 3. Autenticação e identidade
                     |
                     +---> 4. Progresso na trilha ---> 5. Quiz: tentativa e correção
                     |            |                              |
                     |            |                              v
                     |            +----------------------> 6. Gamificação
                     |            |                              |
                     |            +---> 8. Vídeo na atividade    v
                     |                                    7. Acompanhamento
                     +---> 9. Administração de conteúdo

10. Endurecimento e observabilidade: em paralelo, a qualquer momento
```

A autenticação vem antes do progresso de propósito. Pontuação, sequência e ranking
construídos sobre um e-mail não verificado nascem fraudáveis, e o CRUD de conteúdo
não pode ficar aberto na internet.

## 1. Integração com a Naty API V3

- [ ] **1.1 Adicionar Resilience4j ao `pom.xml`**
  Coloca a biblioteca de resiliência no projeto. Sem ela o cliente da Naty não tem
  retry nem circuit breaker, e uma instabilidade da Naty derruba a sincronização.
  Toca: `pom.xml`

- [ ] **1.2 Preencher `RestClientConfig` com base URL e timeout**
  Hoje é uma classe de configuração vazia. Passa a montar o `RestClient` que fala
  com a Naty, lendo a URL e o timeout de `NatyApiProperties`.
  Toca: `config/RestClientConfig.java`

- [ ] **1.3 Transformar `NatyUsuarioResponse` em record espelhando o JSON da Naty**
  Hoje é uma classe vazia. Vira um record com os campos que a Naty devolve, com os
  nomes que a Naty usa, sem traduzir nada para português aqui.
  Toca: `natyapi/dto/NatyUsuarioResponse.java`

- [ ] **1.4 Transformar `NatyPaginaResponse` em record de paginação**
  Hoje é uma classe vazia. Vira o envelope de página da Naty, para o cliente saber
  quantos registros existem e se há próxima página.
  Toca: `natyapi/dto/NatyPaginaResponse.java`

- [ ] **1.5 Implementar `NatyApiClient` com listagem paginada de usuários**
  É o único ponto do sistema que faz chamada HTTP para a Naty. Busca os integrantes
  de uma empresa percorrendo todas as páginas até o fim.
  Toca: `natyapi/NatyApiClient.java`

- [ ] **1.6 Injetar o token da empresa por requisição**
  O token da Naty mora em `empresa.naty_api_token`, um por cliente, e não no
  `application.yml`. O cliente precisa receber o token de quem está sincronizando, e
  esse valor nunca pode aparecer em log ou em resposta de erro.
  Toca: `natyapi/NatyApiClient.java`, coluna `empresa.naty_api_token`

- [ ] **1.7 Falhar com `NatyAuthenticationException` quando o token for vazio ou recusado**
  O token pode subir vazio de propósito, para a aplicação funcionar sem credencial.
  Nesse caso, e em 401 e 403, o erro tem que ser claro, não `NullPointerException`.
  Toca: `natyapi/NatyApiClient.java`

- [ ] **1.8 Traduzir 429 em `NatyRateLimitException` com a espera do header**
  Quando a Naty responde que estamos chamando demais, ela diz quanto esperar. Esse
  valor precisa chegar na exceção, porque ignorar e tentar de novo em seguida derruba
  a integração inteira.
  Toca: `natyapi/NatyApiClient.java`

- [ ] **1.9 Aplicar retry e circuit breaker no cliente**
  Faz falha momentânea de rede ser repetida e falha contínua abrir o circuito, em vez
  de cada chamada esperar o timeout inteiro.
  Toca: `natyapi/NatyApiClient.java`, `application.yml`

- [ ] **1.10 Implementar `NatyApiHealthIndicator`**
  Hoje é um componente vazio que não aparece em `/actuator/health`. Passa a dizer se
  a Naty responde, distinguindo "sem credencial configurada" de "Naty fora do ar", e
  sem expor token nem URL interna, porque esse endpoint é público.
  Toca: `health/NatyApiHealthIndicator.java`, `/actuator/health`

- [ ] **1.11 Testar o cliente contra um servidor HTTP falso**
  Cobre paginação com mais de uma página, token vazio, 401, 429 com espera e queda da
  Naty, sem depender da Naty real.
  Toca: `src/test/java/.../natyapi`

- [ ] **1.12 Atualizar `natyapi/CLAUDE.md` e `health/CLAUDE.md`**
  Tira das seções Estado atual o que deixou de ser stub, para o próximo a mexer não
  reimplementar o que já existe.
  Toca: `natyapi/CLAUDE.md`, `health/CLAUDE.md`

## 2. Sincronização de integrantes

- [ ] **2.1 Criar `SincronizacaoProperties` lendo `habilitada` e `cron`**
  As duas propriedades já existem no `application.yml` e nenhuma classe Java as lê
  hoje. Passa a existir um único lugar tipado que as expõe.
  Toca: `sincronizacao`, `application.yml`

- [ ] **2.2 Preencher `UsuarioMapper` convertendo `NatyUsuarioResponse` em `Usuario`**
  É a fronteira onde o JSON da Naty vira domínio nosso: nome, e-mail, identificador
  da Naty, perfil, status e último acesso.
  Toca: `usuario/UsuarioMapper.java`

- [ ] **2.3 Guardar o payload cru da Naty na coluna jsonb**
  Salva a resposta original em `usuario.payload`, para que campo novo da Naty não
  exija migration antes de podermos investigar um caso estranho.
  Toca: coluna `usuario.payload`

- [ ] **2.4 Implementar o upsert idempotente por `(empresa_id, naty_id)`**
  Rodar a sincronização duas vezes não pode duplicar integrante. A unicidade é o par
  empresa mais identificador da Naty, porque o mesmo identificador pode existir em
  duas empresas diferentes.
  Toca: `sincronizacao/SincronizacaoUsuarioService.java`, índice `usuario_empresa_naty_id_idx`

- [ ] **2.5 Tornar o lote tolerante a falha de um integrante**
  Um integrante com dado inválido não pode abortar a sincronização dos outros. A
  decisão de continuar precisa estar explícita no serviço.
  Toca: `sincronizacao/SincronizacaoUsuarioService.java`

- [ ] **2.6 Devolver relatório de criados, atualizados e falhos**
  Quem dispara a sincronização precisa saber o que aconteceu, quantos integrantes
  entraram, quantos mudaram e quais falharam e por quê.
  Toca: `sincronizacao`, `POST /api/v1/sincronizacoes/usuarios`

- [ ] **2.7 Expor `POST /api/v1/sincronizacoes/usuarios`**
  O controller já existe com o caminho declarado e nenhum método mapeado. Passa a
  aceitar o disparo manual, útil para colocar uma empresa nova em produção sem
  esperar o cron.
  Toca: `POST /api/v1/sincronizacoes/usuarios`

- [ ] **2.8 Ligar o agendamento respeitando `sincronizacao.habilitada`**
  Habilita o agendamento no `SchedulerConfig` e o `@Scheduled` no scheduler, mas só
  dispara quando a flag estiver ligada. Ela nasce desligada porque scheduler ligado
  por padrão bate na Naty no primeiro `docker compose up`.
  Toca: `config/SchedulerConfig.java`, `sincronizacao/SincronizacaoScheduler.java`

- [ ] **2.9 Respeitar a espera do rate limit entre páginas e empresas**
  Quando a Naty pedir para esperar, o lote espera o tempo que ela mandou antes de
  continuar, em vez de insistir e ser bloqueado.
  Toca: `sincronizacao/SincronizacaoUsuarioService.java`

- [ ] **2.10 Sincronizar todas as empresas ativas, cada uma com seu token**
  O cron percorre as empresas ativas e usa o token de cada uma. Empresa sem token
  configurado é registrada e pulada, não derruba o lote.
  Toca: `sincronizacao`, tabela `empresa`

- [ ] **2.11 Testar idempotência rodando a sincronização duas vezes**
  Prova que a segunda passada não cria linha nova e atualiza o que mudou, com a Naty
  simulada por servidor falso.
  Toca: `src/test/java/.../sincronizacao`

- [ ] **2.12 Filtrar por empresa a busca de integrante por e-mail**
  Hoje a consulta procura o e-mail sem filtrar empresa e devolve um único
  resultado. Com duas empresas sincronizadas, um e-mail repetido faz
  `POST /api/v1/sessoes` estourar em vez de resolver o integrante.
  Toca: `usuario/UsuarioRepository.java`, `POST /api/v1/sessoes`

- [ ] **2.13 Marcar o integrante que desapareceu da Naty**
  A Naty é a fonte da verdade de quem existe. Quem sai de lá precisa parar de
  aparecer no ranking, sem apagar o registro, porque progresso e pontuação apontam
  para ele.
  Toca: nova migration em `db/migration`, tabela `usuario`

- [ ] **2.14 Persistir o histórico de execução da sincronização**
  Guarda início, fim, contadores e erros de cada execução, para conferir depois se o
  cron rodou e o que ele fez, sem depender de log que já rotacionou.
  Toca: nova migration em `db/migration`, `sincronizacao`

- [ ] **2.15 Impedir duas sincronizações simultâneas**
  O cron e o disparo manual chamam o mesmo serviço e podem colidir no mesmo
  integrante. Serializa a execução.
  Toca: `sincronizacao/SincronizacaoUsuarioService.java`

- [ ] **2.16 Preparar a criação de empresa por código**
  A entidade `Empresa` não gera id nem preenche os carimbos de data, então
  persistir uma empresa nova pelo JPA falha hoje. Sem isso não há caminho para
  colocar um cliente novo em produção fora do SQL escrito à mão.
  Toca: `empresa/Empresa.java`, tabela `empresa`

- [ ] **2.17 Atualizar `sincronizacao/CLAUDE.md`**
  Registra o que virou real, a política de erro do lote e como o rate limit é tratado.
  Toca: `sincronizacao/CLAUDE.md`

## 3. Autenticação e identidade

- [ ] **3.1 Decidir o mecanismo de autenticação e registrar a decisão**
  Hoje qualquer pessoa entra digitando um e-mail. Como o sistema nunca cadastra
  usuário próprio, a decisão é entre validar credencial na Naty, mandar código de
  acesso por e-mail ou emitir token próprio após confirmação. A escolha muda todas as
  tasks seguintes deste grupo.
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

- [ ] **3.9 Separar o papel administrativo do campo `perfil` da Naty**
  O campo `usuario.perfil` traz `admin`, `supervisor` e `user` da Naty API e não vale
  como permissão no treinamento. Quem pode administrar conteúdo e ver acompanhamento
  precisa de um papel nosso, explícito.
  Toca: `usuario`, nova migration

- [ ] **3.10 Testar que integrante de uma empresa não lê dado de outra**
  Cria duas empresas com integrantes e prova que o token de uma não alcança nada da
  outra, em todos os endpoints por empresa.
  Toca: `src/test/java/.../usuario`

- [ ] **3.11 Registrar o último acesso sem escrever em `usuario`**
  Só a sincronização escreve na tabela `usuario`. O último login do integrante
  precisa morar na tabela de sessão, ou a regra tem que ser reescrita de propósito no
  `CLAUDE.md` do pacote.
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

## 7. Acompanhamento pela Naty

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

- [ ] **7.5 Restringir o acompanhamento ao papel da Naty**
  Este é o único lugar que lê dado de mais de uma empresa. Só o papel nosso definido
  na etapa de autenticação entra aqui, e não o campo `usuario.perfil` que vem da Naty
  API.
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
  Publica métricas de requisição, banco e chamada à Naty, para dar para responder
  "está lento onde" sem adivinhar.
  Toca: `application.yml`, `/actuator`

- [ ] **10.9 Padronizar log sem vazar credencial**
  Define o que é logado em cada camada e garante que `empresa.naty_api_token` e token
  de sessão nunca apareçam em log nem em mensagem de erro.
  Toca: `natyapi`, `sincronizacao`, `shared`

- [ ] **10.10 Repassar `SINCRONIZACAO_CRON` e `NATY_API_TIMEOUT` no `docker-compose.yml`**
  As duas variáveis existem no `.env.example` e no `application.yml`, mas o compose
  não as passa para o contêiner, então mudar o `.env` não muda nada em Docker.
  Toca: `docker-compose.yml`

- [ ] **10.11 Testar as regras de dependência entre pacotes**
  Faz o build falhar se `trilha` passar a depender de `progresso`, se algum pacote
  além de `sincronizacao` escrever em `usuario`, ou se um DTO de leitura ganhar campo
  de gabarito. Essas setas são fáceis de inverter por conveniência.
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
  O arquivo base mostra detalhe de saúde sempre, e o indicador da Naty passa por ali.
  Revisa o que fica exposto fora de desenvolvimento.
  Toca: `application.yml`, `application-prod.yml`

- [ ] **10.16 Definir retenção de dado operacional**
  Limpa execução de sincronização antiga e decide o que fazer com tentativa velha,
  para tabela de histórico não crescer para sempre.
  Toca: `sincronizacao`, `progresso`

- [ ] **10.17 Expor a leitura de integrante em `UsuarioController`**
  O controller existe com o caminho declarado e nenhum método mapeado, e
  `UsuarioResponse` e `UsuarioFiltro` são classes vazias. A tela de ranking e o
  acompanhamento precisam ler integrante, sempre filtrado por empresa e sem nunca
  criar, alterar ou apagar, porque a fonte da verdade é o Naty App.
  Toca: `GET /api/v1/usuarios`, `usuario/UsuarioController.java`

## Decisões em aberto

Cada linha precisa de resposta antes de codar o épico correspondente:

- **Paginação da Naty API (1.5):** página e tamanho, deslocamento e limite, ou
  cursor. Muda a assinatura do cliente e o envelope de página, e precisa de uma
  chamada real registrada antes do código.
- **Onde o token entra na requisição (1.6):** cabeçalho `Authorization` com bearer ou
  cabeçalho proprietário da Naty.
- **`naty.api.token` do `application.yml` (1.6):** ele conflita com o token por
  empresa. Ou vira token apenas do indicador de saúde e do teste de fumaça, ou sai.
- **Integrante que saiu da Naty (2.13):** coluna de ativo, data de remoção ou valor
  no campo de status. Progresso, ranking e acompanhamento leem isso, então é decisão
  anterior a eles.
- **Qual empresa no login (3.1):** o mesmo e-mail pode existir em duas empresas. O
  app envia a empresa, o token da Naty determina a empresa, ou o login pede um código
  do cliente.
- **Token de sessão (3.5):** token assinado sem estado, mais simples, ou token opaco
  em tabela, que permite revogar.
- **Autenticação (3.1):** validar credencial na Naty, código de acesso por e-mail ou
  token próprio após confirmação. Nós não podemos cadastrar usuário.
- **Papel administrativo (3.9):** quem define que alguém administra conteúdo, se o
  campo `perfil` da Naty não serve como permissão.
- **Fuso da sequência (6.4):** fuso fixo do produto, fuso da empresa ou fuso do
  integrante.
- **Ranking (6.8 e 6.10):** calculado a cada consulta ou materializado em tabela, e
  qual o período, sempre ou por semana.
- **Tentativas de quiz (5.5 e 5.6):** limite e espera entre tentativas, e se a nota
  que vale é a última ou a melhor.
- **Gabarito depois da aprovação (5.7):** revela a resposta certa ou não.
- **Hospedagem de vídeo (8.1):** plataforma pública, plataforma privada ou
  armazenamento próprio.
- **Quem é a Naty (7.5):** como uma pessoa da Naty entra no sistema, se ela não é
  integrante de nenhuma empresa cliente.
- **Definição de parado (7.1):** quantos dias sem concluir atividade contam como
  parado.
- **Empresa nova em produção (2.10):** como uma empresa e seu token entram no
  sistema, se não existe CRUD de empresa e ela nasce por seed.
