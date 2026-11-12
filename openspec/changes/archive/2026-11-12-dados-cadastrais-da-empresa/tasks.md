# Tarefas

Pacote raiz dos caminhos abaixo: `src/main/java/com/projetointegrador/natysync`. Testes em
`src/test/java/com/projetointegrador/natysync`. Toda execução de teste passa pelo subagent
`test-runner`, com Docker ativo. Nenhum texto gerado leva travessão.

## 1. Preparação, levantamento e parada de revisão

- [x] 1.1 Rodar `git fetch origin` e confirmar que a branch `feat/swagger-descritivo` está
  integrada em `origin/main` (`git branch -r --merged origin/main` lista
  `origin/feat/swagger-descritivo`, ou o PR dela consta como merged). Se não estiver, PARAR e
  perguntar ao Breno. Se estiver, criar `feat/dados-cadastrais-da-empresa` a partir de
  `origin/main`. Aceite: `git log -1 origin/main` é ancestral de `HEAD` e `git status` limpo.
- [x] 1.2 Ler `painel/CLAUDE.md`, `empresa/CLAUDE.md`, `usuario/CLAUDE.md` e
  `config/CLAUDE.md`. Aceite: as armadilhas de normalização no construtor, `toString` com
  senha mascarada, `ViolacaoDeConstraint` e ordem fixa da listagem estão anotadas para as
  tarefas seguintes.
- [x] 1.3 Levantamento com o subagent `Explore`, sem editar nada. Perguntas, cada uma com
  arquivo e linha na resposta:
  1. Onde `EmpresaRequest`, `EmpresaResponse`, `EmpresaMapper` e a entidade `Empresa` são
     usados em `src/main` e em `src/test`.
  2. Quais testes enviam corpo de criação ou alteração de empresa (JSON literal, record ou
     helper) e quantos pontos são.
  3. Onde `SessaoResponse` é construído e quais testes leem os campos dele.
  4. Assinatura do método de `IntegranteService` que cadastra conta, e se ele exige algo que
     só o controller resolve.
  5. Como `EmpresaService` e `IntegranteService` fazem `flush` e traduzem
     `DataIntegrityViolationException` com `ViolacaoDeConstraint`.
  6. Se o Jackson do projeto recusa propriedade desconhecida no corpo
     (`FAIL_ON_UNKNOWN_PROPERTIES`, `spring.jackson.*` em `application*.yml`, ou teste que
     prova isso).
  7. Como os testes limpam `empresa` e `usuario` (SQL de limpeza, classe base) e quais
     inserem em `empresa` ou em `usuario` por SQL.
  8. Qual padrão os testes usam para provar que senha não aparece em log
     (`OutputCaptureExtension` ou outro).
  9. Onde `IntegranteService` desativa conta e altera papel, com a assinatura de cada
     método, e como o `ADMIN` e o `NATY` chegam a eles.
  10. Impacto do telefone novo em `usuario`: usos de `Usuario`, `IntegranteCriacaoRequest`,
      `IntegranteAlteracaoRequest`, `IntegranteResponse` e `IntegranteMapper`, e testes que
      montam esses corpos ou leem a resposta.
  11. Impacto no seed: `R__seed_*.sql` que inserem em `empresa` e em `usuario`, e testes que
      dependem do seed, como `TravaDaContaDeSeedTest`.
  12. Impacto no bootstrap: leitura de `app.bootstrap.naty.*` em `BootstrapNaty`, validação
      de propriedades obrigatórias, `application*.yml`, `.env.example`, README e todo teste
      que sobe o contexto com bootstrap obrigatório ou que testa `BootstrapNaty`.
  Aceite: respostas registradas como bullets simples logo abaixo desta tarefa, sem caixa de
  seleção.
  - `EmpresaRequest`: `EmpresaController` (`@RequestBody`), `EmpresaService`, `EmpresaServiceTest`
    (4 construções posicionais). `EmpresaResponse` e `EmpresaMapper` (MapStruct) em
    `EmpresaService` e `EmpresaController`. `Empresa` também em `Usuario`, `IntegranteService`,
    `BootstrapNaty`, `RecompensaDoEstudo`, `CalculadoraDeAcompanhamento`,
    `AcompanhamentoService` e `IntegracaoTest.criarEmpresa` (persiste por JPA, sem CNPJ).
  - Corpo de empresa em teste: `EmpresaPainelApiTest` (helper `criarPelaApi` mais 5 `PUT`),
    `AutorizacaoPainelTest` (corpo literal) e 4 construções em `EmpresaServiceTest`. São 7
    pontos literais e 4 construções de record.
  - `SessaoResponse` é construído só em `SessaoService.respostaDe`, chamado pela emissão e pela
    renovação. Os testes leem por `JsonNode`, nenhum compara o conjunto de chaves.
  - `IntegranteService.criar(UUID empresaId, IntegranteCriacaoRequest)`, sem papel do chamador.
    A empresa é resolvida dentro do serviço, e o controller só passa o `empresaId`.
  - `EmpresaService` usa `saveAndFlush` na criação e na alteração e traduz violação só na
    exclusão. `IntegranteService.salvar` faz `saveAndFlush` e traduz `usuario_email_idx` por
    `ViolacaoDeConstraint`, que hoje reconhece só essa constraint.
  - O Jackson 3 do projeto tem `FAIL_ON_UNKNOWN_PROPERTIES` falso e nenhuma configuração
    contrária: propriedade desconhecida no corpo é ignorada.
  - A limpeza de teste apaga `usuario` e `empresa` por `empresa_id` em `IntegracaoTest`. Nenhum
    teste insere em `empresa` ou `usuario` por SQL, só atualiza `usuario`.
  - Senha fora do log: `LogSemCredencialTest` com `OutputCaptureExtension`, nível `DEBUG` e
    `doesNotContain` sobre a senha, o hash e o token.
  - `IntegranteService.alterar`, `definirSenha` e `definirAtivo` passam por
    `travarIntegranteAdministravel`, que trava a linha do alvo. `NATY` e `ADMIN` chegam pelos
    dois controllers de integrante. Não há contagem de `ADMIN` ativo.
  - Telefone em `usuario` entra por `IntegranteMapper` (MapStruct, por nome), em
    `IntegranteCriacaoRequest`, `IntegranteAlteracaoRequest` e `IntegranteResponse`. No `PUT`,
    nome, e-mail e papel são sobrescritos e `perfil` ausente mantém o valor.
  - Seed `R__seed_empresa_exemplo.sql` faz upsert de duas empresas e três contas por id fixo.
    A próxima migration livre é `V15`.
  - `BootstrapNaty` lê `app.bootstrap.naty.*` por `@Value`. `application.yml`,
    `application-prod.yml` e `.env.example` carregam as variáveis. `BootstrapNatyTest` constrói o
    componente com construtor posicional. Nenhum teste de contexto sobe com bootstrap
    obrigatório.
