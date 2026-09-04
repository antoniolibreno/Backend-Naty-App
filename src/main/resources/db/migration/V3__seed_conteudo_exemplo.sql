insert into trilha (id, titulo, descricao, ordem, ativa) values
  ('00000000-0000-0000-0000-000000000001', 'Conhecendo a Naty (conteudo de exemplo)', 'Trilha de exemplo, substituida pelo conteudo real em migration futura', 1, true);

insert into modulo (id, trilha_id, titulo, descricao, ordem) values
  ('00000000-0000-0000-0001-000000000001', '00000000-0000-0000-0000-000000000001', 'Primeiros passos', 'Fundamentos da plataforma e da conexao com o WhatsApp', 1);

insert into atividade (id, modulo_id, titulo, descricao, ordem, imagem_url, xp) values
  ('00000000-0000-0000-0002-000000000001', '00000000-0000-0000-0001-000000000001', 'O que e a Naty', 'Visao geral da plataforma e do problema que ela resolve', 1, '/imagens/atividade-01.png', 10);

insert into quiz (id, atividade_id, nota_minima) values
  ('00000000-0000-0000-0003-000000000001', '00000000-0000-0000-0002-000000000001', 70);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000001', '00000000-0000-0000-0003-000000000001', 'A Naty serve principalmente para que?', 1);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000001', '00000000-0000-0000-0004-000000000001', 'Gerenciar conversas de WhatsApp de uma empresa', true, 1),
  ('00000000-0000-0000-0005-000000000002', '00000000-0000-0000-0004-000000000001', 'Editar planilhas financeiras', false, 2),
  ('00000000-0000-0000-0005-000000000003', '00000000-0000-0000-0004-000000000001', 'Hospedar sites institucionais', false, 3),
  ('00000000-0000-0000-0005-000000000004', '00000000-0000-0000-0004-000000000001', 'Emitir notas fiscais', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000002', '00000000-0000-0000-0003-000000000001', 'Quem atende as conversas dentro da Naty?', 2);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000005', '00000000-0000-0000-0004-000000000002', 'Os integrantes da empresa, chamados de usuarios', true, 1),
  ('00000000-0000-0000-0005-000000000006', '00000000-0000-0000-0004-000000000002', 'Apenas robos, sem pessoa envolvida', false, 2),
  ('00000000-0000-0000-0005-000000000007', '00000000-0000-0000-0004-000000000002', 'A equipe de suporte da propria Naty', false, 3),
  ('00000000-0000-0000-0005-000000000008', '00000000-0000-0000-0004-000000000002', 'Os contatos que enviaram a mensagem', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000003', '00000000-0000-0000-0003-000000000001', 'Uma conversa em andamento com um contato e chamada de que?', 3);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000009', '00000000-0000-0000-0004-000000000003', 'Ticket', true, 1),
  ('00000000-0000-0000-0005-000000000010', '00000000-0000-0000-0004-000000000003', 'Planilha', false, 2),
  ('00000000-0000-0000-0005-000000000011', '00000000-0000-0000-0004-000000000003', 'Campanha', false, 3),
  ('00000000-0000-0000-0005-000000000012', '00000000-0000-0000-0004-000000000003', 'Etiqueta', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000004', '00000000-0000-0000-0003-000000000001', 'A pessoa de fora que fala com a empresa e chamada de que?', 4);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000013', '00000000-0000-0000-0004-000000000004', 'Contato', true, 1),
  ('00000000-0000-0000-0005-000000000014', '00000000-0000-0000-0004-000000000004', 'Usuario', false, 2),
  ('00000000-0000-0000-0005-000000000015', '00000000-0000-0000-0004-000000000004', 'Fila', false, 3),
  ('00000000-0000-0000-0005-000000000016', '00000000-0000-0000-0004-000000000004', 'Conexao', false, 4);

insert into atividade (id, modulo_id, titulo, descricao, ordem, imagem_url, xp) values
  ('00000000-0000-0000-0002-000000000002', '00000000-0000-0000-0001-000000000001', 'Conectando o WhatsApp', 'Como o numero da empresa passa a funcionar na plataforma', 2, '/imagens/atividade-02.png', 10);

