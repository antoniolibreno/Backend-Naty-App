# usuario

## Responsabilidade

Integrantes das empresas e a API REST que o app Flutter consome para le-los. Este
pacote só lê: quem escreve na tabela é o pacote `painel`, que ainda não existe.

O usuário é o sujeito do treinamento. `progresso` e `gamificacao` apontam para ele,
mas ele não conhece nenhum dos dois.

## Contratos

- `Usuario`: entidade JPA mapeada na tabela `usuario`.
- `UsuarioRepository`: vai estender `JpaRepository<Usuario, UUID>`.
- `UsuarioService`: leitura e regra de consulta. Sem método de escrita exposto para
  o controller.
- `UsuarioController`: `/api/v1/usuarios`, só verbos de leitura.
- `UsuarioMapper`: traduz `Usuario` em `UsuarioResponse`.
- `dto/UsuarioResponse` e `dto/UsuarioFiltro`: contrato de saída e de filtro de busca.

## Decisões

A entidade se chama `Usuario`, sem sufixo `Entity`. O nome do domínio é o nome da
classe.

`UsuarioController` expõe apenas leitura. Criar, alterar ou apagar integrante é
trabalho do `painel`, e concentrar a escrita num lugar só é o que mantém a regra
auditável.

Todo usuário pertence a uma empresa. `empresa_id` é obrigatório e nenhuma consulta de
usuário roda sem filtro de empresa.

## Armadilhas

A migration `V5` removeu `naty_id`, `payload`, `ultimo_acesso_naty` e
`sincronizado_em`, junto com o índice único de `(empresa_id, naty_id)`. O identificador
natural do integrante passou a ser o e-mail, e `usuario_empresa_email_idx` é o único
índice único que sobrou. A decisão de e-mail único no sistema inteiro está em
`docs/decisoes.md` e vira migration na etapa do painel.

O campo `perfil` (`admin`, `supervisor`, `user`) descreve o que a pessoa faz no
WhatsApp e não muda nada no treinamento: todos fazem a mesma trilha. Não use esse campo
como permissão. O papel administrativo nasce como coluna própria na etapa do painel.

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
`dto/UsuarioFiltro`. Todos esperam a etapa que tiver um caso de uso de listagem de
integrante, que é a do painel.
