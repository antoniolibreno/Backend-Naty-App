# usuario

## Responsabilidade

Integrantes das empresas, a autenticação e a sessão. Este pacote não escreve cadastro:
quem escreve em `usuario` é o pacote `painel`.

O usuário é o sujeito do treinamento. `progresso` e `gamificacao` apontam para ele, e ele
não conhece nenhum dos dois.

## Contratos

- `Usuario`: entidade JPA mapeada na tabela `usuario`, ligada a `Empresa`.
- `Usuario.podeEntrar()`: integrante ativo de empresa ativa. É a regra única de quem entra,
  usada no login e em cada chamada autenticada.
- `UsuarioRepository`: busca por e-mail normalizado, busca por identificador com a empresa,
  busca por identificador dentro de uma empresa e `JpaSpecificationExecutor` para a
  listagem do painel.
- `Papel`: `INTEGRANTE`, `ADMIN` e `NATY`.
- `UsuarioService`: resolução de integrante. Sem método de escrita exposto para o
  controller.
- `SessaoController`: `POST /api/v1/sessoes`.
- `dto/SessaoRequest` e `dto/SessaoResponse`.
- `IntegranteDaRequisicao` e `IntegranteArgumentResolver`: injetam o integrante, a empresa
  e o papel da chamada nos controllers que precisam saber de quem é a operação. O registro
  do resolvedor fica em `config/WebMvcResolverConfig`.
- `TokenSessaoFiltro`: põe `ROLE_<papel>` como autoridade no contexto de segurança.
- `SessaoService.revogarTodasDoIntegrante` e `revogarTodasDaEmpresa`: revogação em lote que
  o `painel` chama na troca de senha e na desativação.

## Decisões

A entidade se chama `Usuario`, sem sufixo `Entity`. O nome do domínio é o nome da classe.

Criar, alterar e desativar integrante é trabalho do `painel`. Concentrar a escrita num
lugar só é o que mantém a regra auditável.

A sessão confere `Usuario.podeEntrar()` a cada chamada. A busca da sessão já faz
`join fetch` do usuário e da empresa, então a checagem não custa consulta, e desativar
integrante ou empresa vale na chamada seguinte. A revogação em lote garante que reativar
não ressuscita token antigo.

Todo usuário pertence a uma empresa. `empresa_id` é obrigatório e nenhuma consulta de
usuário roda sem filtro de empresa.

`IntegranteArgumentResolver` lê o integrante do contexto de segurança, preenchido por
`TokenSessaoFiltro`. O identificador nunca vem do corpo, da URL ou de cabeçalho próprio.

O token é opaco e o banco guarda só o hash SHA-256 dele, nunca o valor em claro. Vazamento
do banco não vira sessão ativa. A senha usa bcrypt, que é lento de propósito; o token não
precisa disso porque já nasce com 32 bytes de entropia.

E-mail inexistente e senha incorreta devolvem a mesma recusa, com a mesma mensagem. Separar
as duas respostas entregaria a lista de quem tem conta.

`SessaoRequest` normaliza o e-mail no próprio construtor do record, em minúsculas e sem
espaço nas pontas. Isso é obrigatório e não é detalhe: a validação do Bean Validation roda
depois do construtor, então normalizar no serviço chegaria tarde e um e-mail com espaço
seria rejeitado com 400 antes de qualquer busca.

## Armadilhas

A senha, o hash dela e o token de sessão nunca entram em log nem em mensagem de erro. O
token em claro existe uma única vez, na resposta da emissão.

O identificador natural do integrante é o e-mail, e `usuario_email_idx` é único no sistema
inteiro sobre `lower(email)`. É ele que resolve a empresa no login sem o app informar
empresa. A mesma pessoa em duas empresas precisa de dois e-mails.

`Usuario.papel` e `Usuario.ativo` nascem com valor no próprio campo, e não só com valor
padrão no banco. O padrão do banco não alcança quem persiste pela JPA, porque o insert
carrega a coluna explicitamente.

A senha de desenvolvimento das três contas do seed é `123qweasd`: `admin@admin.com`
(`ADMIN`), `user@user.com` (`INTEGRANTE`) e `naty@naty.com` (`NATY`).

O campo `perfil` (`admin`, `supervisor`, `user`) descreve o que a pessoa faz no WhatsApp e
não muda nada no treinamento: todos fazem a mesma trilha. Não use esse campo como
permissão. A permissão é `usuario.papel`, restrita por check constraint aos três valores de
`Papel`.

`IntegranteDaRequisicao` é construído só em `SessaoService`. Componente novo no record
quebra quem o constrói, e não quem só lê `usuarioId` e `empresaId`.

`ddl-auto` está em `validate`. Adicionar campo na entidade sem escrever a migration
correspondente derruba a aplicação na subida, e isso é proposital.

## Ausências deliberadas

`UsuarioController` declara o caminho `/api/v1/usuarios` e não mapeia nenhum método.
`UsuarioMapper`, `dto/UsuarioResponse` e `dto/UsuarioFiltro` não têm conteúdo. A listagem de
integrante para quem administra mora no `painel`.

A sessão não renova: quando o token expira, o app autentica de novo.
