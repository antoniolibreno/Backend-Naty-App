-- repeatable: roda de novo a cada mudanca de checksum, por isso o delete antes do insert
delete from usuario where empresa_id = '00000000-0000-0000-1000-000000000001';

delete from empresa where id = '00000000-0000-0000-1000-000000000001';

insert into empresa (id, nome, naty_api_token, ativa) values
  ('00000000-0000-0000-1000-000000000001', 'Empresa Exemplo', null, true);

insert into usuario (id, empresa_id, naty_id, nome, email, perfil, status, payload) values
  ('00000000-0000-0000-1001-000000000001', '00000000-0000-0000-1000-000000000001',
   'naty-exemplo-001', 'Ana Souza', 'ana@empresaexemplo.com.br', 'admin', 'offline', '{}'::jsonb),
  ('00000000-0000-0000-1001-000000000002', '00000000-0000-0000-1000-000000000001',
   'naty-exemplo-002', 'Bruno Lima', 'bruno@empresaexemplo.com.br', 'supervisor', 'offline', '{}'::jsonb),
  ('00000000-0000-0000-1001-000000000003', '00000000-0000-0000-1000-000000000001',
   'naty-exemplo-003', 'Carla Dias', 'carla@empresaexemplo.com.br', 'user', 'offline', '{}'::jsonb);
