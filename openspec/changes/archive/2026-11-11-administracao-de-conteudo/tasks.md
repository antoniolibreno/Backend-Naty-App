# Tarefas

## 1. Base do pacote

- [x] 1.1 Ler `trilha/CLAUDE.md` e `config/CLAUDE.md`.
- [x] 1.2 Escrever a migration de `alteracao_conteudo`.
- [x] 1.3 Criar o pacote `conteudo` com `AlteracaoDeConteudo`, o repositório e `AuditoriaDeConteudo`.
- [x] 1.4 Criar a consulta de histórico em `AtividadeRepository` e a de maior ordem por pai.
- [x] 1.5 Adicionar os matchers `NATY` em `SecurityConfig`.

## 2. Trilha

- [x] 2.1 Criar `TrilhaRequest`, `AtivacaoRequest` e `TrilhaAdminResponse`.
- [x] 2.2 Criar `POST`, `PUT`, `DELETE` e `PUT /ativa` de trilha com auditoria.

## 3. Módulo e atividade

- [x] 3.1 Criar `ModuloRequest`, `AtividadeRequest` com validação de vídeo, duração e XP.
- [x] 3.2 Criar `POST`, `PUT` e `DELETE` de módulo e de atividade com auditoria.
- [x] 3.3 Criar a proteção de remoção por histórico em trilha, módulo e atividade.

## 4. Ordem

- [x] 4.1 Criar `OrdemRequest` e `ReordenacaoService` com a renumeração em duas passagens.
- [x] 4.2 Mapear `PUT /api/v1/trilhas/{id}/modulos/ordem` e `PUT /api/v1/modulos/{id}/atividades/ordem`.

## 5. Leitura das alterações

- [x] 5.1 Criar `GET /api/v1/alteracoes-de-conteudo` paginado, com filtro por `entidadeId`.

## 6. Testes

- [x] 6.1 Testar que `ADMIN`, `INTEGRANTE` e sem token são recusados em toda rota de escrita e na leitura das alterações.
- [x] 6.2 Testar cadastro, alteração, remoção e validação de trilha, módulo e atividade.
- [x] 6.3 Testar reordenação, lista inválida e criação no fim.
- [x] 6.4 Testar ativação, listagem e preservação do progresso.
- [x] 6.5 Testar a recusa de remoção com progresso, tentativa e ponto, e a remoção sem histórico.
- [x] 6.6 Testar a auditoria, o filtro e que alteração recusada não gera registro.
- [x] 6.7 Atualizar `RegrasDeDependenciaTest` com as regras de escrita em `trilha` e de `correta`.

## 7. Documentação e fechamento

- [x] 7.1 Escrever `conteudo/CLAUDE.md` e atualizar `trilha/CLAUDE.md`, `CLAUDE.md` raiz e `docs/regras.md`.
- [x] 7.2 Marcar 9.2, 9.3, 9.7, 9.8 e 9.10 em `docs/backlog.md`.
- [x] 7.3 Rodar `./mvnw -B test-compile` e `./mvnw -B verify`.
