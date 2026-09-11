# usuario

## Responsabilidade

Espelho local dos integrantes que vivem no Naty App, e a API REST que o app Flutter
consome para le-los. Este pacote nunca cadastra usuário próprio: quem escreve na
tabela é o pacote `sincronizacao`.

O usuário é o sujeito do treinamento. `progresso` e `gamificacao` apontam para ele,
mas ele não conhece nenhum dos dois.

## Contratos

- `Usuario`: entidade JPA mapeada na tabela `usuario`.
- `UsuarioRepository`: vai estender `JpaRepository<Usuario, UUID>`.
- `UsuarioService`: leitura e regra de consulta. Sem método de escrita exposto para
  o controller.
- `UsuarioController`: `/api/v1/usuarios`, só verbos de leitura.
- `UsuarioMapper`: traduz `NatyUsuarioResponse` em `Usuario` e `Usuario` em
  `UsuarioResponse`.
- `dto/UsuarioResponse` e `dto/UsuarioFiltro`: contrato de saída e de filtro de busca.

## Decisões

A entidade se chama `Usuario`, sem sufixo `Entity`. O nome do domínio é o nome da
classe.

`UsuarioController` expõe apenas leitura. Criar, alterar ou apagar usuário aqui
inverteria a fonte da verdade, que é o Naty App. Escrita chega só pela sincronização.

A tabela `usuario` guarda o payload cru da Naty API em coluna `payload jsonb`, além
das colunas tipadas. Campo novo que a Naty adicionar fica disponível sem migration,
e só vira coluna quando alguém precisar consultar por ele.

Todo usuário pertence a uma empresa. `empresa_id` é obrigatório e nenhuma consulta de
usuário roda sem filtro de empresa. O token da Naty API é por empresa, então dois
integrantes de clientes diferentes podem colidir em qualquer campo menos nesse par.

## Armadilhas

`naty_id` é a chave natural vinda da Naty API, e o índice único é no par
`(empresa_id, naty_id)`, não em `naty_id` sozinho. O upsert da sincronização depende
disso. Remover essa restrição transforma resincronização em duplicação silenciosa de
linha.

O campo `perfil` (`admin`, `supervisor`, `user`) vem da Naty API e não muda nada no
treinamento: todos fazem a mesma trilha. Não use esse campo como permissão.

`ddl-auto` está em `validate`. Adicionar campo na entidade sem escrever a migration
correspondente derruba a aplicação na subida, e isso é proposital.

## Estado atual

Real: `Usuario` como entidade JPA ligada a `Empresa`, `UsuarioRepository` com busca
por e-mail normalizado e busca por identificador com a empresa, `UsuarioService` com a
resolução de integrante, `SessaoController`, os DTOs `SessaoRequest` e `SessaoResponse`,
`IntegranteDaRequisicao` e `IntegranteArgumentResolver`.

`SessaoRequest` exige senha além do e-mail, e a senha é descartada. Ela não é verificada,
não é guardada e não é registrada em log: existe só para o app já mandar o corpo
definitivo antes da etapa de autenticação. Qualquer senha não vazia é aceita. Não trate
isso como implementação pela metade, e não escreva verificação de senha aqui sem a
migration de credencial da etapa de autenticação.

`IntegranteArgumentResolver` lê o cabeçalho `X-Integrante-Id` e injeta
`IntegranteDaRequisicao` nos controllers que precisam saber de quem é a operação. O
registro dele fica em `config/WebMvcResolverConfig`. O nome é `IntegranteDaRequisicao`, e
não `IntegranteAutenticado`, porque é o integrante que a requisição alega ser: o
cabeçalho é forjável enquanto não existir autenticação. Quando ela entrar, só a fonte do
identificador muda dentro do resolvedor, e nenhum controller é reescrito.

`SessaoRequest` normaliza o e-mail no próprio construtor do record, em minúsculas e
sem espaço nas pontas. Isso é obrigatório e não é detalhe: a validação do Bean
Validation roda depois do construtor, então normalizar no serviço chegaria tarde e um
e-mail com espaço seria rejeitado com 400 antes de qualquer busca.

Stub: `UsuarioController`, `UsuarioMapper`, `dto/UsuarioResponse` e
`dto/UsuarioFiltro`. O `UsuarioMapper` é preenchido pela etapa da integração com a
Naty API, que precisa converter `NatyUsuarioResponse` em `Usuario`. Os outros esperam
a etapa que tiver um caso de uso de listagem de integrante.
