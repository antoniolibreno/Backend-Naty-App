# Design

## Context

Ver `proposal.md`, seção Why. O login por e-mail e senha, a sessão opaca, os papéis `NATY`,
`ADMIN` e `INTEGRANTE` e as rotas de `/api/v1/painel/**` estão construídos. Esta change não
mexe em `config/SecurityConfig` nem nas regras de rota.

Restrições que moldam a abordagem:

- `empresa` e `usuario` só são escritas pelo pacote `painel`. O controller, o serviço e os
  DTOs de empresa moram em `painel`; a entidade e o repositório moram em `empresa`.
- Coluna nova `not null` em `empresa` ou em `usuario` exige default, porque a limpeza dos
  testes e o seed repetível inserem por SQL. CNPJ único e telefone não têm default possível.
- `BootstrapNaty` cria a empresa interna e a conta `NATY` a partir de variáveis de ambiente.
- `ViolacaoDeConstraint` traduz violação de integridade pelo nome da constraint, e qualquer
  outra sobe como erro.
- Todo record que carrega senha sobrescreve `toString` mascarando a senha.
- `SenhaRequest.TAMANHO_MINIMO` e `SenhaRequest.cabeNoBcrypt` concentram a regra de senha.
- O seed `db/seed-dev/R__seed_empresa_exemplo.sql` faz upsert por id fixo.

## Goals / Non-Goals

**Goals:**

- Empresa com CNPJ e razão social, validados na borda e garantidos no banco quando
  presentes.
- Unicidade de CNPJ garantida pelo banco, inclusive sob concorrência.
- Busca na listagem de empresas sem quebrar a ordem estável por nome e identificador.
- Empresa criada já com o primeiro `ADMIN`, que é o responsável, numa única transação.
- Toda empresa com ao menos um `ADMIN` ativo.
- Empresa interna da Naty com CNPJ e razão social vindos do bootstrap.
- Papel na resposta de sessão, para o cliente rotear sem chamada extra.

**Non-Goals:**

- Tela do painel. O repositório entrega o contrato REST.
- Consulta de CNPJ na Receita ou em qualquer serviço externo.
- Tornar CNPJ ou razão social `not null` no banco.
- Busca sem acento ou por similaridade.
- Endpoint de identidade (`/eu`) além do papel na sessão.
- Envio de e-mail: convite com e-mail e senha, ou link para o próprio usuário definir a
  senha. A senha inicial do administrador é informada por quem cadastra (`NATY`). Pendência
  vira item de `docs/backlog.md`.
- O `ADMIN` ver o desempenho da própria equipe. O pacote `acompanhamento` só aceita `NATY`.
  Pendência vira item de `docs/backlog.md`.

## Decisions

### D1. O responsável é a conta `ADMIN`

Contratante e responsável são a mesma pessoa, com login para acompanhar a própria equipe.
Não há colunas de responsável em `empresa`, nem DTO de responsável. O contato do responsável
é o nome, o e-mail e o telefone da conta `ADMIN`.

`usuario` ganha a coluna `telefone varchar(13)`, nula no banco, com
`check (telefone ~ '^[0-9]{10,13}$')`. O telefone é obrigatório na API só no administrador
inicial criado com a empresa. `IntegranteCriacaoRequest` e `IntegranteAlteracaoRequest`
ganham `telefone` opcional, e `IntegranteResponse` o devolve.

Alternativa descartada: guardar nome, e-mail e telefone do responsável em `empresa`. Duplica
dado que a conta `ADMIN` já tem e cria um contato que não autentica.

### D2. CNPJ e razão social nulos no banco, obrigatórios na API

`cnpj varchar(14)` e `razao_social varchar(255)`, ambas nulas no banco. A obrigatoriedade
mora no Bean Validation de `EmpresaRequest` e de `EmpresaCriacaoRequest`. Nulas no banco
porque os testes e o seed inserem empresa por SQL e a coluna única de CNPJ não tem default.
Não há empresa real cadastrada em nenhum ambiente, então nenhuma linha precisa de
saneamento.

Alternativa descartada: `not null` com default vazio. Quebra o índice único de CNPJ e grava
dado falso.

A empresa interna da Naty recebe CNPJ e razão social do bootstrap (D10).

### D3. CNPJ normalizado no construtor do record, validado por `@AssertTrue`

Classe `painel/Cnpj` com `static String normalizar(String)` (remove `.`, `/`, `-` e espaço,
converte para maiúsculas) e `static boolean valido(String)`:

