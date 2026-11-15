-- Cenario repetivel para o app Flutter testar login, trilha, video, quiz e progresso.
-- Login principal: user@user.com / 123qweasd

insert into trilha (id, titulo, descricao, ordem, ativa)
values (
    '00000000-0000-0000-0000-000000000101',
    'Primeiros passos na Naty',
    'Trilha de demonstracao para validar o fluxo completo do aplicativo.',
    10,
    true
)
on conflict (id) do update set
    titulo = excluded.titulo,
    descricao = excluded.descricao,
    ordem = excluded.ordem,
    ativa = excluded.ativa;

insert into trilha (id, titulo, descricao, ordem, ativa)
values (
    '00000000-0000-0000-0000-000000000102',
    'Atendimento avancado na Naty',
    'Segunda trilha de demonstracao, liberada depois dos primeiros passos.',
    20,
    true
)
on conflict (id) do update set
    titulo = excluded.titulo,
    descricao = excluded.descricao,
    ordem = excluded.ordem,
    ativa = excluded.ativa;

insert into modulo (id, trilha_id, titulo, descricao, ordem)
values
    ('00000000-0000-0000-0001-000000000101', '00000000-0000-0000-0000-000000000101',
     'Comece aqui', 'Conheca o fluxo basico de atendimento.', 1),
    ('00000000-0000-0000-0001-000000000102', '00000000-0000-0000-0000-000000000101',
     'Atendimento na pratica', 'Aplique o que aprendeu em situacoes do dia a dia.', 2)
    ,('00000000-0000-0000-0001-000000000201', '00000000-0000-0000-0000-000000000102',
     'Casos avancados', 'Pratique situacoes mais complexas de atendimento.', 1)
    ,('00000000-0000-0000-0001-000000000202', '00000000-0000-0000-0000-000000000102',
     'Consolidacao', 'Revise e conclua a segunda trilha.', 2)
on conflict (id) do update set
    trilha_id = excluded.trilha_id,
    titulo = excluded.titulo,
    descricao = excluded.descricao,
    ordem = excluded.ordem;

insert into atividade (id, modulo_id, titulo, descricao, ordem, imagem_url, video_url, duracao_segundos, xp)
values
    ('00000000-0000-0000-0002-000000000101', '00000000-0000-0000-0001-000000000101',
     'Boas-vindas', 'Assista ao video de introducao da plataforma.', 1,
     '/imagens/demo-boas-vindas.png', 'https://youtu.be/dQw4w9WgXcQ', 30, 10),
    ('00000000-0000-0000-0002-000000000102', '00000000-0000-0000-0001-000000000101',
     'Conhecendo a caixa de entrada', 'Veja como organizar as conversas recebidas.', 2,
     '/imagens/demo-caixa-entrada.png', 'https://youtu.be/9bZkp7q19f0', 45, 10),
    ('00000000-0000-0000-0002-000000000103', '00000000-0000-0000-0001-000000000101',
     'Revisao do primeiro modulo', 'Responda ao quiz sem precisar de video.', 3,
     '/imagens/demo-revisao.png', 'https://youtu.be/kJQP7kiw5Fk', 40, 10),
    ('00000000-0000-0000-0002-000000000104', '00000000-0000-0000-0001-000000000102',
     'Respondendo um ticket', 'Assista a um exemplo de atendimento.', 1,
     '/imagens/demo-ticket.png', 'https://youtu.be/jNQXAC9IVRw', 60, 10),
    ('00000000-0000-0000-0002-000000000105', '00000000-0000-0000-0001-000000000102',
     'Desafio final', 'Conclua a ultima atividade do cenario.', 2,
     '/imagens/demo-desafio.png', 'https://youtu.be/fJ9rUzIMcZQ', 55, 10),
    ('00000000-0000-0000-0002-000000000201', '00000000-0000-0000-0001-000000000201',
     'Priorizando atendimentos', 'Aprenda a identificar o que deve ser tratado primeiro.', 1,
     '/imagens/demo-priorizacao.png', 'https://youtu.be/ScMzIvxBSi4', 45, 15),
    ('00000000-0000-0000-0002-000000000202', '00000000-0000-0000-0001-000000000201',
     'Comunicacao clara', 'Veja como estruturar uma resposta objetiva.', 2,
     '/imagens/demo-comunicacao.png', 'https://youtu.be/ysz5S6PUM-U', 50, 15),
    ('00000000-0000-0000-0002-000000000203', '00000000-0000-0000-0001-000000000202',
     'Revisao avancada', 'Teste seus conhecimentos antes de finalizar a trilha.', 1,
     '/imagens/demo-revisao-avancada.png', 'https://youtu.be/ZZ5LpwO-An4', 45, 20),
    ('00000000-0000-0000-0002-000000000204', '00000000-0000-0000-0001-000000000202',
     'Conclusao da trilha', 'Finalize o percurso avancado.', 2,
     '/imagens/demo-conclusao.png', 'https://youtu.be/aqz-KE-bpKQ', 60, 20)
