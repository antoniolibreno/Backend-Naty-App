-- repeatable: roda de novo a cada mudanca de checksum, por isso upsert pelos ids fixos
insert into empresa (id, nome, ativa, fuso_horario, cnpj, razao_social) values
  ('00000000-0000-0000-1000-000000000001', 'Empresa Exemplo', true, 'America/Sao_Paulo',
   '11222333000181', 'Empresa Exemplo Ltda'),
  ('00000000-0000-0000-1000-000000000002', 'Naty', true, 'America/Sao_Paulo',
   '48055059000103', 'Naty App Oficial LTDA')
on conflict (id) do update set
  nome = excluded.nome, ativa = excluded.ativa, fuso_horario = excluded.fuso_horario,
  cnpj = excluded.cnpj, razao_social = excluded.razao_social;

-- hash bcrypt da senha '123qweasd', a mesma para as tres contas de exemplo
insert into usuario (id, empresa_id, nome, email, perfil, status, papel, ativo, telefone, senha_hash) values
  ('00000000-0000-0000-1001-000000000001', '00000000-0000-0000-1000-000000000001',
   'Administrador', 'admin@admin.com', 'admin', 'offline', 'ADMIN', true, '11987654321',
   '{bcrypt}$2a$10$UEcBm/s9mBcpaHu9u.w.FunMf78e0MCSZOQZ1Kja7S4wJH37FMxQa'),
  ('00000000-0000-0000-1001-000000000002', '00000000-0000-0000-1000-000000000001',
   'Integrante', 'user@user.com', 'user', 'offline', 'INTEGRANTE', true, null,
   '{bcrypt}$2a$10$UEcBm/s9mBcpaHu9u.w.FunMf78e0MCSZOQZ1Kja7S4wJH37FMxQa'),
  ('00000000-0000-0000-1001-000000000003', '00000000-0000-0000-1000-000000000002',
   'Equipe Naty', 'naty@naty.com', 'admin', 'offline', 'NATY', true, null,
   '{bcrypt}$2a$10$UEcBm/s9mBcpaHu9u.w.FunMf78e0MCSZOQZ1Kja7S4wJH37FMxQa')
on conflict (id) do update set
  empresa_id = excluded.empresa_id, nome = excluded.nome, email = excluded.email,
  perfil = excluded.perfil, papel = excluded.papel, ativo = excluded.ativo,
  telefone = excluded.telefone, senha_hash = excluded.senha_hash;
