# empresa

## Responsabilidade

Representa um cliente da Naty. E a raiz do isolamento de dados: todo integrante, todo
progresso e todo ranking pertencem a uma empresa. Este pacote nao conhece treinamento
nem trilha.

## Contratos

- `Empresa`: entidade JPA da tabela `empresa`, com nome, o token da Naty API dessa
  empresa e o indicador de ativa.
- `EmpresaRepository`: `JpaRepository<Empresa, UUID>`.

## Decisoes

O token da Naty API mora na empresa, nao em `application.yml`. A Naty API V3 nao tem
endpoint de empresa e o token bearer e amarrado a um cliente, entao atender varias
empresas significa guardar um token por linha. Colocar o token em configuracao
limitaria o sistema a um cliente so.

Conteudo de treinamento nao pertence a empresa. Todas fazem a mesma trilha, entao
nenhuma tabela de conteudo tem `empresa_id`. Se um dia existir conteudo exclusivo, ele
entra como tabela de associacao entre empresa e trilha, sem alterar o que ja existe.

## Armadilhas

`naty_api_token` e credencial. Nunca inclua esse campo em DTO de resposta, log ou
mensagem de erro. Hoje nenhum endpoint expoe `Empresa`, e essa ausencia e proposital.

Empresa inativa ainda tem integrantes e progresso no banco. Desativar nao apaga nada,
e nenhuma consulta filtra por `ativa` automaticamente. Quem precisar desse filtro
escreve ele.

## Estado atual

Real: `Empresa` e `EmpresaRepository`.

Nao existe endpoint de empresa, nem CRUD. A empresa nasce por seed. A etapa de
integracao com a Naty API define como uma empresa nova entra em producao.