on conflict (id) do update set
    modulo_id = excluded.modulo_id,
    titulo = excluded.titulo,
    descricao = excluded.descricao,
    ordem = excluded.ordem,
    imagem_url = excluded.imagem_url,
    video_url = excluded.video_url,
    duracao_segundos = excluded.duracao_segundos,
    xp = excluded.xp;

insert into quiz (id, atividade_id, nota_minima)
values
    ('00000000-0000-0000-0003-000000000101', '00000000-0000-0000-0002-000000000102', 70),
    ('00000000-0000-0000-0003-000000000102', '00000000-0000-0000-0002-000000000103', 70),
    ('00000000-0000-0000-0003-000000000103', '00000000-0000-0000-0002-000000000104', 70)
    ,('00000000-0000-0000-0003-000000000201', '00000000-0000-0000-0002-000000000202', 70)
    ,('00000000-0000-0000-0003-000000000202', '00000000-0000-0000-0002-000000000203', 70)
on conflict (id) do update set
    atividade_id = excluded.atividade_id,
    nota_minima = excluded.nota_minima;

insert into pergunta (id, quiz_id, enunciado, ordem)
values
    ('00000000-0000-0000-0004-000000000101', '00000000-0000-0000-0003-000000000101',
     'Qual e o primeiro passo ao receber uma conversa?', 1),
    ('00000000-0000-0000-0004-000000000102', '00000000-0000-0000-0003-000000000101',
     'Onde ficam as conversas que precisam de atendimento?', 2),
    ('00000000-0000-0000-0004-000000000103', '00000000-0000-0000-0003-000000000102',
     'O que deve ser feito antes de responder?', 1),
    ('00000000-0000-0000-0004-000000000104', '00000000-0000-0000-0003-000000000103',
     'Quando um ticket pode ser encerrado?', 1),
    ('00000000-0000-0000-0004-000000000201', '00000000-0000-0000-0003-000000000201',
     'Como priorizar um atendimento?', 1),
    ('00000000-0000-0000-0004-000000000202', '00000000-0000-0000-0003-000000000201',
     'O que torna uma resposta clara?', 2),
    ('00000000-0000-0000-0004-000000000203', '00000000-0000-0000-0003-000000000202',
     'Qual e o objetivo da revisao?', 1)
on conflict (id) do update set
    quiz_id = excluded.quiz_id,
    enunciado = excluded.enunciado,
    ordem = excluded.ordem;