- [x] 1.4 Decidir, com base em 1.3, e registrar abaixo desta tarefa:
  - Se a pergunta 2 achar 5 pontos ou mais, criar o helper de teste
    `painel/CorpoDeEmpresa` (tarefa 4.6); com menos, editar cada ponto.
  - Se a pergunta 4 ou a 9 mostrar que cadastrar a conta `ADMIN` a partir de `EmpresaService`
    ou checar o último `ADMIN` exige mudar a assinatura pública de `IntegranteService` além
    de um método novo, PARAR e registrar para a revisão humana da tarefa 1.5.
  - Pela pergunta 6, qual resposta o `PUT` com bloco `administrador` dá (ignora ou 400), e
    ajustar o cenário "Alteração com bloco administrador" do spec delta se for 400.
  - Como o telefone entra em `IntegranteAlteracaoRequest` (campo ausente mantém ou limpa o
    valor), conforme a semântica dos demais campos opcionais.
  - A lista de testes, seeds e arquivos de configuração afetados por telefone, CNPJ e razão
    social, para alimentar os grupos 2, 4 e 6.
  Aceite: as decisões escritas abaixo da tarefa.
  - Helper de teste `painel/CorpoDeEmpresa`: são 7 pontos literais, então a tarefa 4.6 cria o
    helper.
  - Sem mudança de assinatura pública de `IntegranteService`: o cadastro do `ADMIN` chama
    `criar(UUID, IntegranteCriacaoRequest)` de dentro da transação de `EmpresaService`, e a regra
    do último `ADMIN` entra por método novo em `UsuarioRepository` e checagem interna em
    `alterar` e `definirAtivo`, serializada por `travarTodosDaEmpresa`. Nenhuma parada.
  - `PUT` com o bloco `administrador` altera a empresa e ignora o bloco, porque o Jackson
    ignora propriedade desconhecida. O cenário do spec fica como está.
  - Telefone em `IntegranteAlteracaoRequest`: ausente mantém o valor atual, como `perfil`.
  - Afetados: `EmpresaPainelApiTest`, `AutorizacaoPainelTest`, `EmpresaServiceTest` (4
    construções e o construtor posicional de `EmpresaService`), `BootstrapNatyTest`
    (construtor posicional), testes de integrante que montam corpo, o seed de desenvolvimento,
    `application.yml`, `application-prod.yml`, `.env.example`, `docs/regras.md` e o `CLAUDE.md`
    raiz.
