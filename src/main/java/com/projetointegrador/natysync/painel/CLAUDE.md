# painel

## Responsabilidade

Escrita do cadastro: empresa, integrante, senha e ativação. É o único pacote que escreve
em `usuario` e em `empresa`. Também cria a primeira conta `NATY` na subida.

Depende de `usuario` e de `empresa`. Nenhum pacote depende dele.

## Contratos

- `EmpresaController`: `/api/v1/painel/empresas`. Listagem paginada, detalhe, criação,
  alteração e exclusão. Exige `NATY`.
- `IntegranteController`: `/api/v1/painel/integrantes`. Exige `ADMIN`, e a empresa vem da
  identidade da requisição.
- `EmpresaIntegranteController`: `/api/v1/painel/empresas/{empresaId}/integrantes`. Exige
  `NATY`, e a empresa vem da URL.
- `IntegranteService`: toda operação recebe a empresa já resolvida pelo controller.
- `EmpresaService`: desativar empresa revoga as sessões dos integrantes dela.
- `BootstrapNaty`: `ApplicationRunner` que cria a empresa interna e a conta `NATY` a partir
  de `app.bootstrap.naty.*` quando não existe nenhum `NATY`. Recusa subir quando o
  bootstrap é obrigatório e faltam as variáveis, quando a senha está fora do limite e
  quando o e-mail pertence a uma conta que não é `NATY`.
- `TravaDaContaDeSeed`: `ApplicationRunner` que recusa subir fora do perfil `dev` quando
  encontra uma das contas do seed, cuja senha é pública.
- `CodigoDeConflito`: códigos estáveis dos 409 deste pacote.
- `ViolacaoDeConstraint`: reconhece, pelo nome, a constraint que uma
  `DataIntegrityViolationException` violou.

## Decisões

A credencial é nossa. O painel cadastra o integrante e define a senha, o backend guarda o
hash bcrypt, e nenhum serviço externo responde por quem existe.

Os papéis são `INTEGRANTE`, `ADMIN` e `NATY`, sem hierarquia. `ADMIN` cadastra integrante
e `ADMIN` na própria empresa, e altera, redefine senha e desativa só contas `INTEGRANTE`
dela: conta `ADMIN` recebe 403 `ACESSO_NEGADO`. `NATY` é o time da Naty e administra
empresas e as contas `INTEGRANTE` e `ADMIN` de qualquer uma. A regra de rota mora em
`config/SecurityConfig`, e a regra sobre a conta alvo mora em `IntegranteService`.

Dois controllers sobre o mesmo serviço, porque a empresa do `ADMIN` vem da identidade e
nunca da URL, e a do `NATY` vem da URL, já que atravessar empresas é o papel dele.

`NATY` nasce só pelo bootstrap ou pelo seed. O corpo de cadastro e de alteração recusa
`papel: NATY` com 400, e conta `NATY` recebe 409 `CONTA_NATY_FORA_DO_PAINEL` em qualquer
escrita pelo painel. Isso fecha a escalada de privilégio pela própria API.

Integrante nunca é apagado: não existe `DELETE`. Desativar revoga as sessões e preserva o
progresso, que aponta para ele.

`ADMIN` não administra conta `ADMIN`, nem a própria. Um `ADMIN` que redefine a senha de
outro assume a conta dele, e um token roubado viraria credencial permanente pela troca da
própria senha. A consequência é que ninguém altera, redefine ou desativa a própria conta
pelo painel.

Empresa com conta `NATY` não é desativada nem excluída (409 `EMPRESA_COM_CONTA_NATY`).
Senão um `NATY` tranca outro, e o bootstrap não recupera, porque ele conta `NATY` inativo.

Escrita em integrante trava a linha do usuário, e desativar empresa trava as linhas dos
integrantes dela. O login trava a mesma linha, então os dois se serializam e nenhuma sessão
escapa da revogação.

Violação de integridade só vira `EMAIL_JA_CADASTRADO` quando a constraint é
`usuario_email_idx`. Qualquer outra sobe como erro, para bug de schema não se disfarçar de
conflito de negócio. A exclusão de empresa traduz violação de FK em `EMPRESA_COM_VINCULOS`,
porque um cadastro concorrente pode entrar entre a checagem e o `delete`.

`BootstrapNaty` toma `pg_advisory_xact_lock` antes de checar se existe `NATY`. Réplicas que
sobem juntas se enfileiram, e só a primeira cria a conta.

O controller, o serviço e os DTOs de empresa moram aqui e não em `empresa`, porque
desativar empresa revoga sessão em `usuario`, e `usuario` depende de `empresa`.

## Armadilhas

E-mail é normalizado no construtor do record de entrada, em minúsculas e sem espaço nas
pontas. A validação do Bean Validation roda depois do construtor, então normalizar no
serviço chegaria tarde.

A senha tem de 8 caracteres a 72 bytes em UTF-8. 72 bytes é o limite do bcrypt, e o
`BCryptPasswordEncoder` recusa acima disso com exceção, não com erro de validação.

Todo record que carrega senha sobrescreve `toString` com a senha mascarada. Record novo
com senha precisa fazer o mesmo, senão um log de depuração vaza a senha.

Integrante de outra empresa responde 404, nunca 403. O 403 confirmaria que o identificador
existe.

A unicidade de e-mail é global. O cadastro de uma empresa recebe 409 para e-mail que existe
em outra, e isso revela que o e-mail existe em algum lugar. É o preço do login sem escolha
de empresa.

A listagem ignora o parâmetro `sort` do cliente e ordena por nome e identificador. Ordenar
por campo livre expõe coluna e quebra paginação estável.

Validação com `@AssertTrue` em método `isX` do record gera erro de campo com o nome `x`,
não com o nome do componente. O app lê `codigo`, não o nome do campo.

O seed de desenvolvimento faz upsert pelos ids fixos. A cada mudança de checksum as três
contas dele voltam à senha e ao estado do seed, e o que o painel cadastrou nas empresas do
seed fica intacto.

`PUT` de integrante sem `perfil` mantém o perfil atual. O padrão `user` vale só no
cadastro.

## Ausências deliberadas

Nenhuma rota exclui integrante.

Conta `NATY` não troca senha pelo painel. A senha dela vem do bootstrap ou do seed, e
recuperar a senha do único `NATY` é operação no banco.