- formato `^[0-9A-Z]{12}[0-9]{2}$`;
- os 14 caracteres não são todos iguais;
- valor de cada caractere é o código ASCII menos 48 (`0` a `9` valem 0 a 9, `A` vale 17,
  `Z` vale 42);
- primeiro dígito: pesos `5,4,3,2,9,8,7,6,5,4,3,2` sobre os 12 primeiros; resto da soma por
  11; resto menor que 2 dá 0, senão 11 menos o resto;
- segundo dígito: pesos `6,5,4,3,2,9,8,7,6,5,4,3,2` sobre os 12 primeiros mais o primeiro
  dígito, mesma regra.

O cálculo cobre o CNPJ numérico e o alfanumérico com o mesmo código. Normalizar no
construtor segue a armadilha já registrada: o Bean Validation roda depois do construtor.
O banco tem `check (cnpj ~ '^[0-9A-Z]{12}[0-9]{2}$')` como segunda barreira de formato.

Exemplos verificados: `11222333000181`, `12ABC34501DE35` e `48055059000103` válidos;
`11222333000182` inválido.

### D4. Unicidade por índice único com tradução nominal

`create unique index empresa_cnpj_idx on empresa (cnpj)`. Postgres aceita vários nulos.
`ViolacaoDeConstraint` reconhece `empresa_cnpj_idx` e `EmpresaService` traduz para 409
`CNPJ_JA_CADASTRADO`, constante nova em `CodigoDeConflito`. Sem checagem prévia por
consulta: o índice resolve a corrida, e a tradução cobre os dois caminhos. O `flush` dentro
do serviço garante que a violação aparece antes de sair do método transacional, como o
cadastro de integrante já faz para `usuario_email_idx`.

### D5. `EmpresaCriacaoRequest` separado de `EmpresaRequest`, com administrador obrigatório

`EmpresaCriacaoRequest` tem os campos de `EmpresaRequest` mais `AdministradorInicialRequest
administrador`, obrigatório (`@NotNull @Valid`). A empresa nasce sempre com o primeiro
`ADMIN`, sem opção de criar empresa vazia. `EmpresaRequest` continua sendo o corpo do `PUT`,
sem o bloco, e nunca cria conta. O Jackson do projeto ignora propriedade desconhecida no
corpo, então o `PUT` com o bloco altera a empresa e descarta o bloco, como diz o cenário
"Alteração com bloco administrador" do spec. Mesmo padrão de `IntegranteCriacaoRequest` e
`IntegranteAlteracaoRequest`.

`AdministradorInicialRequest(nome, email, senha, telefone)` usa `SenhaRequest.TAMANHO_MINIMO`
e `SenhaRequest.cabeNoBcrypt`, normaliza o e-mail no construtor, aplica a normalização de
telefone de D8 com presença obrigatória e mascara a senha no `toString`. O papel é sempre
`ADMIN`; o perfil é o padrão do cadastro de integrante.

`EmpresaService.criar` grava a empresa e chama `IntegranteService` para cadastrar a conta
`ADMIN` na mesma transação. Falha em qualquer parte, como `EMAIL_JA_CADASTRADO` ou
`CNPJ_JA_CADASTRADO`, desfaz tudo por rollback. A resposta é `EmpresaResponse`, sem dado da
conta. Se cadastrar o `ADMIN` exigir mudar a assinatura pública de `IntegranteService` além
de método novo, a tarefa 1.4 manda parar e registrar para revisão humana.

Alternativa descartada: rota de onboarding separada. Duplicaria validação de empresa e
deixaria a rota de criação sem o caso comum.

### D6. Busca por `EmpresaRepository` com consulta JPQL

`EmpresaRepository.buscar(String texto, String cnpjParcial, Pageable)` com
`lower(e.nome) like %texto%`, `lower(e.razaoSocial) like %texto%` ou `e.cnpj like
%cnpjParcial%`. `texto` é a busca em minúsculas; `cnpjParcial` é a busca passada por
`Cnpj.normalizar`, e só entra na consulta quando não fica vazia. Os curingas `%` e `_` da
entrada são escapados. A ordem continua a do serviço (nome e identificador), e o `sort` do
cliente continua ignorado. Sem índice novo: a listagem de empresas é pequena e só o `NATY`
a consulta.

### D7. Papel em `SessaoResponse`

Campo `Papel papel` em `SessaoResponse`, preenchido na emissão e na renovação a partir da
conta carregada. Campo novo em resposta não quebra o app Flutter.

### D8. Telefone só com dígitos