- [x] 1.5 PARADA PARA REVISÃO HUMANA. Apresentar ao Breno o resultado de 1.3 e 1.4 e as
  decisões D1 (responsável é a conta `ADMIN`, com telefone em `usuario`), D2 (CNPJ e razão
  social nulos no banco, obrigatórios na API), D5 (bloco `administrador` obrigatório na
  criação), D9 (empresa sempre com `ADMIN` ativo) e D10 (bootstrap com CNPJ e razão social)
  de `design.md`. Não iniciar o grupo 2 sem a aprovação dele. Mudança de decisão volta para
  `/opsx:update` antes do código.

## 2. Banco, entidades, seed e bootstrap

- [x] 2.1 Criar `src/main/resources/db/migration/V15__dados_cadastrais_da_empresa.sql`:
  ```sql
  alter table empresa
    add column cnpj varchar(14),
    add column razao_social varchar(255);
  alter table empresa
    add constraint empresa_cnpj_formato_check check (cnpj ~ '^[0-9A-Z]{12}[0-9]{2}$');
  create unique index empresa_cnpj_idx on empresa (cnpj);
  alter table usuario
    add column telefone varchar(13);
  alter table usuario
    add constraint usuario_telefone_check check (telefone ~ '^[0-9]{10,13}$');
  ```
  Conferir antes que `V15` é o próximo número livre. Nenhuma coluna é `not null`, porque a
  limpeza dos testes e o seed inserem por SQL. Aceite: test-runner com
  `./mvnw -B test -Dtest='**/painel/**'` sobe o contexto sem erro de Flyway.
- [x] 2.2 Em `empresa/Empresa.java`, mapear `cnpj` e `razaoSocial` (`String`, nuláveis) com
  getter e setter, no mesmo estilo dos campos existentes. Em `usuario/Usuario.java`, mapear
  `telefone` (`String`, nulável). Aceite: `./mvnw -B test-compile` passa e o test-runner da
  tarefa 2.1 continua verde (`ddl-auto: validate`).
- [x] 2.3 Reescrever `src/main/resources/db/seed-dev/R__seed_empresa_exemplo.sql` com dados
  válidos, incluindo `cnpj` e `razao_social` no `insert` e no `on conflict ... do update` de
  `empresa` e `telefone` no de `usuario`: `Empresa Exemplo` com CNPJ `11222333000181`, razão
  social `Empresa Exemplo Ltda` e conta `ADMIN` com telefone `11987654321`; `Naty` com CNPJ
  `48055059000103` e razão social `Naty App Oficial LTDA`. Toda empresa de cliente do seed
  tem ao menos uma conta `ADMIN` ativa, e a empresa interna da Naty tem a conta `NATY`, que
  `TravaDaContaDeSeed` já cobre. Aceite: test-runner da tarefa 2.1 verde e
  `TravaDaContaDeSeedTest` verde.
- [x] 2.4 Em `painel/BootstrapNaty.java` e em `src/main/resources/application.yml`, ler
  `app.bootstrap.naty.cnpj` (`${APP_BOOTSTRAP_NATY_CNPJ:}`) e
  `app.bootstrap.naty.razao-social` (`${APP_BOOTSTRAP_NATY_RAZAO_SOCIAL:}`), gravá-los na
  empresa interna, normalizar o CNPJ com `Cnpj.normalizar` e recusar a subida quando o
  bootstrap é obrigatório e algum dos dois falta ou o CNPJ não passa em `Cnpj.valido`, com a
  mesma mensagem fixa e o mesmo mecanismo das demais variáveis. Atualizar `.env.example`,
  README e `docs/` com as duas variáveis. Aceite: `./mvnw -B test-compile` passa.
