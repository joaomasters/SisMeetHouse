-- V7: Adiciona 'BEBIDA' ao CHECK constraint de tipo_produto na tabela produtos.
-- PostgreSQL não suporta ALTER de CHECK inline — precisa dropar e recriar.

ALTER TABLE produtos
    DROP CONSTRAINT IF EXISTS produtos_tipo_produto_check;

ALTER TABLE produtos
    ADD CONSTRAINT produtos_tipo_produto_check
        CHECK (tipo_produto IN ('CORTE', 'INDUSTRIALIZADO', 'INSUMO', 'SUBPRODUTO', 'BEBIDA'));
