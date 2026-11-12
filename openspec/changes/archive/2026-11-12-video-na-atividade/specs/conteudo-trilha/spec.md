## ADDED Requirements

### Requirement: URL de vídeo do YouTube
A atividade SHALL aceitar vídeo HTTPS em domínios exatos do YouTube, com identificador de vídeo válido. URLs externas, credenciais embutidas, portas próprias e URLs malformadas SHALL ser recusadas. Vídeo configurado SHALL ter duração positiva.

#### Scenario: Domínio semelhante
- **WHEN** uma URL aponta para `youtube.com.exemplo.org`
- **THEN** o conteúdo é recusado

#### Scenario: Atividade sem vídeo
- **WHEN** uma atividade não possui vídeo
- **THEN** a leitura responde 200 com `videoUrl` nulo
