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
  de `app.bootstrap.naty.*` quando não existe nenhum `NATY`.
- `CodigoDeConflito`: códigos estáveis dos 409 deste pacote.

## Decisões

A credencial é nossa. O painel cadastra o integrante e define a senha, o backend guarda o
hash bcrypt, e nenhum serviço externo responde por quem existe.

Os papéis são `INTEGRANTE`, `ADMIN` e `NATY`. `ADMIN` administra os integrantes da própria
empresa. `NATY` é o time da Naty: administra empresas e os integrantes de qualquer uma. A
regra de papel mora em `config/SecurityConfig`, por prefixo de rota.

Dois controllers sobre o mesmo serviço, porque a empresa do `ADMIN` vem da identidade e
nunca da URL, e a do `NATY` vem da URL, já que atravessar empresas é o papel dele.

`NATY` nasce só pelo bootstrap ou pelo seed. O corpo de cadastro e de alteração recusa
`papel: NATY` com 400, e conta `NATY` recebe 409 `CONTA_NATY_FORA_DO_PAINEL` em qualquer
escrita pelo painel. Isso fecha a escalada de privilégio pela própria API.

Integrante nunca é apagado: não existe `DELETE`. Desativar revoga as sessões e preserva o
progresso, que aponta para ele.

Ninguém altera o próprio papel nem se desativa, e `NATY` não desativa nem exclui a empresa
a que pertence. Sem isso o último administrador tranca todos para fora.

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

O seed de desenvolvimento apaga os integrantes das empresas dele a cada mudança de
checksum. Integrante cadastrado pelo painel na `Empresa Exemplo` ou na `Naty` some quando o
seed roda de novo.

## Ausências deliberadas

Nenhuma rota exclui integrante.

Conta `NATY` não troca senha pelo painel. A senha dela vem do bootstrap ou do seed.
