-- =====================================================================
-- V2 — Módulos: Sangria/Suprimento, Perdas, Inventário, Contas a Pagar
-- =====================================================================

-- ─────────────────────────────────────────────────────────────────────
-- SANGRIA E SUPRIMENTO DE CAIXA
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE sangria_caixa (
    id          BIGSERIAL    PRIMARY KEY,
    caixa_id    BIGINT       NOT NULL REFERENCES caixa(id),
    tipo        VARCHAR(20)  NOT NULL CHECK (tipo IN ('SANGRIA','SUPRIMENTO')),
    valor       NUMERIC(12,2) NOT NULL,
    motivo      VARCHAR(255),
    operador_id BIGINT,
    created_at  TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_sangria_caixa ON sangria_caixa(caixa_id);

-- ─────────────────────────────────────────────────────────────────────
-- PERDAS DE ESTOQUE
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE perdas_estoque (
    id              BIGSERIAL    PRIMARY KEY,
    produto_id      BIGINT       NOT NULL REFERENCES produtos(id),
    quantidade      NUMERIC(12,4) NOT NULL,
    custo_unitario  NUMERIC(12,4),
    custo_total     NUMERIC(12,2),
    motivo          VARCHAR(50)  NOT NULL
                    CHECK (motivo IN ('VENCIMENTO','AVARIA','FURTO','DESOSSA','OUTROS')),
    observacao      TEXT,
    usuario_id      BIGINT,
    created_at      TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_perdas_produto_data ON perdas_estoque(produto_id, created_at DESC);
CREATE INDEX idx_perdas_data         ON perdas_estoque(created_at DESC);

-- ─────────────────────────────────────────────────────────────────────
-- INVENTÁRIO FÍSICO
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE inventario_fisico (
    id          BIGSERIAL   PRIMARY KEY,
    status      VARCHAR(20) NOT NULL DEFAULT 'ABERTO'
                CHECK (status IN ('ABERTO','FINALIZADO','CANCELADO')),
    observacao  TEXT,
    usuario_id  BIGINT,
    data_inicio TIMESTAMP   DEFAULT NOW(),
    data_fim    TIMESTAMP,
    created_at  TIMESTAMP   DEFAULT NOW()
);

CREATE TABLE inventario_fisico_item (
    id              BIGSERIAL    PRIMARY KEY,
    inventario_id   BIGINT       NOT NULL REFERENCES inventario_fisico(id),
    produto_id      BIGINT       NOT NULL REFERENCES produtos(id),
    saldo_sistema   NUMERIC(12,4) NOT NULL,
    saldo_contado   NUMERIC(12,4),
    divergencia     NUMERIC(12,4),
    created_at      TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_inventario_status ON inventario_fisico(status);
CREATE INDEX idx_inv_item_inv      ON inventario_fisico_item(inventario_id);

-- ─────────────────────────────────────────────────────────────────────
-- CONTAS A PAGAR
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE contas_pagar (
    id              BIGSERIAL    PRIMARY KEY,
    descricao       VARCHAR(255) NOT NULL,
    fornecedor      VARCHAR(255),
    valor           NUMERIC(12,2) NOT NULL,
    valor_pago      NUMERIC(12,2) DEFAULT 0,
    data_vencimento DATE         NOT NULL,
    data_pagamento  DATE,
    categoria       VARCHAR(100),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ABERTO'
                    CHECK (status IN ('ABERTO','PAGO','PARCIAL','CANCELADO')),
    observacao      TEXT,
    created_at      TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_contas_pagar_venc   ON contas_pagar(data_vencimento, status);
CREATE INDEX idx_contas_pagar_status ON contas_pagar(status);

-- ─────────────────────────────────────────────────────────────────────
-- ATUALIZAR TABELAS EXISTENTES
-- ─────────────────────────────────────────────────────────────────────
ALTER TABLE caixa ADD COLUMN IF NOT EXISTS total_sangria    NUMERIC(12,2) DEFAULT 0;
ALTER TABLE caixa ADD COLUMN IF NOT EXISTS total_suprimento NUMERIC(12,2) DEFAULT 0;

ALTER TABLE clientes ADD COLUMN IF NOT EXISTS saldo_fiado_atual NUMERIC(12,2) DEFAULT 0;
