
ALTER TABLE produtos
    DROP CONSTRAINT IF EXISTS produtos_tipo_produto_check;

ALTER TABLE produtos
    ADD CONSTRAINT produtos_tipo_produto_check
        CHECK (tipo_produto IN ('CORTE', 'INDUSTRIALIZADO', 'INSUMO', 'SUBPRODUTO', 'BEBIDA'));
