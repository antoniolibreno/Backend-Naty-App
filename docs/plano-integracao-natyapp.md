# Plano de integração do backend com o NatyApp

## Primeira entrega

Esta entrega integra o núcleo de treinamento do Flutter ao backend existente:

- autenticação e revogação de sessão;
- leitura de trilhas, módulos, atividades e quizzes;
- progresso linear por integrante;
- registro de vídeo iniciado/assistido;
- envio e correção de tentativas de quiz;
- erros no formato `ErroResposta`.

O cadastro público não será implementado. Contas continuam nascendo no painel
administrativo. A avaliação inicial de nivelamento também foi removida do fluxo
do aplicativo e não terá endpoint.

## Endpoints consumidos pelo app

| Método | Endpoint | Uso |
| --- | --- | --- |
| POST | `/api/v1/sessoes` | login por e-mail e senha |
| DELETE | `/api/v1/sessoes/atual` | logout |
| GET | `/api/v1/trilhas` | catálogo de trilhas ativas |
| GET | `/api/v1/trilhas/{trilhaId}` | módulos e atividades |
| GET | `/api/v1/atividades/{atividadeId}` | detalhe da aula |
| GET | `/api/v1/atividades/{atividadeId}/quiz` | perguntas sem gabarito |
| GET | `/api/v1/trilhas/{trilhaId}/progresso` | estado do integrante |
| POST | `/api/v1/atividades/{atividadeId}/video-iniciado` | início do vídeo |
| POST | `/api/v1/atividades/{atividadeId}/video-assistido` | conclusão de vídeo sem quiz |
| POST | `/api/v1/atividades/{atividadeId}/quiz/tentativas` | correção server-side |

Todas as rotas, exceto a emissão da sessão, usam `Authorization: Bearer`.

## Regras preservadas

- atividade bloqueada retorna `409 ATIVIDADE_BLOQUEADA`;
- atividade com quiz só conclui após aprovação;
- tentativa incompleta, duplicada ou com alternativa de outra pergunta é inválida;
- tentativas são ilimitadas e o progresso conserva a melhor nota;
- o gabarito nunca aparece em DTO de leitura ou resultado;
- dados de pessoa e progresso ficam isolados pela empresa da sessão.

## Fora desta entrega

XP, ranking, conquistas, sequência, nível calculado, selo de conclusão e os
endpoints de resumo/conclusão permanecem para uma fase posterior. O app não deve
apresentar mocks desses dados como se viessem da API.

## Status da implementação

Os endpoints desta primeira entrega já estão implementados e cobertos por testes de
integração. O teste de contrato do quiz também verifica estruturalmente que os campos
`correta` e `alternativaCorreta` não aparecem em leituras nem no resultado da tentativa.

Permanece pendente apenas a carga dos vídeos reais: ela deve entrar em uma migration nova,
com uma URL HTTPS do YouTube e a duração positiva de cada atividade. Esses valores
dependem do material oficial de treinamento e não são substituídos por placeholders.

Renovação de sessão permanece fora desta entrega e será tratada em contrato separado.
