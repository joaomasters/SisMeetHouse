

ALTER TABLE recebimento_item    DROP CONSTRAINT IF EXISTS recebimento_item_recebimento_id_fkey;
ALTER TABLE processo_desossa    DROP CONSTRAINT IF EXISTS processo_desossa_recebimento_id_fkey;

ALTER TABLE recebimento_mercadoria ALTER COLUMN id TYPE BIGINT;

ALTER TABLE recebimento_item ALTER COLUMN id             TYPE BIGINT;
ALTER TABLE recebimento_item ALTER COLUMN recebimento_id TYPE BIGINT;
ALTER TABLE recebimento_item ALTER COLUMN produto_id     TYPE BIGINT;

ALTER TABLE recebimento_item ADD CONSTRAINT recebimento_item_recebimento_id_fkey
    FOREIGN KEY (recebimento_id) REFERENCES recebimento_mercadoria(id) ON DELETE CASCADE;

ALTER TABLE processo_desossa ALTER COLUMN recebimento_id TYPE BIGINT;
ALTER TABLE processo_desossa ADD CONSTRAINT processo_desossa_recebimento_id_fkey
    FOREIGN KEY (recebimento_id) REFERENCES recebimento_mercadoria(id);

ALTER TABLE nota_fiscal_saida_item DROP CONSTRAINT IF EXISTS nota_fiscal_saida_item_nota_id_fkey;

ALTER TABLE nota_fiscal_saida ALTER COLUMN id         TYPE BIGINT;
ALTER TABLE nota_fiscal_saida ALTER COLUMN cliente_id TYPE BIGINT;

ALTER TABLE nota_fiscal_saida_item ALTER COLUMN id         TYPE BIGINT;
ALTER TABLE nota_fiscal_saida_item ALTER COLUMN nota_id    TYPE BIGINT;
ALTER TABLE nota_fiscal_saida_item ALTER COLUMN produto_id TYPE BIGINT;

ALTER TABLE nota_fiscal_saida_item ADD CONSTRAINT nota_fiscal_saida_item_nota_id_fkey
    FOREIGN KEY (nota_id) REFERENCES nota_fiscal_saida(id) ON DELETE CASCADE;