- [x] 2.5 Ajustar os testes de bootstrap e todo teste que sobe o contexto com bootstrap
  obrigatório, conforme os pontos achados na pergunta 12 de 1.3, e cobrir: bootstrap
  obrigatório sem CNPJ recusa subir, sem razão social recusa subir, com CNPJ inválido recusa
  subir, com os dois válidos grava empresa com CNPJ normalizado e razão social. Aceite:
  test-runner com `./mvnw -B test -Dtest='**/painel/**'` verde.
- [x] 2.6 Atualizar `empresa/CLAUDE.md` com `cnpj` e `razaoSocial`, o índice único de CNPJ e
  o fato de as duas colunas serem nulas no banco, e `usuario/CLAUDE.md` com a coluna
  `telefone` nula. Aceite: varredura de travessão vazia
  (`git diff | grep -n "$(python3 -c 'print(chr(8212))')"`).
- [x] 2.7 Pendência da confirmação do CNPJ da Naty: o Breno confirma na Receita Federal que
  `48.055.059/0001-03` é o CNPJ de `Naty App Oficial LTDA` antes de configurá-lo em
  produção. Aceite: confirmação do Breno registrada na tarefa 9.4. Confirmado pelo Breno em consulta
  da Serasa Experian: CNPJ `48.055.059/0001-03`, razão social `NATY APP OFICIAL LTDA - ME`.

## 3. CNPJ

- [x] 3.1 Criar `painel/Cnpj.java`, classe final com construtor privado e dois métodos
  estáticos: `normalizar(String)` (devolve `null` para `null`; remove `.`, `/`, `-` e
  espaços; converte para maiúsculas com `Locale.ROOT`) e `valido(String)` (regra de D3 em
  `design.md`: formato `^[0-9A-Z]{12}[0-9]{2}$`, caracteres não todos iguais, valor do
  caractere igual a ASCII menos 48, pesos `5,4,3,2,9,8,7,6,5,4,3,2` e
  `6,5,4,3,2,9,8,7,6,5,4,3,2`, resto menor que 2 vira 0). Sem comentário explicativo.
- [x] 3.2 Criar `painel/CnpjTest.java` (JUnit puro, sem Spring) com: `11222333000181`
  válido, `48055059000103` válido, `12ABC34501DE35` válido, `11222333000182` inválido,
  `00000000000000` inválido, `AAAAAAAAAAAA00` inválido, 13 caracteres inválido, letra nas
  duas últimas posições inválido, `normalizar("12.abc.345/01de-35")` igual a
  `12ABC34501DE35`, `normalizar(null)` nulo. Aceite: test-runner com
  `./mvnw -B test -Dtest='CnpjTest'` verde.

## 4. Dados cadastrais no contrato de empresa e telefone no integrante

- [x] 4.1 Em `painel/dto/EmpresaRequest.java`, adicionar `cnpj` (`@NotBlank`) e `razaoSocial`
  (`@NotBlank`, `@Size(max = 255)`). No construtor: `cnpj = Cnpj.normalizar(cnpj)` e `trim`
  da razão social. `@AssertTrue(message = "cnpj invalido") isCnpjValido()`: verdadeiro quando
  nulo ou vazio (o `@NotBlank` cobre), senão `Cnpj.valido(cnpj)`, com
  `@Schema(hidden = true)`. Aceite: `./mvnw -B test-compile` passa.
- [x] 4.2 Incluir `cnpj` e `razaoSocial` em `painel/dto/EmpresaResponse.java` e ajustar
  `painel/EmpresaMapper.java` para os dois sentidos. Aceite: `./mvnw -B test-compile` passa.
- [x] 4.3 Em `painel/dto/IntegranteCriacaoRequest.java` e em
  `painel/dto/IntegranteAlteracaoRequest.java`, adicionar `telefone` opcional: no construtor,
  só dígitos (`replaceAll("\\D", "")`) e vazio vira `null`; `@AssertTrue isTelefoneValido`
  (nulo ou de 10 a 13 dígitos) com `@Schema(hidden = true)`. Incluir `telefone` em
  `painel/dto/IntegranteResponse.java`, em `painel/IntegranteMapper.java` e no ponto de
  gravação de `painel/IntegranteService.java`, conforme a decisão de 1.4 para a alteração.
  Aceite: `./mvnw -B test-compile` passa.