insert into alternativa (id, pergunta_id, texto, correta, ordem)
values
    ('00000000-0000-0000-0005-000000000101', '00000000-0000-0000-0004-000000000101', 'Ler o historico da conversa', true, 1),
    ('00000000-0000-0000-0005-000000000102', '00000000-0000-0000-0004-000000000101', 'Apagar a conversa', false, 2),
    ('00000000-0000-0000-0005-000000000103', '00000000-0000-0000-0004-000000000101', 'Trocar a senha', false, 3),
    ('00000000-0000-0000-0005-000000000104', '00000000-0000-0000-0004-000000000101', 'Criar uma empresa', false, 4),
    ('00000000-0000-0000-0005-000000000105', '00000000-0000-0000-0004-000000000102', 'Na caixa de entrada', true, 1),
    ('00000000-0000-0000-0005-000000000106', '00000000-0000-0000-0004-000000000102', 'No ranking', false, 2),
    ('00000000-0000-0000-0005-000000000107', '00000000-0000-0000-0004-000000000102', 'No cadastro de empresas', false, 3),
    ('00000000-0000-0000-0005-000000000108', '00000000-0000-0000-0004-000000000102', 'No historico de sessoes', false, 4),
    ('00000000-0000-0000-0005-000000000109', '00000000-0000-0000-0004-000000000103', 'Ler o historico', true, 1),
    ('00000000-0000-0000-0005-000000000110', '00000000-0000-0000-0004-000000000103', 'Apagar as mensagens', false, 2),
    ('00000000-0000-0000-0005-000000000111', '00000000-0000-0000-0004-000000000103', 'Encerrar a conta', false, 3),
    ('00000000-0000-0000-0005-000000000112', '00000000-0000-0000-0004-000000000103', 'Criar uma tag sem ler', false, 4),
    ('00000000-0000-0000-0005-000000000113', '00000000-0000-0000-0004-000000000104', 'Quando a demanda foi resolvida', true, 1),
    ('00000000-0000-0000-0005-000000000114', '00000000-0000-0000-0004-000000000104', 'Depois da primeira mensagem', false, 2),
    ('00000000-0000-0000-0005-000000000115', '00000000-0000-0000-0004-000000000104', 'Automaticamente em cinco minutos', false, 3),
    ('00000000-0000-0000-0005-000000000116', '00000000-0000-0000-0004-000000000104', 'Quando o app for fechado', false, 4)
    ,('00000000-0000-0000-0005-000000000201', '00000000-0000-0000-0004-000000000201', 'Atender primeiro o caso mais urgente', true, 1)
    ,('00000000-0000-0000-0005-000000000202', '00000000-0000-0000-0004-000000000201', 'Escolher sempre o mais antigo', false, 2)
    ,('00000000-0000-0000-0005-000000000203', '00000000-0000-0000-0004-000000000201', 'Responder sem ler o contexto', false, 3)
    ,('00000000-0000-0000-0005-000000000204', '00000000-0000-0000-0004-000000000201', 'Ignorar o prazo', false, 4)
    ,('00000000-0000-0000-0005-000000000205', '00000000-0000-0000-0004-000000000202', 'Objetividade e contexto', true, 1)
    ,('00000000-0000-0000-0005-000000000206', '00000000-0000-0000-0004-000000000202', 'Texto longo sem saudacao', false, 2)
    ,('00000000-0000-0000-0005-000000000207', '00000000-0000-0000-0004-000000000202', 'Resposta automatica sempre', false, 3)
    ,('00000000-0000-0000-0005-000000000208', '00000000-0000-0000-0004-000000000202', 'Usar apenas abreviacoes', false, 4)
    ,('00000000-0000-0000-0005-000000000209', '00000000-0000-0000-0004-000000000203', 'Confirmar que os conceitos foram assimilados', true, 1)
    ,('00000000-0000-0000-0005-000000000210', '00000000-0000-0000-0004-000000000203', 'Pular todas as atividades', false, 2)
    ,('00000000-0000-0000-0005-000000000211', '00000000-0000-0000-0004-000000000203', 'Trocar o usuario', false, 3)
    ,('00000000-0000-0000-0005-000000000212', '00000000-0000-0000-0004-000000000203', 'Apagar o progresso', false, 4)
on conflict (id) do update set
    pergunta_id = excluded.pergunta_id,
    texto = excluded.texto,
    correta = excluded.correta,
    ordem = excluded.ordem;
