-- Novo módulo CLIENTES na matriz de permissões.
-- SUPER_ADMIN e ADMIN ganham acesso total; GESTOR ganha ver/criar/editar
-- (sem excluir); CAIXA ganha ver/criar (precisa cadastrar cliente rápido
-- ao abrir uma comanda, mas não edita dados de crédito); demais perfis
-- (incluindo os criados manualmente pela tela) recebem a linha negada,
-- para manter a matriz sempre completa.


INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
SELECT id, 'CLIENTES', TRUE, TRUE, TRUE, TRUE FROM perfis WHERE nome IN ('SUPER_ADMIN', 'ADMIN')
  AND NOT EXISTS (SELECT 1 FROM perfil_permissoes pp WHERE pp.perfil_id = perfis.id AND pp.modulo = 'CLIENTES');

INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
SELECT id, 'CLIENTES', TRUE, TRUE, TRUE, FALSE FROM perfis WHERE nome = 'GESTOR'
  AND NOT EXISTS (SELECT 1 FROM perfil_permissoes pp WHERE pp.perfil_id = perfis.id AND pp.modulo = 'CLIENTES');

INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
SELECT id, 'CLIENTES', TRUE, TRUE, FALSE, FALSE FROM perfis WHERE nome = 'CAIXA'
  AND NOT EXISTS (SELECT 1 FROM perfil_permissoes pp WHERE pp.perfil_id = perfis.id AND pp.modulo = 'CLIENTES');

-- Qualquer outro perfil (ex: criado manualmente pela tela) recebe a linha negada por padrão.
INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
SELECT id, 'CLIENTES', FALSE, FALSE, FALSE, FALSE FROM perfis
WHERE nome NOT IN ('SUPER_ADMIN', 'ADMIN', 'GESTOR', 'CAIXA')
  AND NOT EXISTS (SELECT 1 FROM perfil_permissoes pp WHERE pp.perfil_id = perfis.id AND pp.modulo = 'CLIENTES');