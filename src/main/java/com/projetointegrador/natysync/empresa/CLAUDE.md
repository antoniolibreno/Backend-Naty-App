# empresa

## Responsabilidade

Representa um cliente da Naty. É a raiz do isolamento de dados: todo integrante, todo
progresso e todo ranking pertencem a uma empresa. Este pacote não conhece treinamento nem
trilha.

## Contratos

- `Empresa`: entidade JPA da tabela `empresa`, com nome e o indicador de ativa.
- `EmpresaRepository`: `JpaRepository<Empresa, UUID>`.

## Decisões

Conteúdo de treinamento não pertence à empresa. Todas fazem a mesma trilha, então nenhuma
tabela de conteúdo tem `empresa_id`. Conteúdo exclusivo, se existir, entra como tabela de
associação entre empresa e trilha, sem alterar o que já existe.

## Armadilhas

`Empresa` não gera identificador e não preenche os carimbos de data que as outras
entidades preenchem. Persistir uma empresa por código exige montar o id à mão.

Empresa inativa continua com integrantes e progresso no banco. Desativar não apaga nada, e
nenhuma consulta filtra por `ativa` automaticamente. Quem precisar desse filtro escreve
ele.

## Ausências deliberadas

Nenhum endpoint expõe `Empresa`. O cadastro pertence ao pacote `painel`, junto com o fuso
horário que a sequência de dias exige.

A empresa existente vem do seed de desenvolvimento.
