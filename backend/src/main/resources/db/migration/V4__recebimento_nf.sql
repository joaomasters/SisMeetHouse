-- Módulo de Recebimento de Mercadoria (entrada NF)
CREATE TABLE recebimento_mercadoria (
    id               SERIAL PRIMARY KEY,
    numero_nf        VARCHAR(50),
    serie_nf         VARCHAR(5)    DEFAULT '1',
    chave_nf         VARCHAR(44),
    fornecedor       VARCHAR(200)  NOT NULL,
    xml_nf           TEXT,
    data_emissao     DATE,
    data_recebimento TIMESTAMP     DEFAULT NOW(),
    valor_total      NUMERIC(12,2),
    status           VARCHAR(20)   DEFAULT 'CONFERIDO',
    observacao       TEXT,
    created_at       TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE recebimento_item (
    id             SERIAL PRIMARY KEY,
    recebimento_id INT           NOT NULL REFERENCES recebimento_mercadoria(id) ON DELETE CASCADE,
    produto_id     INT           NOT NULL REFERENCES produtos(id),
    quantidade     NUMERIC(12,4) NOT NULL,
    custo_unitario NUMERIC(12,4),
    custo_total    NUMERIC(12,4),
    created_at     TIMESTAMP     DEFAULT NOW()
);

-- Casamento NF x Desossa
ALTER TABLE processo_desossa
    ADD COLUMN IF NOT EXISTS recebimento_id INT REFERENCES recebimento_mercadoria(id);

-- Módulo de Nota Fiscal de Saída (emissão)
CREATE TABLE nota_fiscal_saida (
    id                SERIAL PRIMARY KEY,
    numero_nf         VARCHAR(50),
    serie_nf          VARCHAR(5)    DEFAULT '1',
    chave_nf          VARCHAR(44),
    cliente_id        INT REFERENCES clientes(id),
    natureza_operacao VARCHAR(200)  DEFAULT 'VENDA DE MERCADORIAS',
    data_emissao      TIMESTAMP     DEFAULT NOW(),
    valor_produtos    NUMERIC(12,2) DEFAULT 0,
    valor_desconto    NUMERIC(12,2) DEFAULT 0,
    valor_total       NUMERIC(12,2) DEFAULT 0,
    status            VARCHAR(20)   DEFAULT 'PENDENTE',
    xml_nf            TEXT,
    observacao        TEXT,
    created_at        TIMESTAMP     DEFAULT NOW()
);

CREATE TABLE nota_fiscal_saida_item (
    id             SERIAL PRIMARY KEY,
    nota_id        INT           NOT NULL REFERENCES nota_fiscal_saida(id) ON DELETE CASCADE,
    produto_id     INT REFERENCES produtos(id),
    descricao      VARCHAR(200),
    quantidade     NUMERIC(12,4) NOT NULL,
    valor_unitario NUMERIC(12,4) NOT NULL,
    valor_total    NUMERIC(12,4) NOT NULL
);
