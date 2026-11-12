# Dados cadastrais da empresa

## Why

O login por e-mail e senha, os três papéis (`NATY` como dono da Naty, `ADMIN` como
contratante e responsável pela empresa e `INTEGRANTE` como funcionário do contratante) e o
cadastro de empresa e de integrante pelo painel existem e estão especificados em
`sessao-integrante`, `cadastro-empresa` e `cadastro-integrante`. O que falta para o time da
Naty operar o painel de clientes é a empresa como cliente de verdade: ela só tem nome, ativa
e fuso horário. Não há CNPJ nem razão social, não há como achar uma empresa na listagem, e
uma empresa recém-criada nasce sem ninguém capaz de entrar nela. O contratante e o
responsável são a mesma pessoa, a conta `ADMIN`, que entra no app para acompanhar a própria
equipe.

O cliente do painel também não sabe para qual tela levar quem acabou de entrar: a resposta do
login não traz o papel da conta.

## What Changes

- Empresa com CNPJ e razão social, obrigatórios na criação e na alteração pelo painel. O
  responsável pela empresa é a conta `ADMIN`, sem cópia de nome, e-mail ou telefone na
  empresa.
- CNPJ aceito com ou sem máscara, guardado só com os 14 caracteres, validado pelos dígitos
  verificadores e já no formato alfanumérico da Receita Federal. CNPJ é único entre as
  empresas, e repetição responde 409 `CNPJ_JA_CADASTRADO`.
- Telefone na conta de integrante: obrigatório no administrador inicial criado com a
  empresa, opcional nos demais cadastros e devolvido na resposta de integrante.
- Listagem de empresas do painel com o parâmetro opcional `busca`, que procura por nome,
  razão social ou CNPJ, com ou sem máscara.
- Criação de empresa com o bloco obrigatório `administrador` (nome, e-mail, senha e
  telefone), que cria a primeira conta `ADMIN` da empresa na mesma transação. Falha em
  qualquer parte não cria nada. Toda empresa tem ao menos um `ADMIN` ativo, e desativar ou
  rebaixar o último responde 409 `ULTIMO_ADMINISTRADOR`.
- A empresa interna da Naty tem CNPJ e razão social: o bootstrap lê
  `app.bootstrap.naty.cnpj` e `app.bootstrap.naty.razao-social`, obrigatórios onde o
  bootstrap é obrigatório. O seed de desenvolvimento usa dados válidos.
- Resposta de `POST /api/v1/sessoes` e de `POST /api/v1/sessoes/atual/renovacao` com o
  campo `papel`, para o cliente decidir entre painel da Naty, painel do contratante e app.
- **BREAKING** para o cliente do painel: `POST` e `PUT` de `/api/v1/painel/empresas` exigem
  `cnpj` e `razaoSocial`, e `POST` exige `administrador`. O app Flutter não chama essas
  rotas.

Fora do escopo: a tela do painel, que é cliente desta API e não mora neste repositório;
consulta de CNPJ em serviço externo, porque o sistema não depende de serviço externo;
endereço, contrato, plano e cobrança da empresa; envio de e-mail, ou seja convite com
e-mail e senha ou link para a própria pessoa definir a senha, porque a senha inicial do
administrador é informada por quem cadastra (`NATY`), e a pendência vira item de
`docs/backlog.md`; o `ADMIN` ver o desempenho da própria equipe, porque o pacote
`acompanhamento` só aceita `NATY`, e a pendência vira item de `docs/backlog.md`.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `cadastro-empresa`: empresa com CNPJ e razão social, unicidade de CNPJ, busca na
  listagem, criação com o primeiro `ADMIN` e empresa sempre com `ADMIN` ativo.
- `sessao-integrante`: a resposta da emissão e da renovação de sessão traz o papel da
  conta.

## Impact

- Migration nova em `src/main/resources/db/migration` com as colunas de `empresa`, a
  coluna `telefone` de `usuario`, as restrições de formato e o índice único de CNPJ.
- Pacote `painel`: `EmpresaRequest`, `EmpresaResponse`, `EmpresaMapper`, `EmpresaService`,
  `EmpresaController`, `CodigoDeConflito`, `ViolacaoDeConstraint`, DTO novo de criação,
  `IntegranteCriacaoRequest`, `IntegranteAlteracaoRequest`, `IntegranteResponse`,
  `IntegranteService`, `BootstrapNaty` e validação de CNPJ.
- Pacote `empresa`: entidade `Empresa` e `EmpresaRepository` (busca).
- Pacote `usuario`: entidade `Usuario`, `SessaoResponse` e o serviço que a monta.
- Configuração: `application.yml`, `.env.example` e README com as variáveis de CNPJ e razão
  social da Naty.
- Seed de desenvolvimento em `src/main/resources/db/seed-dev`, reescrito com dados válidos.
- Testes de `painel`, de `usuario` e do bootstrap que montam corpo de empresa, de
  integrante ou leem a resposta de sessão.
- `painel/CLAUDE.md`, `empresa/CLAUDE.md`, `usuario/CLAUDE.md`, `docs/regras.md`,
  `docs/backlog.md`, `README.md` e as descrições do Swagger dessas rotas.
