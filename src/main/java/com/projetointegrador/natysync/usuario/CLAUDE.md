# usuario

## Responsabilidade

Integrantes das empresas e a API REST que o app Flutter consome para lê-los. Este pacote só
lê: quem escreve na tabela é o pacote `painel`.

O usuário é o sujeito do treinamento. `progresso` e `gamificacao` apontam para ele, e ele
não conhece nenhum dos dois.

## Contratos

- `Usuario`: entidade JPA mapeada na tabela `usuario`, ligada a `Empresa`.
- `UsuarioRepository`: busca por e-mail normalizado e busca por identificador com a
  empresa.
- `UsuarioService`: resolução de integrante. Sem método de escrita exposto para o
  controller.
- `SessaoController`: `POST /api/v1/sessoes`.
- `dto/SessaoRequest` e `dto/SessaoResponse`.
- `IntegranteDaRequisicao` e `IntegranteArgumentResolver`: injetam o integrante da chamada
  nos controllers que precisam saber de quem é a operação. O registro do resolvedor fica em
  `config/WebMvcResolverConfig`.

## Decisões

A entidade se chama `Usuario`, sem sufixo `Entity`. O nome do domínio é o nome da classe.

`UsuarioController` expõe apenas leitura. Criar, alterar ou apagar integrante é trabalho do
`painel`, e concentrar a escrita num lugar só é o que mantém a regra auditável.

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

A senha de desenvolvimento das três contas do seed é `desenvolvimento`.

O campo `perfil` (`admin`, `supervisor`, `user`) descreve o que a pessoa faz no WhatsApp e
não muda nada no treinamento: todos fazem a mesma trilha. Não use esse campo como
permissão. O papel administrativo é coluna própria, criada pelo painel.

`ddl-auto` está em `validate`. Adicionar campo na entidade sem escrever a migration
correspondente derruba a aplicação na subida, e isso é proposital.

## Ausências deliberadas

`UsuarioController` declara o caminho `/api/v1/usuarios` e não mapeia nenhum método.
`UsuarioMapper`, `dto/UsuarioResponse` e `dto/UsuarioFiltro` não têm conteúdo. O caso de uso
de listagem de integrante pertence ao painel.

A tabela `usuario` é populada pelo seed de desenvolvimento.

A sessão não renova: quando o token expira, o app autentica de novo.
