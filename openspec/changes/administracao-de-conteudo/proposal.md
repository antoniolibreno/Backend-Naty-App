# Administração de conteúdo

## Motivo

O conteúdo global do treinamento precisa ser mantido pelo papel NATY sem alterar migrations já aplicadas e sem permitir que o gabarito chegue ao app de integrante.

## Escopo

CRUD autenticado de trilha, módulo, atividade, quiz, pergunta e alternativa; DTOs separados para escrita e leitura; validação do gabarito; reordenação; desativação; proteção do histórico de progresso; trilha de auditoria de alterações; testes de autorização.

## Dependências

A substituição do seed de exemplo exige o conteúdo real aprovado, incluindo textos, ordem, imagens, links e gabaritos. O material não está no repositório.

## Impacto

Somente NATY pode escrever conteúdo. Leituras existentes mantêm seus contratos, em especial a ausência do campo de gabarito.
