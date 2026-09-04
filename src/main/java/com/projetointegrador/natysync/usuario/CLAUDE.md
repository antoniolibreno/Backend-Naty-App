# usuario

## Responsabilidade

Espelho local dos integrantes que vivem no Naty App, e a API REST que o app Flutter
consome para le-los. Este pacote nunca cadastra usuario proprio: quem escreve na
tabela e o pacote `sincronizacao`.

O usuario e o sujeito do treinamento. `progresso` e `gamificacao` apontam para ele,
mas ele nao conhece nenhum dos dois.

## Contratos

- `Usuario`: entidade JPA mapeada na tabela `usuario`.
- `UsuarioRepository`: vai estender `JpaRepository<Usuario, UUID>`.
- `UsuarioService`: leitura e regra de consulta. Sem metodo de escrita exposto para
  o controller.
- `UsuarioController`: `/api/v1/usuarios`, so verbos de leitura.
- `UsuarioMapper`: traduz `NatyUsuarioResponse` em `Usuario` e `Usuario` em
  `UsuarioResponse`.
- `dto/UsuarioResponse` e `dto/UsuarioFiltro`: contrato de saida e de filtro de busca.

## Decisoes

A entidade se chama `Usuario`, sem sufixo `Entity`. O nome do dominio e o nome da
classe.

`UsuarioController` expoe apenas leitura. Criar, alterar ou apagar usuario aqui
inverteria a fonte da verdade, que e o Naty App. Escrita chega so pela sincronizacao.

A tabela `usuario` guarda o payload cru da Naty API em coluna `payload jsonb`, alem
das colunas tipadas. Campo novo que a Naty adicionar fica disponivel sem migration,
e so vira coluna quando alguem precisar consultar por ele.

Todo usuario pertence a uma empresa. `empresa_id` e obrigatorio e nenhuma consulta de
usuario roda sem filtro de empresa. O token da Naty API e por empresa, entao dois
integrantes de clientes diferentes podem colidir em qualquer campo menos nesse par.

## Armadilhas

`naty_id` e a chave natural vinda da Naty API, e o indice unico e no par
`(empresa_id, naty_id)`, nao em `naty_id` sozinho. O upsert da sincronizacao depende
disso. Remover essa restricao transforma resincronizacao em duplicacao silenciosa de
linha.

O campo `perfil` (`admin`, `supervisor`, `user`) vem da Naty API e nao muda nada no
treinamento: todos fazem a mesma trilha. Nao use esse campo como permissao.

`ddl-auto` esta em `validate`. Adicionar campo na entidade sem escrever a migration
correspondente derruba a aplicacao na subida, e isso e proposital.

## Estado atual

Real: `Usuario` como entidade JPA ligada a `Empresa`, `UsuarioRepository` com busca
por e-mail normalizado, `UsuarioService` com a resolucao de integrante,
`SessaoController` e os DTOs `SessaoRequest` e `SessaoResponse`.

`SessaoRequest` normaliza o e-mail no proprio construtor do record, em minusculas e
sem espaco nas pontas. Isso e obrigatorio e nao e detalhe: a validacao do Bean
Validation roda depois do construtor, entao normalizar no servico chegaria tarde e um
e-mail com espaco seria rejeitado com 400 antes de qualquer busca.

Stub: `UsuarioController`, `UsuarioMapper`, `dto/UsuarioResponse` e
`dto/UsuarioFiltro`. O `UsuarioMapper` e preenchido pela etapa da integracao com a
Naty API, que precisa converter `NatyUsuarioResponse` em `Usuario`. Os outros esperam
a etapa que tiver um caso de uso de listagem de integrante.
