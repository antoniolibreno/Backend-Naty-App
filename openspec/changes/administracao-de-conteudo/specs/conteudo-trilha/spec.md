# conteudo-trilha Specification Delta

## ADDED Requirements

### Requirement: Escrita administrativa de conteúdo

O sistema SHALL permitir que somente o papel NATY crie, edite e remova trilhas, módulos, atividades, quizzes, perguntas e alternativas. A escrita de atividade SHALL validar a URL e a duração do vídeo. As alternativas SHALL usar DTO de escrita separado do DTO de leitura do app.

#### Scenario: Papel sem permissão tenta escrever conteúdo

- **WHEN** integrante ou ADMIN chama uma rota de escrita de conteúdo
- **THEN** o sistema responde com acesso negado e não altera o conteúdo

#### Scenario: Escrita de alternativa

- **WHEN** NATY cria ou altera uma alternativa
- **THEN** o campo de gabarito é aceito pela escrita e não aparece nas respostas de leitura

### Requirement: Gabarito único

Cada pergunta SHALL ter exatamente uma alternativa correta. Uma escrita que produza zero ou mais de uma alternativa correta SHALL ser recusada.

### Requirement: Reordenação de módulos e atividades

O sistema SHALL oferecer operação própria para reordenar todos os módulos de uma trilha e todas as atividades de um módulo, sem violar a unicidade de ordem por pai.

### Requirement: Preservação do histórico de aprendizagem

Trilha com progresso registrado SHALL ser desativada em vez de removida. Atividade, quiz, pergunta ou alternativa referenciada por progresso ou tentativa SHALL ter remoção física recusada. Trilhas inativas SHALL ficar ausentes da listagem pública.

### Requirement: Auditoria de conteúdo

Cada criação, alteração, reordenação, desativação e remoção administrativa SHALL registrar o recurso, a operação, o autor autenticado e o instante.