- [x] 4.4 Em `painel/CodigoDeConflito.java`, criar `CNPJ_JA_CADASTRADO`. Em
  `painel/ViolacaoDeConstraint.java`, reconhecer `empresa_cnpj_idx` pelo mesmo mecanismo de
  `usuario_email_idx`. Em `painel/EmpresaService.java`, na criação e na alteração, gravar os
  campos novos, forçar `flush` e traduzir a violação de `empresa_cnpj_idx` em 409
  `CNPJ_JA_CADASTRADO` no formato único de erro; outra violação continua subindo.
- [x] 4.5 Em `painel/EmpresaController.java`, documentar no Swagger os campos novos e a
  resposta 409 `CNPJ_JA_CADASTRADO` em `POST` e `PUT`, no padrão de anotação já usado no
  arquivo. Documentar `telefone` nas rotas de integrante. Aceite: test-runner do pacote
  `painel` verde depois da 4.6.
- [x] 4.6 Ajustar os testes existentes que montam corpo de empresa ou de integrante conforme
  a decisão de 1.4 (helper `CorpoDeEmpresa` com CNPJ válido distinto por chamada, ou edição
  ponto a ponto). Aceite: test-runner com `./mvnw -B test -Dtest='**/painel/**'` e
  `./mvnw -B test -Dtest='**/acompanhamento/**'` verdes.
- [x] 4.7 Em `painel/EmpresaPainelApiTest.java`, um teste por cenário do requisito
  "Cadastro de empresa pelo papel NATY" (criação completa, CNPJ ausente, razão social
  ausente, empresa gravada sem CNPJ por SQL lida com campos nulos), "CNPJ válido e
  normalizado" (máscara, alfanumérico, dígito errado, repetidos) e "CNPJ único entre as
  empresas" (criação, alteração para CNPJ alheio, alteração com o próprio CNPJ). Aceite:
  test-runner com `./mvnw -B test -Dtest='EmpresaPainelApiTest'` verde.
- [x] 4.8 Nos testes de integrante de `painel`, cobrir telefone opcional: criação sem
  telefone, com telefone mascarado guardado só com dígitos, com 9 e com 14 dígitos recusado,
  alteração conforme a decisão de 1.4 e `IntegranteResponse` devolvendo o telefone. Aceite:
  test-runner com `./mvnw -B test -Dtest='**/painel/**'` verde.
- [x] 4.9 Atualizar `painel/CLAUDE.md` (CNPJ normalizado no construtor, `Cnpj`,
  `CNPJ_JA_CADASTRADO` por `empresa_cnpj_idx`, responsável é a conta `ADMIN`, telefone
  normalizado e opcional no integrante, bootstrap com CNPJ e razão social) e
  `docs/regras.md` (CNPJ único entre empresas, responsável é o `ADMIN`). Aceite: varredura
  de travessão vazia e texto sem verbo de mudança nem marcador de tempo.

## 5. Busca na listagem de empresas

