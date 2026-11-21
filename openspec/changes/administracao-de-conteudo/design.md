# Desenho

O pacote trilha permanece responsável pelo conteúdo e passa a oferecer operações administrativas. A segurança exige papel NATY nas rotas de escrita.

Cada gravação administrativa registra recurso, identificador, operação, autor e instante em tabela de auditoria. A exclusão física de atividade com progresso ou tentativa é recusada. Conteúdo elegível para preservação é desativado.

Alterar ordem ocorre em transação e desloca temporariamente os valores para evitar colisões nos índices únicos existentes. A escrita de alternativa usa DTO próprio com o gabarito; a resposta administrativa e a resposta do app não o incluem.
