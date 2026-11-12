## 1. Base

- [x] 1.1 Ler o `CLAUDE.md` de cada pacote antes de editar o pacote
- [x] 1.2 Documentar `shared/exception/ErroResposta` com `@Schema` e exemplo
- [x] 1.3 `OpenApiConfig`: `OpenApiCustomizer` com 401 em toda operação protegida

## 2. DTOs e operações por pacote

- [x] 2.1 `usuario`: DTOs de sessão e usuário, `SessaoController`, `UsuarioController`
- [x] 2.2 `painel`: DTOs de empresa e integrante, três controllers, códigos de `CodigoDeConflito`
- [x] 2.3 `trilha`: DTOs de leitura, `TrilhaController`, `AtividadeController`
- [x] 2.4 `progresso`: DTOs de progresso, vídeo e tentativa, `ProgressoController`
- [x] 2.5 `gamificacao`: DTOs de perfil, conquista e ranking, dois controllers
- [x] 2.6 `conteudo`: DTOs de escrita de trilha, módulo, atividade, quiz, pergunta e alternativa
- [x] 2.7 `acompanhamento`: DTOs de situação, métricas e funil, `AcompanhamentoController`
- [x] 2.8 `shared/pagina`: `PaginaResponse` com descrição dos campos de página

## 3. Verificação

- [x] 3.1 Teste de integração em `/v3/api-docs`: todo schema de Request e Response tem
      `description` e `example`, toda operação tem `summary`, e toda operação protegida declara 401
- [x] 3.2 Teste de integração: campos `required` do schema batem com `@NotNull` e `@NotBlank`
- [x] 3.3 `./mvnw -B test-compile` e `./mvnw -B verify`
- [ ] 3.4 Abrir o Swagger UI e conferir o "Try it out" de uma rota por pacote
- [ ] 3.5 Varredura de U+2014 e de verbo de mudança no texto novo