Normalizado no construtor removendo tudo que não é dígito; validado entre 10 e 13 dígitos
quando presente; vazio vira nulo. Cobre fixo e celular com DDD e com código do país. A
regra vale para o administrador inicial (obrigatório) e para o cadastro e a alteração de
integrante (opcional). Alterar integrante com telefone ausente no corpo segue a semântica de
campo opcional já usada pelo `IntegranteAlteracaoRequest`, confirmada no levantamento 1.3.

### D9. Empresa sempre com `ADMIN` ativo

Desativar ou rebaixar de papel o último `ADMIN` ativo da empresa responde 409, código novo
estável `ULTIMO_ADMINISTRADOR` em `CodigoDeConflito`. A regra vale para desativar (`PUT` com
`ativo` falso) e para alterar o papel pelo painel. Desativar a empresa inteira continua
permitido. A checagem mora em `IntegranteService`, no ponto onde ele desativa e altera papel,
e conta apenas contas `ADMIN` ativas da mesma empresa, com a conta alterada excluída da
contagem.

O requisito "Empresa sempre com ADMIN ativo" é escrito em `specs/cadastro-integrante/spec.md`,
porque descreve a desativação e a alteração de papel de integrante.

Se a implementação exigir mudar a interface pública de `IntegranteService` além de método
novo, a parada de revisão humana 1.5 registra a mudança antes do código.

Alternativa descartada: impedir o rebaixamento só pela interface do painel. O contrato REST
é a barreira, não a tela.

### D10. Bootstrap da Naty com CNPJ e razão social

`BootstrapNaty` lê `app.bootstrap.naty.cnpj` e `app.bootstrap.naty.razao-social` (variáveis
`APP_BOOTSTRAP_NATY_CNPJ` e `APP_BOOTSTRAP_NATY_RAZAO_SOCIAL`), obrigatórias onde o bootstrap
é obrigatório (produção), com validação do CNPJ por `Cnpj.valido` depois de `Cnpj.normalizar`.
Valor ausente ou CNPJ inválido recusa a subida, como as demais variáveis do bootstrap. O
seed de desenvolvimento da empresa Naty usa a razão social `Naty App Oficial LTDA` e o CNPJ
`48055059000103`. A conta `NATY` do bootstrap não leva telefone, que é opcional no banco.

Impacto em testes: todo teste que sobe o contexto com bootstrap obrigatório ou que testa
`BootstrapNaty` informa as duas propriedades. O levantamento da tarefa 1.3 lista
esses pontos.

## Risks / Trade-offs

- [Cliente do painel quebra ao criar e alterar empresa sem os campos novos] → Contrato
  documentado no Swagger e na proposta como BREAKING. O app Flutter não usa essas rotas.
- [Testes existentes montam corpo de empresa sem os campos novos e quebram em massa] →
  Levantamento na tarefa 1.3 conta os pontos e a tarefa 1.4 decide se um construtor de teste
  centraliza o corpo.
- [Produção recusa subir sem `APP_BOOTSTRAP_NATY_CNPJ` e `APP_BOOTSTRAP_NATY_RAZAO_SOCIAL`]
  → `.env.example`, README e docs listam as variáveis; o Breno confirma o CNPJ oficial na
  Receita Federal antes do uso em produção.
- [Conta `ADMIN` única desativada por engano deixa a empresa sem responsável] → D9 recusa
  com 409 `ULTIMO_ADMINISTRADOR`.
- [CNPJ revela cliente na busca] → A listagem já é exclusiva de `NATY`.
- [Bloco `administrador` amplia a superfície de senha] → Mesmas regras e mesma máscara do
  cadastro de integrante, com teste de log e resposta.
- [Telefone novo em `usuario` altera cadastro, seed e testes de integrante] → Coluna nula no
  banco e opcional fora do administrador inicial; tarefas 2.x e 4.x cobrem seed, DTOs e
  testes.
- [Conflito com a branch `feat/swagger-descritivo`, que altera `EmpresaRequest`,
  `EmpresaResponse` e `SessaoResponse`] → A tarefa 1.1 exige essa branch integrada em `main`
  antes de começar.

## Migration Plan

1. Migration `V15__dados_cadastrais_da_empresa.sql` aditiva: colunas nulas em `empresa` e em
   `usuario`, `check` e índice. Não toca linha existente.
2. Seed `R__seed_empresa_exemplo.sql` reescrito com dados válidos: CNPJ e razão social nas
   duas empresas e telefone na conta `ADMIN`.
3. Rollback: a versão anterior da aplicação ignora as colunas novas, porque `ddl-auto`
   `validate` não reclama de coluna sobrando. Não há migration de reversão.

## Open Questions

Nenhuma que mude specs ou tarefas. As decisões D1, D2, D5 e D9 passam pela parada de revisão
da tarefa 1.5.
