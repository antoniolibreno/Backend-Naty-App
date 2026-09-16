-- repeatable: roda de novo a cada mudanca de checksum, por isso o delete antes do insert
delete from usuario where empresa_id = '00000000-0000-0000-1000-000000000001';

delete from empresa where id = '00000000-0000-0000-1000-000000000001';

insert into empresa (id, nome, ativa) values
  ('00000000-0000-0000-1000-000000000001', 'Empresa Exemplo', true);

-- hash bcrypt da senha 'desenvolvimento', a mesma para as tres contas de exemplo
insert into usuario (id, empresa_id, nome, email, perfil, status, papel, ativo, senha_hash) values
  ('00000000-0000-0000-1001-000000000001', '00000000-0000-0000-1000-000000000001',
   'Ana Souza', 'ana@empresaexemplo.com.br', 'admin', 'offline', 'ADMIN', true,
   '{bcrypt}$2a$10$mVIwlrsJeIkdFP3vAa1Ts.FiEZzqDIwRsgqw1fnXd2nND2z2OohBa'),
  ('00000000-0000-0000-1001-000000000002', '00000000-0000-0000-1000-000000000001',
   'Bruno Lima', 'bruno@empresaexemplo.com.br', 'supervisor', 'offline', 'INTEGRANTE', true,
   '{bcrypt}$2a$10$mVIwlrsJeIkdFP3vAa1Ts.FiEZzqDIwRsgqw1fnXd2nND2z2OohBa'),
  ('00000000-0000-0000-1001-000000000003', '00000000-0000-0000-1000-000000000001',
   'Carla Dias', 'carla@empresaexemplo.com.br', 'user', 'offline', 'INTEGRANTE', true,
   '{bcrypt}$2a$10$mVIwlrsJeIkdFP3vAa1Ts.FiEZzqDIwRsgqw1fnXd2nND2z2OohBa');
