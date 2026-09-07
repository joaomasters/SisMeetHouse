-- Corrige PKs e FKs das tabelas do V4: SERIAL (INTEGER) -> BIGINT para casar com JPA Long

-- ──────────────────────────────────────────────
-- 1. recebimento_mercadoria
-- ──────────────────────────────────────────────

-- Remover FKs que apontam para recebimento_mercadoria.id antes de alterar o tipo
ALTER TABLE recebimento_item    DROP CONSTRAINT IF EXISTS recebimento_item_recebimento_id_fkey;
ALTER TABLE processo_desossa    DROP CONSTRAINT IF EXISTS processo_desossa_recebimento_id_fkey;

-- Alterar PK
ALTER TABLE recebimento_mercadoria ALTER COLUMN id TYPE BIGINT;

-- Alterar FKs filhos e recriar constraints
ALTER TABLE recebimento_item ALTER COLUMN id             TYPE BIGINT;
ALTER TABLE recebimento_item ALTER COLUMN recebimento_id TYPE BIGINT;
ALTER TABLE recebimento_item ALTER COLUMN produto_id     TYPE BIGINT;

ALTER TABLE recebimento_item ADD CONSTRAINT recebimento_item_recebimento_id_fkey
    FOREIGN KEY (recebimento_id) REFERENCES recebimento_mercadoria(id) ON DELETE CASCADE;

ALTER TABLE processo_desossa ALTER COLUMN recebimento_id TYPE BIGINT;
ALTER TABLE processo_desossa ADD CONSTRAINT processo_desossa_recebimento_id_fkey
    FOREIGN KEY (recebimento_id) REFERENCES recebimento_mercadoria(id);

-- ──────────────────────────────────────────────
-- 2. nota_fiscal_saida
-- ──────────────────────────────────────────────

-- Remover FK filha antes de alterar o tipo da PK
ALTER TABLE nota_fiscal_saida_item DROP CONSTRAINT IF EXISTS nota_fiscal_saida_item_nota_id_fkey;

-- Alterar PK
ALTER TABLE nota_fiscal_saida ALTER COLUMN id         TYPE BIGINT;
ALTER TABLE nota_fiscal_saida ALTER COLUMN cliente_id TYPE BIGINT;

-- Alterar FKs filhos e recriar constraints
ALTER TABLE nota_fiscal_saida_item ALTER COLUMN id         TYPE BIGINT;
ALTER TABLE nota_fiscal_saida_item ALTER COLUMN nota_id    TYPE BIGINT;
ALTER TABLE nota_fiscal_saida_item ALTER COLUMN produto_id TYPE BIGINT;

ALTER TABLE nota_fiscal_saida_item ADD CONSTRAINT nota_fiscal_saida_item_nota_id_fkey
    FOREIGN KEY (nota_id) REFERENCES nota_fiscal_saida(id) ON DELETE CASCADE;
