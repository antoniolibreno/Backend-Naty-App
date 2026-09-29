# empresa

## Responsabilidade

Representa um cliente da Naty. É a raiz do isolamento de dados: todo integrante, todo
progresso e todo ranking pertencem a uma empresa. Este pacote não conhece treinamento nem
trilha.

## Contratos

- `Empresa`: entidade JPA da tabela `empresa`, com nome, indicador de ativa, fuso horário
  e carimbos de data. Identificador e carimbos são gerados.
- `Empresa.getFusoHorario()`: `ZoneId`, sempre preenchido. A virada do dia de qualquer
  regra por data acontece nesse fuso.
- `ZoneIdConverter`: grava o fuso como identificador de região em `empresa.fuso_horario`.
- `EmpresaRepository`: `JpaRepository<Empresa, UUID>`.

## Decisões

Conteúdo de treinamento não pertence à empresa. Todas fazem a mesma trilha, então nenhuma
tabela de conteúdo tem `empresa_id`. Conteúdo exclusivo, se existir, entra como tabela de
associação entre empresa e trilha, sem alterar o que já existe.

A escrita de empresa mora em `painel`. Desativar empresa revoga sessão em `usuario`, e
`usuario` depende de `empresa`, então escrever daqui criaria ciclo.

Empresa inativa bloqueia o login e as sessões abertas dos integrantes dela. A checagem
mora em `Usuario.podeEntrar`.

## Armadilhas

`Empresa.ativa` e `Empresa.fusoHorario` nascem com valor no próprio campo. O padrão do
banco não alcança quem persiste pela JPA, porque o insert carrega a coluna explicitamente.

Com identificador gerado, salvar uma `Empresa` nova com o id preenchido é tratado como
`merge` e falha no Hibernate. Crie sem id.

Empresa inativa continua com integrantes e progresso no banco. Desativar não apaga nada, e
nenhuma consulta filtra por `ativa` automaticamente. `Usuario.podeEntrar` confere o
indicador no login e em cada chamada autenticada; qualquer outra leitura que precise do
filtro escreve ele.

A empresa interna da Naty, onde mora a conta `NATY`, aparece na listagem de empresas como
qualquer outra. Empresa com conta `NATY` não é desativada nem excluída.
