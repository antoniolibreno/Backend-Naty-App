# Proposal

## Why

Os testes de integração herdam o perfil de desenvolvimento, dependem de e-mails fixos num
banco compartilhado e não há verificação automática das regras de dependência entre
pacotes, de comentário e de escrita de documento. Nada roda em integração contínua. Sem
essa base, as próximas entregas (conteúdo, observabilidade, acompanhamento) editam código
sem uma rede que impeça regressão de arquitetura e de convenção.

## What Changes

- Perfil de teste próprio em `src/test/resources/application-test.yml`, com as migrations e
  o seed de desenvolvimento declarados explicitamente.
- E-mails de teste aleatórios por execução em todas as classes que hoje usam e-mail fixo.
- Teste de dependência entre pacotes: `trilha` não depende de `progresso`, só `painel`
  escreve em `usuario`, DTO de leitura sem campo de gabarito.
- Teste de build que reprova comentário explicativo, marcador temporal ou de autoria no
  código e narrativa de mudança nos documentos.
- Workflow de integração contínua que roda `./mvnw -B verify` com Docker disponível.
- O assert de `QuizApiTest` já verifica a estrutura da resposta. O backlog marca o item
  como concluído sem código novo.

## Capabilities

### New Capabilities

### Modified Capabilities

Nenhuma. A entrega é de ferramental e não altera comportamento observável da API.

## Impact

- `src/test/resources/application-test.yml` (novo), testes em `src/test/java`.
- `pom.xml`: dependência de teste para as regras de arquitetura, se aprovada no design.
- `.github/workflows/ci.yml` (novo).
- `docs/backlog.md`: itens 10.3, 10.5, 10.9, 10.10, 10.11 e 10.18 marcados.
