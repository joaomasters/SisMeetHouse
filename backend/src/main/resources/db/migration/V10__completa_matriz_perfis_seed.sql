-- Completa a matriz de permissões dos perfis seed (ADMIN, GESTOR, CAIXA)
-- com uma linha para cada um dos 17 módulos, mesmo que negada (false).
-- A V9 só inseriu as linhas relevantes para cada perfil; perfis criados
-- pela tela (PerfilService.criar) sempre nascem com as 17 linhas completas
-- — esta migration deixa os perfis seed consistentes com essa mesma regra.
INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
SELECT p.id, m.modulo, FALSE, FALSE, FALSE, FALSE
FROM perfis p
CROSS JOIN (VALUES
    ('PDV'), ('SANGRIA'), ('PRODUTOS'), ('RECEBIMENTO'), ('FICHAS_DESOSSA'),
    ('RATEIO_DESOSSA'), ('INVENTARIO'), ('PERDAS'), ('NF_SAIDA'), ('FATURAMENTO'),
    ('CONTAS_RECEBER'), ('CONTAS_PAGAR'), ('DRE'), ('RELATORIOS'), ('CARGA_BALANCA'),
    ('USUARIOS'), ('PERFIS')
) AS m(modulo)
WHERE p.nome IN ('ADMIN', 'GESTOR', 'CAIXA')
  AND NOT EXISTS (
    SELECT 1 FROM perfil_permissoes pp
    WHERE pp.perfil_id = p.id AND pp.modulo = m.modulo
  );