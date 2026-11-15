# Tarefas: identidade e sessão autenticada

## 1. Dependência

- [x] 1.1 Adicionar `spring-boot-starter-security` ao `pom.xml`. Nenhuma biblioteca de
  token entra: o token é opaco e mora em tabela.

## 2. Migration

A migration vem antes da entidade. `ddl-auto` está em `validate`, e campo em entidade sem
coluna no banco derruba a subida.

- [x] 2.1 Escrever `src/main/resources/db/migration/V6__credencial_papel_e_sessao.sql`.
  Não editar `V1` a `V5`: o Flyway valida o checksum do que já rodou.
- [x] 2.2 Adicionar a `usuario` as colunas `senha_hash` text, `papel` varchar(20) not
  null com padrão `INTEGRANTE` e `ativo` boolean not null com padrão `true`. As colunas
  nascem com padrão porque o seed repetível roda depois das migrations versionadas.
- [x] 2.3 Criar a tabela `sessao` com `id` uuid padrão `gen_random_uuid()`, `usuario_id`
  not null referenciando `usuario(id)` com `on delete cascade`, `token_hash` text not
  null, `criado_em`, `expira_em`, `ultimo_acesso_em` e `revogado_em` timestamptz.
- [x] 2.4 Criar o índice único `sessao_token_hash_idx` sobre `token_hash` e o índice
  `sessao_usuario_idx` sobre `usuario_id`.
- [x] 2.5 Dropar `usuario_empresa_email_idx` e criar o índice único global
  `usuario_email_idx` sobre `lower(email)`. A migration falha alto se já existir e-mail
  repetido entre empresas, e não deduplica em silêncio.
- [x] 2.6 Subir com `docker compose down -v` antes, porque `baseline-on-migrate` mascara
  divergência de histórico em banco já migrado. Conferir com `\d usuario` e `\d sessao`.

## 3. Seed de desenvolvimento

- [x] 3.1 Dar `senha_hash` e `papel` às duas contas de
  `db/seed-dev/R__seed_empresa_exemplo.sql`, uma `ADMIN` e uma `INTEGRANTE`. O arquivo é
  repetível e recalcula o próprio checksum.
- [x] 3.2 Registrar a senha de desenvolvimento no `.env.example` e no
  `usuario/CLAUDE.md`, para o time do app conseguir entrar.

## 4. Credencial e papel no domínio

- [x] 4.1 Adicionar a `usuario/Usuario.java` os campos de hash de senha, papel e ativo,
  espelhando as colunas de `V6`.
- [x] 4.2 Criar o enum `usuario/Papel.java` com `INTEGRANTE` e `ADMIN`. `usuario.perfil`
  continua descrevendo o papel no WhatsApp e não vale como permissão.
- [x] 4.3 Trocar `UsuarioRepository.buscarPorEmailNormalizado` para buscar sem filtro de
  empresa, porque o e-mail passa a ser único no sistema inteiro.

## 5. Sessão

- [x] 5.1 Criar `usuario/Sessao.java` e `usuario/SessaoRepository.java`, com busca pelo
  hash do token.
- [x] 5.2 Criar o serviço que emite sessão: gera token opaco com gerador criptográfico,
  guarda o hash dele, calcula a expiração e devolve o token em claro uma única vez.
- [x] 5.3 Implementar a revogação, marcando o momento em `revogado_em`.
- [x] 5.4 Registrar o último acesso na própria sessão. Só o painel escreve em `usuario`,
  então o carimbo não mora lá.
- [x] 5.5 Ler tempo de expiração e parâmetros de hash de senha de configuração de
  ambiente. A aplicação recusa subir em produção sem eles.

## 6. Autenticação

- [x] 6.1 Trocar `usuario/UsuarioService.resolverPorEmail` por verificação de senha
  contra o hash, devolvendo a mesma recusa para e-mail inexistente e para senha
  incorreta.
- [x] 6.2 Recusar sessão para integrante desativado.
- [x] 6.3 Acrescentar `token` e `expiraEm` a `SessaoResponse`, preservando `usuarioId`,
  `empresaId`, `nome` e `email`. A adição é aditiva e a tela de Login não muda de
  formato.