insert into quiz (id, atividade_id, nota_minima) values
  ('00000000-0000-0000-0003-000000000002', '00000000-0000-0000-0002-000000000002', 70);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000005', '00000000-0000-0000-0003-000000000002', 'O vinculo entre a Naty e um numero de WhatsApp e chamado de que?', 1);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000017', '00000000-0000-0000-0004-000000000005', 'Conexao', true, 1),
  ('00000000-0000-0000-0005-000000000018', '00000000-0000-0000-0004-000000000005', 'Contato', false, 2),
  ('00000000-0000-0000-0005-000000000019', '00000000-0000-0000-0004-000000000005', 'Ticket', false, 3),
  ('00000000-0000-0000-0005-000000000020', '00000000-0000-0000-0004-000000000005', 'Tag', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000006', '00000000-0000-0000-0003-000000000002', 'Uma empresa pode ter mais de uma conexao ativa?', 2);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000021', '00000000-0000-0000-0004-000000000006', 'Sim, cada numero e uma conexao', true, 1),
  ('00000000-0000-0000-0005-000000000022', '00000000-0000-0000-0004-000000000006', 'Nao, o limite e sempre um numero', false, 2),
  ('00000000-0000-0000-0005-000000000023', '00000000-0000-0000-0004-000000000006', 'So se contratar outra empresa', false, 3),
  ('00000000-0000-0000-0005-000000000024', '00000000-0000-0000-0004-000000000006', 'Apenas em finais de semana', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000007', '00000000-0000-0000-0003-000000000002', 'Se a conexao cair, o que acontece com o envio de mensagem?', 3);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000025', '00000000-0000-0000-0004-000000000007', 'O envio falha ate a conexao voltar', true, 1),
  ('00000000-0000-0000-0005-000000000026', '00000000-0000-0000-0004-000000000007', 'A mensagem e enviada por e-mail', false, 2),
  ('00000000-0000-0000-0005-000000000027', '00000000-0000-0000-0004-000000000007', 'O ticket e apagado', false, 3),
  ('00000000-0000-0000-0005-000000000028', '00000000-0000-0000-0004-000000000007', 'Nada muda, o envio segue normal', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000008', '00000000-0000-0000-0003-000000000002', 'O que fazer quando uma conexao apresenta instabilidade?', 4);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000029', '00000000-0000-0000-0004-000000000008', 'Reiniciar a conexao pela plataforma', true, 1),
  ('00000000-0000-0000-0005-000000000030', '00000000-0000-0000-0004-000000000008', 'Apagar todos os contatos', false, 2),
  ('00000000-0000-0000-0005-000000000031', '00000000-0000-0000-0004-000000000008', 'Criar uma nova empresa', false, 3),
  ('00000000-0000-0000-0005-000000000032', '00000000-0000-0000-0004-000000000008', 'Desinstalar o aplicativo do celular', false, 4);

insert into atividade (id, modulo_id, titulo, descricao, ordem, imagem_url, xp) values
  ('00000000-0000-0000-0002-000000000003', '00000000-0000-0000-0001-000000000001', 'Conhecendo a caixa de entrada', 'Onde as conversas chegam e como elas sao organizadas', 3, '/imagens/atividade-03.png', 10);

