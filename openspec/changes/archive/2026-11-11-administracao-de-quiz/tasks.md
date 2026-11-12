# Tarefas

## 1. Base

- [x] 1.1 Ler `conteudo/CLAUDE.md` e `trilha/CLAUDE.md`.
- [x] 1.2 Adicionar os matchers `NATY` de `/api/v1/quizzes/**`, `/api/v1/perguntas/**` e `/api/v1/alternativas/**` em `SecurityConfig`.
- [x] 1.3 Criar as consultas de resposta por pergunta e por alternativa e a trava de pergunta.

## 2. Contratos

- [x] 2.1 Criar `AlternativaRequest`, `PerguntaRequest` e `QuizRequest` com as validações de gabarito.
- [x] 2.2 Criar `AlternativaAdminResponse`, `PerguntaAdminResponse` e `QuizAdminResponse`.

## 3. Quiz

- [x] 3.1 Criar `POST`, `GET`, `PUT` e `DELETE` de quiz com `QUIZ_JA_EXISTE` e proteção de histórico.
- [x] 3.2 Criar `POST`, `PUT` e `DELETE` de pergunta com `QUIZ_SEM_PERGUNTA`.
- [x] 3.3 Criar `POST`, `PUT` e `DELETE` de alternativa com a movimentação da marca e `GABARITO_INVALIDO`.
- [x] 3.4 Gravar a auditoria de quiz, pergunta e alternativa, com dois registros na troca de gabarito.

## 4. Testes

- [x] 4.1 Testar que `ADMIN`, `INTEGRANTE` e sem token são recusados em toda rota de quiz.
- [x] 4.2 Testar cadastro, validação e remoção de quiz, pergunta e alternativa.
- [x] 4.3 Testar a integridade do gabarito em cada transição, inclusive escrita concorrente.
- [x] 4.4 Testar que só a leitura administrativa devolve `correta` e que a do integrante não.
- [x] 4.5 Testar a recusa de remoção com tentativa e resposta registradas.
- [x] 4.6 Testar a auditoria da troca de gabarito.
- [x] 4.7 Atualizar `RegrasDeDependenciaTest` com a regra de que nada fora de `conteudo` depende de `conteudo.dto` e de que `AlternativaResponse` não tem `correta`.

## 5. Documentação e fechamento

- [x] 5.1 Atualizar `conteudo/CLAUDE.md`, `trilha/CLAUDE.md` e `docs/regras.md`.
- [x] 5.2 Marcar 9.4, 9.5, 9.6, 9.9 e 9.11 em `docs/backlog.md`.
- [x] 5.3 Rodar `./mvnw -B test-compile` e `./mvnw -B verify`.