- [x] 6.4 Expor `DELETE /api/v1/sessoes/atual` em `usuario/SessaoController.java`.
- [x] 6.5 Criar o tratamento de erro de credencial inválida em
  `shared/exception/ApiExceptionHandler.java`, com código estável. Não existe tratamento
  genérico de propósito.
- [x] 6.6 Conferir que senha, hash e token não aparecem em log nem em mensagem de erro.

## 7. Cadeia de segurança

- [x] 7.1 Criar `config/SecurityConfig.java`. Público: `POST /api/v1/sessoes`,
  `/actuator/health` e a documentação em desenvolvimento. Todo o resto exige token.
- [x] 7.2 Criar o filtro que resolve a sessão a partir de `Authorization: Bearer` e
  popula o contexto de segurança, recusando token desconhecido, revogado ou expirado.
- [x] 7.3 Trocar `usuario/IntegranteArgumentResolver` para ler do contexto de segurança.
  O cabeçalho `X-Integrante-Id` deixa de ser lido, sem janela de convivência. Nenhum
  controller e nenhuma rota são reescritos.
- [x] 7.4 Remover `IntegranteNaoInformadoException` e o tratamento dele, porque a recusa
  passa a vir da cadeia de segurança.

## 8. OpenAPI

- [x] 8.1 Preencher `config/OpenApiConfig.java` com título, versão, servidores e
  agrupamento de tags.
- [x] 8.2 Declarar o esquema de segurança bearer e aplicá-lo às operações protegidas. Sem
  isso o Swagger não testa endpoint protegido.
- [x] 8.3 Tirar da descrição das operações o texto que anuncia cabeçalho forjável e senha
  aceita sem verificação.

## 9. Testes

- [x] 9.1 Criar um apoio de teste que autentica uma vez e devolve o cabeçalho pronto,
  para os testes existentes não repetirem o login.
- [x] 9.2 Ajustar `TrilhaApiTest`, `QuizApiTest`, `SeedConteudoTest`, `SessaoApiTest` e
  `ProgressoApiTest` para autenticar. Eles quebram juntos, e é consequência do corte seco.
- [x] 9.3 Remover de `SessaoApiTest` os cenários que fixam credencial não emitida e
  qualquer senha aceita.
- [x] 9.4 Criar `usuario/AutenticacaoApiTest` cobrindo senha correta, senha incorreta,
  e-mail inexistente com a mesma resposta, integrante desativado, token ausente, token
  desconhecido, token expirado e token revogado.
- [x] 9.5 Criar o teste de isolamento por empresa: token da empresa A não alcança nada da
  empresa B, em todos os endpoints por empresa.
- [x] 9.6 Criar o teste que prova que identificador de integrante informado pelo cliente
  é ignorado em favor do dono do token.

## 10. Documentação

- [x] 10.1 Tirar dos limites do sistema em `CLAUDE.md` a ausência de autenticação, o
  cabeçalho forjável e a senha aceita sem verificação.
- [x] 10.2 Atualizar `usuario/CLAUDE.md`, `progresso/CLAUDE.md`, `trilha/CLAUDE.md` e
  `config/CLAUDE.md` com o mecanismo construído e as armadilhas dele.
- [x] 10.3 Riscar em `docs/backlog.md` os itens entregues da fundação de identidade e da
  autenticação, mais o preenchimento do OpenAPI.
- [ ] 10.4 Avisar o time do app a data do corte, a senha das contas de desenvolvimento e
  a troca de cabeçalho.

## 11. Fechamento

- [x] 11.1 Rodar `./mvnw -B verify` com Docker ativo e conferir que todos os testes
  passam.
- [x] 11.2 Rodar `npx --yes @fission-ai/openspec@latest validate identidade-e-sessao-autenticada`.
- [x] 11.3 Sincronizar as specs.
- [ ] 11.4 Arquivar a proposta, depois do aviso ao time do app.