insert into quiz (id, atividade_id, nota_minima) values
  ('00000000-0000-0000-0003-000000000003', '00000000-0000-0000-0002-000000000003', 70);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000009', '00000000-0000-0000-0003-000000000003', 'O que a caixa de entrada mostra?', 1);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000033', '00000000-0000-0000-0004-000000000009', 'Os tickets que precisam de atendimento', true, 1),
  ('00000000-0000-0000-0005-000000000034', '00000000-0000-0000-0004-000000000009', 'O faturamento do mes', false, 2),
  ('00000000-0000-0000-0005-000000000035', '00000000-0000-0000-0004-000000000009', 'A lista de funcionarios demitidos', false, 3),
  ('00000000-0000-0000-0005-000000000036', '00000000-0000-0000-0004-000000000009', 'O historico de login da plataforma', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000010', '00000000-0000-0000-0003-000000000003', 'Um ticket guarda o que, alem do contato?', 2);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000037', '00000000-0000-0000-0004-000000000010', 'As mensagens trocadas na conversa', true, 1),
  ('00000000-0000-0000-0005-000000000038', '00000000-0000-0000-0004-000000000010', 'O extrato bancario do cliente', false, 2),
  ('00000000-0000-0000-0005-000000000039', '00000000-0000-0000-0004-000000000010', 'As fotos do perfil da empresa', false, 3),
  ('00000000-0000-0000-0005-000000000040', '00000000-0000-0000-0004-000000000010', 'O codigo fonte da plataforma', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000011', '00000000-0000-0000-0003-000000000003', 'Por que um ticket muda de status ao longo do atendimento?', 3);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000041', '00000000-0000-0000-0004-000000000011', 'Para indicar em que ponto do atendimento ele esta', true, 1),
  ('00000000-0000-0000-0005-000000000042', '00000000-0000-0000-0004-000000000011', 'Para apagar as mensagens antigas', false, 2),
  ('00000000-0000-0000-0005-000000000043', '00000000-0000-0000-0004-000000000011', 'Para trocar o numero do contato', false, 3),
  ('00000000-0000-0000-0005-000000000044', '00000000-0000-0000-0004-000000000011', 'Para encerrar a assinatura', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000012', '00000000-0000-0000-0003-000000000003', 'Dois integrantes podem atender o mesmo ticket ao mesmo tempo?', 4);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000045', '00000000-0000-0000-0004-000000000012', 'Nao, o ticket fica com um responsavel por vez', true, 1),
  ('00000000-0000-0000-0005-000000000046', '00000000-0000-0000-0004-000000000012', 'Sim, sempre e sem restricao', false, 2),
  ('00000000-0000-0000-0005-000000000047', '00000000-0000-0000-0004-000000000012', 'Somente se forem da mesma familia', false, 3),
  ('00000000-0000-0000-0005-000000000048', '00000000-0000-0000-0004-000000000012', 'Apenas se o contato autorizar por escrito', false, 4);

insert into modulo (id, trilha_id, titulo, descricao, ordem) values
  ('00000000-0000-0000-0001-000000000002', '00000000-0000-0000-0000-000000000001', 'Atendimento no dia a dia', 'Rotina de quem usa a plataforma para atender', 2);

insert into atividade (id, modulo_id, titulo, descricao, ordem, imagem_url, xp) values
  ('00000000-0000-0000-0002-000000000004', '00000000-0000-0000-0001-000000000002', 'Filas e distribuicao de atendimento', 'Como o trabalho e repartido entre a equipe', 1, '/imagens/atividade-04.png', 10);

