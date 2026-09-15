-- Amplia a lista de tipos de movimentação permitidos, para suportar
-- a baixa/estorno de estoque causada pela emissão/cancelamento de
-- NF de Saída (módulo Fiscal).
ALTER TABLE movimentacao_estoque DROP CONSTRAINT movimentacao_estoque_tipo_movimentacao_check;

ALTER TABLE movimentacao_estoque ADD CONSTRAINT movimentacao_estoque_tipo_movimentacao_check
    CHECK (tipo_movimentacao IN (
        'ENTRADA_COMPRA','ENTRADA_DESOSSA','SAIDA_VENDA',
        'SAIDA_DESOSSA','SAIDA_DESCARTE','SAIDA_QUEBRA',
        'SAIDA_MOAGEM','AJUSTE_POSITIVO','AJUSTE_NEGATIVO',
        'SAIDA_NF','ENTRADA_ESTORNO_NF'
    ));