-- repeatable: roda de novo a cada mudanca de checksum, por isso o delete antes do insert
delete from usuario where empresa_id in (
  '00000000-0000-0000-1000-000000000001', '00000000-0000-0000-1000-000000000002');

delete from empresa where id in (
  '00000000-0000-0000-1000-000000000001', '00000000-0000-0000-1000-000000000002');

insert into empresa (id, nome, ativa, fuso_horario) values
  ('00000000-0000-0000-1000-000000000001', 'Empresa Exemplo', true, 'America/Sao_Paulo'),
  ('00000000-0000-0000-1000-000000000002', 'Naty', true, 'America/Sao_Paulo');

-- hash bcrypt da senha '123qweasd', a mesma para as tres contas de exemplo
insert into usuario (id, empresa_id, nome, email, perfil, status, papel, ativo, senha_hash) values
  ('00000000-0000-0000-1001-000000000001', '00000000-0000-0000-1000-000000000001',
   'Administrador', 'admin@admin.com', 'admin', 'offline', 'ADMIN', true,
   '{bcrypt}$2a$10$UEcBm/s9mBcpaHu9u.w.FunMf78e0MCSZOQZ1Kja7S4wJH37FMxQa'),
  ('00000000-0000-0000-1001-000000000002', '00000000-0000-0000-1000-000000000001',
   'Integrante', 'user@user.com', 'user', 'offline', 'INTEGRANTE', true,
   '{bcrypt}$2a$10$UEcBm/s9mBcpaHu9u.w.FunMf78e0MCSZOQZ1Kja7S4wJH37FMxQa'),
  ('00000000-0000-0000-1001-000000000003', '00000000-0000-0000-1000-000000000002',
   'Equipe Naty', 'naty@naty.com', 'admin', 'offline', 'NATY', true,
   '{bcrypt}$2a$10$UEcBm/s9mBcpaHu9u.w.FunMf78e0MCSZOQZ1Kja7S4wJH37FMxQa');