insert into quiz (id, atividade_id, nota_minima) values
  ('00000000-0000-0000-0003-000000000004', '00000000-0000-0000-0002-000000000004', 70);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000013', '00000000-0000-0000-0003-000000000004', 'Para que serve uma fila?', 1);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000049', '00000000-0000-0000-0004-000000000013', 'Agrupar atendimentos por assunto ou equipe', true, 1),
  ('00000000-0000-0000-0005-000000000050', '00000000-0000-0000-0004-000000000013', 'Guardar mensagens apagadas', false, 2),
  ('00000000-0000-0000-0005-000000000051', '00000000-0000-0000-0004-000000000013', 'Controlar o pagamento da assinatura', false, 3),
  ('00000000-0000-0000-0005-000000000052', '00000000-0000-0000-0004-000000000013', 'Traduzir mensagens automaticamente', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000014', '00000000-0000-0000-0003-000000000004', 'Um integrante pode participar de mais de uma fila?', 2);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000053', '00000000-0000-0000-0004-000000000014', 'Sim', true, 1),
  ('00000000-0000-0000-0005-000000000054', '00000000-0000-0000-0004-000000000014', 'Nao, nunca', false, 2),
  ('00000000-0000-0000-0005-000000000055', '00000000-0000-0000-0004-000000000014', 'Apenas o administrador', false, 3),
  ('00000000-0000-0000-0005-000000000056', '00000000-0000-0000-0004-000000000014', 'Somente aos domingos', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000015', '00000000-0000-0000-0003-000000000004', 'Qual o efeito de dividir o atendimento em filas?', 3);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000057', '00000000-0000-0000-0004-000000000015', 'Cada assunto chega a quem sabe resolver', true, 1),
  ('00000000-0000-0000-0005-000000000058', '00000000-0000-0000-0004-000000000015', 'As conversas somem da caixa de entrada', false, 2),
  ('00000000-0000-0000-0005-000000000059', '00000000-0000-0000-0004-000000000015', 'O contato deixa de receber resposta', false, 3),
  ('00000000-0000-0000-0005-000000000060', '00000000-0000-0000-0004-000000000015', 'A conexao com o WhatsApp e encerrada', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000016', '00000000-0000-0000-0003-000000000004', 'O que acontece com um ticket que nao tem fila definida?', 4);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000061', '00000000-0000-0000-0004-000000000016', 'Ele fica sem direcionamento e precisa ser encaminhado', true, 1),
  ('00000000-0000-0000-0005-000000000062', '00000000-0000-0000-0004-000000000016', 'Ele e apagado em cinco minutos', false, 2),
  ('00000000-0000-0000-0005-000000000063', '00000000-0000-0000-0004-000000000016', 'Ele vira uma campanha', false, 3),
  ('00000000-0000-0000-0005-000000000064', '00000000-0000-0000-0004-000000000016', 'Ele muda de empresa', false, 4);

insert into atividade (id, modulo_id, titulo, descricao, ordem, imagem_url, xp) values
  ('00000000-0000-0000-0002-000000000005', '00000000-0000-0000-0001-000000000002', 'Respondendo um ticket', 'Boas praticas ao conversar com o contato', 2, '/imagens/atividade-05.png', 10);

insert into quiz (id, atividade_id, nota_minima) values
  ('00000000-0000-0000-0003-000000000005', '00000000-0000-0000-0002-000000000005', 70);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000017', '00000000-0000-0000-0003-000000000005', 'Antes de responder, o que ajuda mais o atendimento?', 1);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000065', '00000000-0000-0000-0004-000000000017', 'Ler o historico da conversa', true, 1),
  ('00000000-0000-0000-0005-000000000066', '00000000-0000-0000-0004-000000000017', 'Apagar as mensagens anteriores', false, 2),
  ('00000000-0000-0000-0005-000000000067', '00000000-0000-0000-0004-000000000017', 'Trocar o numero do contato', false, 3),
  ('00000000-0000-0000-0005-000000000068', '00000000-0000-0000-0004-000000000017', 'Encerrar a conexao', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000018', '00000000-0000-0000-0003-000000000005', 'Por que evitar prometer prazo que nao se pode cumprir?', 2);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000069', '00000000-0000-0000-0004-000000000018', 'Porque quebra a confianca do contato', true, 1),
  ('00000000-0000-0000-0005-000000000070', '00000000-0000-0000-0004-000000000018', 'Porque a plataforma bloqueia a conta', false, 2),
  ('00000000-0000-0000-0005-000000000071', '00000000-0000-0000-0004-000000000018', 'Porque o WhatsApp cobra multa', false, 3),
  ('00000000-0000-0000-0005-000000000072', '00000000-0000-0000-0004-000000000018', 'Porque apaga o ticket', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000019', '00000000-0000-0000-0003-000000000005', 'Quando um atendimento deve ser encerrado?', 3);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000073', '00000000-0000-0000-0004-000000000019', 'Quando a demanda do contato foi resolvida', true, 1),
  ('00000000-0000-0000-0005-000000000074', '00000000-0000-0000-0004-000000000019', 'Assim que o contato manda a primeira mensagem', false, 2),
  ('00000000-0000-0000-0005-000000000075', '00000000-0000-0000-0004-000000000019', 'Todo dia as sete da manha', false, 3),
  ('00000000-0000-0000-0005-000000000076', '00000000-0000-0000-0004-000000000019', 'Somente no fim do mes', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000020', '00000000-0000-0000-0003-000000000005', 'Se o assunto pertence a outra equipe, o que fazer?', 4);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000077', '00000000-0000-0000-0004-000000000020', 'Encaminhar o ticket para a fila certa', true, 1),
  ('00000000-0000-0000-0005-000000000078', '00000000-0000-0000-0004-000000000020', 'Ignorar a mensagem', false, 2),
  ('00000000-0000-0000-0005-000000000079', '00000000-0000-0000-0004-000000000020', 'Apagar o contato', false, 3),
  ('00000000-0000-0000-0005-000000000080', '00000000-0000-0000-0004-000000000020', 'Pedir para o contato ligar em outro numero', false, 4);

