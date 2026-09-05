ALTER TABLE produtos DROP CONSRAINT IF EXISTS produtos_tipo_produto_check;

ALTER TABLE produtos ADD CONTRAINT produtos_tipo_produto_check CHECK (tipo_produto IN ('CORTE', 'INDUSTRIALIZADO', 'INSUMO', 'SUBPRODUTO', 'BEBIDA', 'EMBALAGEM', 'SERVIÇO', 'OUTROS')); 