- [x] 5.1 Em `empresa/EmpresaRepository.java`, criar `Page<Empresa> buscar(String texto,
  String cnpjParcial, Pageable pagina)` com `@Query` JPQL:
  `lower(e.nome) like :texto escape '\'` ou `lower(e.razaoSocial) like :texto escape '\'` ou
  (`:cnpjParcial` não nulo e `e.cnpj like :cnpjParcial escape '\'`). Os parâmetros chegam
  já com `%` nas pontas e com `%`, `_` e `\` da entrada escapados.
- [x] 5.2 Em `painel/EmpresaService.java`, receber `busca` opcional na listagem: nula ou em
  branco usa a listagem atual; senão monta `texto` (minúsculas, `Locale.ROOT`, escapado) e
  `cnpjParcial` (`Cnpj.normalizar`, escapado, `null` quando vazio) e chama `buscar` com a
  mesma ordenação fixa por nome e identificador. Em `painel/EmpresaController.java`, aceitar
  `@RequestParam(required = false) String busca` com `@Parameter` descrito no Swagger.
- [x] 5.3 Em `painel/EmpresaPainelApiTest.java`, testes dos cenários do requisito "Busca na
  listagem de empresas" mais: busca com `%` não devolve todas, busca vazia igual à listagem
  sem busca, ordem por nome mantida com busca, total de páginas correto com busca. Aceite:
  test-runner com `./mvnw -B test -Dtest='EmpresaPainelApiTest'` verde.
- [x] 5.4 Registrar em `painel/CLAUDE.md` o parâmetro `busca` e o escape dos curingas.
  Aceite: varredura de travessão vazia.

## 6. Criação de empresa com o primeiro ADMIN

- [x] 6.1 Criar `painel/dto/AdministradorInicialRequest.java`, record com `nome`
  (`@NotBlank`, `@Size(max = 255)`), `email` (`@NotBlank`, `@Email`, `@Size(max = 255)`),
  `senha` (`@NotBlank`, `@Size(min = SenhaRequest.TAMANHO_MINIMO)`) e `telefone`
  (`@NotBlank`), `@AssertTrue isSenhaDentroDoLimite` com `SenhaRequest.cabeNoBcrypt`,
  `@AssertTrue isTelefoneValido` (10 a 13 dígitos), e-mail normalizado e telefone só com
  dígitos no construtor, e `toString` com `senha=***`, igual a `IntegranteCriacaoRequest`.
- [x] 6.2 Criar `painel/dto/EmpresaCriacaoRequest.java` com os mesmos campos, validações e
  normalizações de `EmpresaRequest` mais `@NotNull @Valid AdministradorInicialRequest
  administrador` e `toString` que não imprime a senha. Trocar o corpo do `POST` de
  `painel/EmpresaController.java` para `EmpresaCriacaoRequest`, documentando o bloco e a
  resposta 409 `EMAIL_JA_CADASTRADO` no Swagger. O `PUT` continua com `EmpresaRequest`.
  Ajustar `EmpresaMapper` para o novo request.
- [x] 6.3 Em `painel/EmpresaService.java`, no método de criação já `@Transactional`, depois
  de gravar a empresa, cadastrar a conta pelo método de `IntegranteService` identificado em
  1.3, com papel `ADMIN`, telefone e perfil padrão. A exceção de e-mail repetido e a de CNPJ
  repetido sobem sem captura para o rollback desfazer a empresa e a conta.
- [x] 6.4 Em `painel/EmpresaPainelApiTest.java`, um teste por cenário do requisito "Criação
  de empresa com o primeiro ADMIN": login da conta criada devolve papel `ADMIN` e a
  `empresaId` nova; bloco ausente devolve 400 e nada é criado; cada dado do administrador
  ausente devolve 400 e nada é criado; telefone com máscara guardado só com dígitos; telefone
  com 9 e com 14 dígitos devolve 400; e-mail repetido devolve 409 e a contagem de empresas
  não muda; CNPJ repetido com administrador válido devolve 409 e nenhuma conta é criada;
  senha com 7 caracteres e com 73 bytes devolve 400 e nada é criado; `PUT` com bloco não cria
  conta (ou 400, conforme 1.4); senha ausente da resposta e do log pelo padrão achado na
  pergunta 8 de 1.3. Aceite: test-runner com `./mvnw -B test -Dtest='**/painel/**'` verde.
- [x] 6.5 Registrar em `painel/CLAUDE.md` o bloco `administrador` obrigatório, a transação
  única e o `toString` mascarado. Aceite: varredura de travessão vazia.

## 7. Empresa sempre com ADMIN ativo

- [x] 7.1 Em `painel/CodigoDeConflito.java`, criar `ULTIMO_ADMINISTRADOR`. Em
  `painel/IntegranteService.java`, nos pontos achados na pergunta 9 de 1.3, recusar com
  `409 ULTIMO_ADMINISTRADOR` desativar conta ou alterar o papel da conta que é o último
  `ADMIN` ativo da empresa, contando só contas `ADMIN` ativas da mesma empresa e excluindo a
  conta alterada. A conta fica como estava. Desativar a empresa em `EmpresaService` não passa
  pela checagem. Se isso exigir mudar a interface pública de `IntegranteService` além de
  método novo, PARAR e registrar na revisão humana 1.5. Aceite: `./mvnw -B test-compile`
  passa.
- [x] 7.2 Em `painel/IntegranteService.java`, tratar a concorrência: duas desativações
  simultâneas dos dois últimos `ADMIN` ativos não podem deixar a empresa sem nenhum. Usar
  trava pessimista na linha da empresa ou consulta equivalente dentro da transação, e
  registrar a escolha em `painel/CLAUDE.md`. Aceite: teste de concorrência no padrão dos
  testes existentes do projeto, ou, se não houver padrão, tarefa registrada em
  `docs/backlog.md`.
- [x] 7.3 Em `painel/IntegranteController.java` e na documentação Swagger das rotas de
  desativação e de alteração de integrante, documentar a resposta 409 `ULTIMO_ADMINISTRADOR`.
- [x] 7.4 Testes por cenário do requisito "Empresa sempre com ADMIN ativo": desativar o
  último `ADMIN` por `NATY`, o próprio `ADMIN` recebendo 403, rebaixar o último `ADMIN`, desativar um
  `ADMIN` havendo outro ativo, outro `ADMIN` inativo não conta, desativar a empresa inteira
  com um único `ADMIN`. Aceite: test-runner com `./mvnw -B test -Dtest='**/painel/**'` verde.
- [x] 7.5 Registrar em `painel/CLAUDE.md` a regra do último `ADMIN` e o código
  `ULTIMO_ADMINISTRADOR`. Aceite: varredura de travessão vazia e texto sem verbo de mudança
  nem marcador de tempo.

## 8. Papel na resposta de sessão

- [x] 8.1 Em `usuario/dto/SessaoResponse.java`, adicionar `Papel papel` com `@Schema`
  (descrição "Papel da conta: INTEGRANTE, ADMIN ou NATY", exemplo `INTEGRANTE`), mantendo o
  `toString` que mascara o token. Preencher o campo em todos os pontos de construção
  achados na pergunta 3 de 1.3 (emissão e renovação). Aceite: `./mvnw -B test-compile` passa.
- [x] 8.2 Em `usuario/SessaoApiTest.java` (ou `AutenticacaoApiTest.java`, onde o login é
  testado), cobrir o cenário "Papel na resposta" com as três contas. Em
  `usuario/RenovacaoDeSessaoApiTest.java`, provar que a renovação devolve o papel. Aceite:
  test-runner com `./mvnw -B test -Dtest='**/usuario/**'` verde.
- [x] 8.3 Registrar em `usuario/CLAUDE.md` que a resposta de sessão traz o papel para o
  cliente escolher a tela. Aceite: varredura de travessão vazia.

## 9. Integração e fechamento

- [x] 9.1 Atualizar `README.md` (empresa com CNPJ e razão social; criação com o primeiro
  `ADMIN` e telefone; variáveis `APP_BOOTSTRAP_NATY_CNPJ` e
  `APP_BOOTSTRAP_NATY_RAZAO_SOCIAL`; papel no login) e o `CLAUDE.md` raiz se a descrição de
  `painel` ou de `empresa` deixar de bater. Aceite: varredura de travessão vazia e texto sem
  verbo de mudança nem marcador de tempo.
- [x] 9.2 Rodar `npx --yes @fission-ai/openspec@latest validate dados-cadastrais-da-empresa
  --strict`. Aceite: sem erro.
- [x] 9.3 Antes do fechamento, `git fetch origin` e `git merge origin/main`; conflito que
  mude comportamento de outra pessoa é parada para o Breno. Test-runner com
  `./mvnw -B test-compile` e `./mvnw -B verify`; se o `spotless:check` falhar, rodar
  `./mvnw -B spotless:apply` e repetir. Aceite: build verde e resumo (total, falhas e motivo)
  mostrado ao Breno.
- [x] 9.4 O requisito "Empresa sempre com ADMIN ativo" e o requisito "Telefone do integrante"
  moram em `specs/cadastro-integrante/spec.md`, e a confirmação do CNPJ da tarefa 2.7 está
  registrada nela.

## Workflow follow-up

- Arquivar a change depois do merge, com `/opsx:archive`.
- Conferir que `openspec/specs/cadastro-empresa/spec.md`,
  `openspec/specs/cadastro-integrante/spec.md` e `openspec/specs/sessao-integrante/spec.md`
  receberam os requisitos novos.
