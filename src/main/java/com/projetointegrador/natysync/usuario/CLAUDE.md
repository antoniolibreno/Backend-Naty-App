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

O nome é `IntegranteDaRequisicao`, e não `IntegranteAutenticado`, porque é o integrante que
a requisição alega ser. A troca do cabeçalho por autenticação muda só a fonte do
identificador dentro do resolvedor, e nenhum controller é reescrito.

`SessaoRequest` normaliza o e-mail no próprio construtor do record, em minúsculas e sem
espaço nas pontas. Isso é obrigatório e não é detalhe: a validação do Bean Validation roda
depois do construtor, então normalizar no serviço chegaria tarde e um e-mail com espaço
seria rejeitado com 400 antes de qualquer busca.

## Armadilhas

`SessaoRequest` exige senha além do e-mail, e a senha é descartada. Ela não é verificada,
não é guardada e não é registrada em log. Qualquer senha não vazia é aceita. Não trate isso
como implementação pela metade, e não escreva verificação de senha aqui sem a migration de
credencial.

`IntegranteArgumentResolver` lê o cabeçalho `X-Integrante-Id`, que é forjável: nenhuma
verificação de identidade acontece.

O identificador natural do integrante é o e-mail, e `usuario_empresa_email_idx` é o único
índice único da tabela. A unicidade de e-mail no sistema inteiro está registrada em
`docs/regras.md` e depende de migration própria.

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

`POST /api/v1/sessoes` não emite token, credencial nem cookie.
