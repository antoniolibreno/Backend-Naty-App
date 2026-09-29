# Tarefas: painel administrativo

## 1. Pré-requisitos

- [ ] 1.1 Mergear `chore/postgres-15` na `main` e rebasear esta branch sobre ela.
- [ ] 1.2 Combinar com a equipe do MVP funcional que `V7` e `/api/v1/painel/**` pertencem
  a esta proposta e que as migrations deles começam em `V8`.
- [ ] 1.3 Arquivar `identidade-e-sessao-autenticada` com `--skip-specs`, depois do aviso
  ao time do app.

## 2. Contratos compartilhados (pull request próprio, antes do pacote `painel`)

- [x] 2.1 Escrever `db/migration/V7__fuso_papel_e_indice_de_empresa.sql` com
  `empresa.fuso_horario`, a restrição de valores de `usuario.papel` e o índice
  `usuario_empresa_idx`.
- [x] 2.2 Gerar identificador e carimbos em `empresa/Empresa.java` e mapear `fusoHorario`
  como `ZoneId` por `empresa/ZoneIdConverter.java`.
- [x] 2.3 Ajustar `IntegracaoTest.criarEmpresa` no mesmo commit da entidade e acrescentar
  os apoios de integrante por papel.
- [x] 2.4 Acrescentar `NATY` a `usuario/Papel.java` e `papel` a
  `usuario/IntegranteDaRequisicao.java`.
- [x] 2.5 Pôr a autoridade `ROLE_<papel>` no contexto em `usuario/TokenSessaoFiltro.java`.
- [x] 2.6 Criar `config/NegacaoDeAcesso.java` com 403 em `ErroResposta` e registrá-lo em
  `config/SecurityConfig.java`.
- [x] 2.7 Criar `shared/exception/ConflitoException.java` e o tratamento 409 em
  `shared/exception/ApiExceptionHandler.java`.
- [x] 2.8 Criar `shared/pagina/PaginaResponse.java` e configurar
  `spring.data.web.pageable` em `application.yml`.
- [x] 2.9 Rodar `./mvnw -B verify` com os testes existentes sem mudança de assert.

## 3. Sessão

- [x] 3.1 Recusar integrante de empresa inativa em `UsuarioService.verificarCredencial`.
- [x] 3.2 Recusar integrante inativo e empresa inativa em `SessaoService.resolverPorToken`.
- [x] 3.3 Criar a revogação em lote por integrante e por empresa em `SessaoService`.

## 4. Pacote `painel`: empresa

- [x] 4.1 Mover controller, serviço, mapper e DTOs de empresa para `painel`, sob
  `/api/v1/painel/empresas`.
- [x] 4.2 Acrescentar `fusoHorario` validado ao `EmpresaRequest` e ao `EmpresaResponse`.
- [x] 4.3 Paginar a listagem de empresa.
- [x] 4.4 Trocar o `ResponseStatusException` de exclusão por `ConflitoException`.
- [x] 4.5 Revogar as sessões da empresa na desativação e recusar operação sobre a empresa
  do próprio `NATY`.

## 5. Pacote `painel`: integrante

- [x] 5.1 Criar a busca paginada por empresa em `UsuarioRepository`.
- [x] 5.2 Criar `IntegranteService` com cadastro, alteração, senha, ativo, detalhe e
  listagem, sempre sobre empresa já resolvida.
- [x] 5.3 Criar `IntegranteController` para `ADMIN` e `EmpresaIntegranteController` para
  `NATY`.
- [x] 5.4 Criar os DTOs com e-mail normalizado no construtor, senha mascarada no
  `toString` e limite de 72 bytes.
- [x] 5.5 Recusar operação de `ADMIN` sobre conta `ADMIN` e escrita em conta `NATY`.

## 6. Autorização

- [x] 6.1 Acrescentar os matchers de `/api/v1/painel/**` em `SecurityConfig`.

## 7. Bootstrap

- [x] 7.1 Criar `painel/BootstrapNaty.java` e as propriedades `app.bootstrap`.
- [x] 7.2 Ligar `app.bootstrap.obrigatorio` em `application-prod.yml` e documentar as
  variáveis em `.env.example`.
- [x] 7.3 Acrescentar a empresa interna e a conta `NATY` ao seed de desenvolvimento.

## 8. Testes

- [x] 8.1 Mover e ajustar `EmpresaServiceTest`.
- [x] 8.2 Criar `painel/EmpresaPainelApiTest`.
- [x] 8.3 Criar `painel/IntegrantePainelApiTest`.
- [x] 8.4 Criar `painel/AutorizacaoPainelTest`.
- [x] 8.5 Criar `painel/IsolamentoDoPainelTest`.
- [x] 8.6 Criar `painel/BootstrapNatyTest`.
- [x] 8.7 Cobrir empresa inativa em `usuario/AutenticacaoApiTest`.

## 9. Documentação

- [x] 9.1 Escrever `painel/CLAUDE.md`.
- [x] 9.2 Atualizar `empresa/CLAUDE.md`, `usuario/CLAUDE.md`, `config/CLAUDE.md`,
  `shared/CLAUDE.md` e o `CLAUDE.md` do pacote raiz.
- [x] 9.3 Atualizar pacotes e limites no `CLAUDE.md` raiz.
- [x] 9.4 Atualizar papéis, empresa inativa e alcance entre empresas em `docs/regras.md`.
- [x] 9.5 Marcar os itens entregues em `docs/backlog.md`.
- [x] 9.6 Esconder `IntegranteDaRequisicao` dos parâmetros do Swagger em `config/OpenApiConfig`.

## 10. Endurecimento

- [x] 10.a Gravar último acesso e revogação por update em lote, sem regravar a sessão.
- [x] 10.b Travar a linha do usuário no login, na troca de senha e na desativação, e as
  linhas da empresa na desativação dela.
- [x] 10.c Comparar a senha com hash fictício em toda recusa de login.
- [x] 10.d Mascarar senha e token no `toString` de `SessaoRequest` e `SessaoResponse`.
- [x] 10.e Recusar desativar ou excluir empresa com conta `NATY`.
- [x] 10.f Traduzir violação de integridade pelo nome da constraint e a corrida da exclusão
  em `EMPRESA_COM_VINCULOS`.
- [x] 10.g Manter o perfil atual quando a alteração não traz perfil.
- [x] 10.h Serializar o bootstrap com `pg_advisory_xact_lock`.
- [x] 10.i Criar `TravaDaContaDeSeed`, fazer o seed por upsert, repassar `.env` no compose
  e desligar `baseline-on-migrate` em produção.
- [x] 10.j Registrar no `CLAUDE.md` e em `docs/regras.md` que conteúdo exige `NATY`, a ordem
  dos matchers e a regra de FK e default em `usuario` e `empresa`.

## 11. Fechamento

- [x] 11.1 Rodar `./mvnw -B verify` com Docker ativo.
- [x] 11.2 Subir com `docker compose up --build` sobre banco já migrado e percorrer o fluxo do
  painel pela API.
- [x] 11.3 Conferir que `trilha` e `progresso` não aparecem no diff.
- [x] 11.4 Rodar `npx --yes @fission-ai/openspec@latest validate painel-administrativo`.
- [x] 11.5 Sincronizar as specs.
- [ ] 11.6 Arquivar a proposta com `--skip-specs` depois do merge.
