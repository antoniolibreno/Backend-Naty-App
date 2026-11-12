## ADDED Requirements

### Requirement: Sessão antiga é removida

O sistema SHALL remover a sessão cuja expiração ou revogação ocorreu há mais que o prazo
de retenção, e NÃO SHALL remover sessão válida nem sessão dentro do prazo. A limpeza SHALL
rodar de forma periódica. O prazo de retenção e a periodicidade SHALL ser lidos de
configuração de ambiente, nunca de constante em código. A produção SHALL recusar subir sem
o prazo de retenção.

A remoção de sessão NÃO SHALL apagar progresso, tentativa de quiz nem ponto do integrante.

#### Scenario: Sessão expirada além do prazo

- **WHEN** a limpeza roda e existe sessão expirada há mais que o prazo de retenção
- **THEN** o sistema remove essa sessão

#### Scenario: Sessão revogada além do prazo

- **WHEN** a limpeza roda e existe sessão revogada há mais que o prazo de retenção
- **THEN** o sistema remove essa sessão

#### Scenario: Sessão dentro do prazo

- **WHEN** a limpeza roda e a sessão expirou há menos que o prazo de retenção
- **THEN** o sistema mantém a sessão

#### Scenario: Sessão válida

- **WHEN** a limpeza roda e a sessão não expirou nem foi revogada
- **THEN** o sistema mantém a sessão

#### Scenario: Produção sem prazo

- **WHEN** a aplicação sobe em produção sem o prazo de retenção configurado
- **THEN** a aplicação recusa subir
