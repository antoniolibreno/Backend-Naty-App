# empresa

## Responsabilidade

Representa um cliente da Naty. É a raiz do isolamento de dados: todo integrante, todo
progresso e todo ranking pertencem a uma empresa. Este pacote não conhece treinamento
nem trilha.

## Contratos

- `Empresa`: entidade JPA da tabela `empresa`, com nome, o token da Naty API dessa
  empresa e o indicador de ativa.
- `EmpresaRepository`: `JpaRepository<Empresa, UUID>`.

## Decisões

O token da Naty API mora na empresa, não em `application.yml`. A Naty API V3 não tem
endpoint de empresa e o token bearer é amarrado a um cliente, então atender várias
empresas significa guardar um token por linha. Colocar o token em configuração
limitaria o sistema a um cliente só.

Conteúdo de treinamento não pertence à empresa. Todas fazem a mesma trilha, então
nenhuma tabela de conteúdo tem `empresa_id`. Se um dia existir conteúdo exclusivo, ele
entra como tabela de associação entre empresa e trilha, sem alterar o que já existe.

## Armadilhas

`naty_api_token` é credencial. Nunca inclua esse campo em DTO de resposta, log ou
mensagem de erro. Hoje nenhum endpoint expõe `Empresa`, e essa ausência é proposital.

Empresa inativa ainda tem integrantes e progresso no banco. Desativar não apaga nada,
e nenhuma consulta filtra por `ativa` automaticamente. Quem precisar desse filtro
escreve ele.

## Estado atual

Real: `Empresa` e `EmpresaRepository`.

Não existe endpoint de empresa, nem CRUD. A empresa nasce por seed. A etapa de
integração com a Naty API define como uma empresa nova entra em produção.
