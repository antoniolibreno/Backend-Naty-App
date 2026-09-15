# empresa

## Responsabilidade

Representa um cliente da Naty. É a raiz do isolamento de dados: todo integrante, todo
progresso e todo ranking pertencem a uma empresa. Este pacote não conhece treinamento
nem trilha.

## Contratos

- `Empresa`: entidade JPA da tabela `empresa`, com nome e o indicador de ativa.
- `EmpresaRepository`: `JpaRepository<Empresa, UUID>`.

## Decisões

Conteúdo de treinamento não pertence à empresa. Todas fazem a mesma trilha, então
nenhuma tabela de conteúdo tem `empresa_id`. Se um dia existir conteúdo exclusivo, ele
entra como tabela de associação entre empresa e trilha, sem alterar o que já existe.

## Armadilhas

Hoje nenhum endpoint expõe `Empresa`, e essa ausência é proposital até o pacote
`painel` existir.

Empresa inativa ainda tem integrantes e progresso no banco. Desativar não apaga nada,
e nenhuma consulta filtra por `ativa` automaticamente. Quem precisar desse filtro
escreve ele.

## Estado atual

Real: `Empresa` e `EmpresaRepository`.

Não existe endpoint de empresa, nem CRUD. A empresa nasce por seed, e a entidade não
gera id nem preenche os carimbos de data, então persistir por código falha hoje. O
pacote `painel` conserta isso e passa a cadastrar empresa com fuso horário.