insert into atividade (id, modulo_id, titulo, descricao, ordem, imagem_url, xp) values
  ('00000000-0000-0000-0002-000000000006', '00000000-0000-0000-0001-000000000002', 'Tags e organizacao', 'Como classificar e reencontrar conversas', 3, '/imagens/atividade-06.png', 10);

insert into quiz (id, atividade_id, nota_minima) values
  ('00000000-0000-0000-0003-000000000006', '00000000-0000-0000-0002-000000000006', 70);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000021', '00000000-0000-0000-0003-000000000006', 'Para que serve uma tag?', 1);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000081', '00000000-0000-0000-0004-000000000021', 'Classificar contatos e tickets por caracteristica', true, 1),
  ('00000000-0000-0000-0005-000000000082', '00000000-0000-0000-0004-000000000021', 'Aumentar o limite de mensagens', false, 2),
  ('00000000-0000-0000-0005-000000000083', '00000000-0000-0000-0004-000000000021', 'Trocar a senha do usuario', false, 3),
  ('00000000-0000-0000-0005-000000000084', '00000000-0000-0000-0004-000000000021', 'Reiniciar a conexao', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000022', '00000000-0000-0000-0003-000000000006', 'Um contato pode ter mais de uma tag?', 2);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000085', '00000000-0000-0000-0004-000000000022', 'Sim', true, 1),
  ('00000000-0000-0000-0005-000000000086', '00000000-0000-0000-0004-000000000022', 'Nao, apenas uma', false, 2),
  ('00000000-0000-0000-0005-000000000087', '00000000-0000-0000-0004-000000000022', 'Somente se for cliente antigo', false, 3),
  ('00000000-0000-0000-0005-000000000088', '00000000-0000-0000-0004-000000000022', 'Apenas em campanhas', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000023', '00000000-0000-0000-0003-000000000006', 'Qual o beneficio pratico de usar tag com criterio?', 3);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000089', '00000000-0000-0000-0004-000000000023', 'Encontrar depois o grupo certo de contatos', true, 1),
  ('00000000-0000-0000-0005-000000000090', '00000000-0000-0000-0004-000000000023', 'Deixar a plataforma mais rapida', false, 2),
  ('00000000-0000-0000-0005-000000000091', '00000000-0000-0000-0004-000000000023', 'Reduzir o valor da assinatura', false, 3),
  ('00000000-0000-0000-0005-000000000092', '00000000-0000-0000-0004-000000000023', 'Aumentar o numero de conexoes', false, 4);

insert into pergunta (id, quiz_id, enunciado, ordem) values
  ('00000000-0000-0000-0004-000000000024', '00000000-0000-0000-0003-000000000006', 'O que acontece quando cada pessoa cria tag do seu jeito?', 4);
insert into alternativa (id, pergunta_id, texto, correta, ordem) values
  ('00000000-0000-0000-0005-000000000093', '00000000-0000-0000-0004-000000000024', 'A classificacao perde valor e vira bagunca', true, 1),
  ('00000000-0000-0000-0005-000000000094', '00000000-0000-0000-0004-000000000024', 'A plataforma unifica tudo sozinha', false, 2),
  ('00000000-0000-0000-0005-000000000095', '00000000-0000-0000-0004-000000000024', 'Os tickets sao encerrados', false, 3),
  ('00000000-0000-0000-0005-000000000096', '00000000-0000-0000-0004-000000000024', 'As mensagens sao reenviadas', false, 4);
