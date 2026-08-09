-- =====================================================================
-- V1 — Schema inicial do sistema ERP/PDV para Açougues
-- Banco: PostgreSQL 15
-- =====================================================================

-- ─────────────────────────────────────────────────────────────────────
-- CATEGORIAS
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE categorias (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    descricao   TEXT,
    created_at  TIMESTAMP    DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────────────
-- PRODUTOS
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE produtos (
    id                BIGSERIAL    PRIMARY KEY,
    codigo_interno    VARCHAR(20)  UNIQUE NOT NULL,
    codigo_balanca    INTEGER,
    ean13             VARCHAR(13),
    nome              VARCHAR(150) NOT NULL,
    descricao         TEXT,
    unidade_medida    VARCHAR(5)   NOT NULL
                        CHECK (unidade_medida IN ('KG','UN','CX','G')),
    tipo_produto      VARCHAR(20)  NOT NULL
                        CHECK (tipo_produto IN ('CORTE','INDUSTRIALIZADO','INSUMO','SUBPRODUTO')),
    preco_custo       NUMERIC(12,4),
    preco_venda       NUMERIC(12,4) NOT NULL,
    estoque_atual     NUMERIC(12,4) DEFAULT 0,
    estoque_minimo    NUMERIC(12,4) DEFAULT 0,
    categoria_id      BIGINT       REFERENCES categorias(id),
    tem_ficha_desossa BOOLEAN      DEFAULT FALSE,
    ativo             BOOLEAN      DEFAULT TRUE,
    created_at        TIMESTAMP    DEFAULT NOW(),
    updated_at        TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_produto_codigo_balanca ON produtos(codigo_balanca);
CREATE INDEX idx_produto_ean13          ON produtos(ean13);

-- ─────────────────────────────────────────────────────────────────────
-- CARGA BALANÇA
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE carga_balanca (
    id             BIGSERIAL   PRIMARY KEY,
    produto_id     BIGINT      NOT NULL REFERENCES produtos(id),
    tipo_balanca   VARCHAR(20) NOT NULL
                    CHECK (tipo_balanca IN ('TOLEDO_MGV6','TOLEDO_MGV7','FILIZOLA_SMART')),
    codigo_plu     INTEGER     NOT NULL,
    preco_enviado  NUMERIC(12,4) NOT NULL,
    validade_dias  INTEGER     DEFAULT 0,
    data_envio     TIMESTAMP   DEFAULT NOW(),
    status         VARCHAR(20) DEFAULT 'PENDENTE'
                    CHECK (status IN ('PENDENTE','ENVIADO','ERRO'))
);

-- ─────────────────────────────────────────────────────────────────────
-- FICHA DE DESOSSA
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE ficha_desossa (
    id                      BIGSERIAL    PRIMARY KEY,
    produto_pai_id          BIGINT       NOT NULL REFERENCES produtos(id),
    nome                    VARCHAR(150) NOT NULL,
    descricao               TEXT,
    perda_total_percentual  NUMERIC(5,2) DEFAULT 0,
    ativo                   BOOLEAN      DEFAULT TRUE,
    created_at              TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE ficha_desossa_itens (
    id                    BIGSERIAL   PRIMARY KEY,
    ficha_desossa_id      BIGINT      NOT NULL REFERENCES ficha_desossa(id),
    produto_filho_id      BIGINT      NOT NULL REFERENCES produtos(id),
    percentual_rendimento NUMERIC(5,2) NOT NULL,
    sequencia             INTEGER     DEFAULT 0
);

-- ─────────────────────────────────────────────────────────────────────
-- MOVIMENTAÇÃO DE ESTOQUE
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE movimentacao_estoque (
    id                BIGSERIAL   PRIMARY KEY,
    produto_id        BIGINT      NOT NULL REFERENCES produtos(id),
    tipo_movimentacao VARCHAR(30) NOT NULL
                        CHECK (tipo_movimentacao IN (
                            'ENTRADA_COMPRA','ENTRADA_DESOSSA','SAIDA_VENDA',
                            'SAIDA_DESOSSA','SAIDA_DESCARTE','SAIDA_QUEBRA',
                            'SAIDA_MOAGEM','AJUSTE_POSITIVO','AJUSTE_NEGATIVO')),
    quantidade        NUMERIC(12,4) NOT NULL,
    custo_unitario    NUMERIC(12,4),
    documento_ref     VARCHAR(50),
    observacao        TEXT,
    usuario_id        BIGINT,
    created_at        TIMESTAMP   DEFAULT NOW()
);

CREATE INDEX idx_mov_produto_data ON movimentacao_estoque(produto_id, created_at DESC);

-- ─────────────────────────────────────────────────────────────────────
-- PROCESSO DE DESOSSA (execução real)
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE processo_desossa (
    id                BIGSERIAL   PRIMARY KEY,
    ficha_desossa_id  BIGINT      NOT NULL REFERENCES ficha_desossa(id),
    quantidade_entrada NUMERIC(12,4) NOT NULL,
    data_processo     TIMESTAMP   DEFAULT NOW(),
    usuario_id        BIGINT,
    observacao        TEXT,
    status            VARCHAR(20) DEFAULT 'CONCLUIDO'
);

CREATE TABLE processo_desossa_resultado (
    id                  BIGSERIAL   PRIMARY KEY,
    processo_desossa_id BIGINT      NOT NULL REFERENCES processo_desossa(id),
    produto_filho_id    BIGINT      NOT NULL REFERENCES produtos(id),
    quantidade_prevista NUMERIC(12,4),
    quantidade_real     NUMERIC(12,4) NOT NULL,
    custo_rateado       NUMERIC(12,4)
);

-- ─────────────────────────────────────────────────────────────────────
-- CLIENTES
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE clientes (
    id             BIGSERIAL    PRIMARY KEY,
    nome           VARCHAR(150) NOT NULL,
    cpf_cnpj       VARCHAR(18),
    tipo_pessoa    VARCHAR(5)   DEFAULT 'PF'
                    CHECK (tipo_pessoa IN ('PF','PJ')),
    telefone       VARCHAR(20),
    email          VARCHAR(100),
    endereco       TEXT,
    tipo_cliente   VARCHAR(20)  DEFAULT 'VAREJO'
                    CHECK (tipo_cliente IN ('VAREJO','ATACADO','RESTAURANTE','CONVENIADO')),
    limite_credito NUMERIC(12,2) DEFAULT 0,
    ativo          BOOLEAN      DEFAULT TRUE,
    created_at     TIMESTAMP    DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────────────
-- CAIXA
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE caixa (
    id                          BIGSERIAL   PRIMARY KEY,
    operador_id                 BIGINT,
    data_abertura               TIMESTAMP   DEFAULT NOW(),
    data_fechamento             TIMESTAMP,
    valor_abertura              NUMERIC(12,2) NOT NULL,
    valor_fechamento_informado  NUMERIC(12,2),
    valor_calculado             NUMERIC(12,2),
    status                      VARCHAR(20) DEFAULT 'ABERTO'
                                  CHECK (status IN ('ABERTO','FECHADO')),
    observacao                  TEXT
);

-- ─────────────────────────────────────────────────────────────────────
-- VENDAS
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE vendas (
    id           BIGSERIAL   PRIMARY KEY,
    numero_cupom VARCHAR(20),
    cliente_id   BIGINT      REFERENCES clientes(id),
    tipo_venda   VARCHAR(20) NOT NULL
                   CHECK (tipo_venda IN ('PDV','FATURAMENTO','DELIVERY')),
    status       VARCHAR(20) DEFAULT 'ABERTA'
                   CHECK (status IN ('ABERTA','FECHADA','CANCELADA')),
    subtotal     NUMERIC(12,2) NOT NULL DEFAULT 0,
    desconto     NUMERIC(12,2)          DEFAULT 0,
    total        NUMERIC(12,2) NOT NULL DEFAULT 0,
    troco        NUMERIC(12,2)          DEFAULT 0,
    data_venda   TIMESTAMP   DEFAULT NOW(),
    operador_id  BIGINT,
    caixa_id     BIGINT      REFERENCES caixa(id),
    nfce_chave   VARCHAR(44),
    nfce_status  VARCHAR(20),
    observacao   TEXT
);

CREATE INDEX idx_vendas_data_status ON vendas(data_venda DESC, status);

-- ─────────────────────────────────────────────────────────────────────
-- ITENS DE VENDA
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE itens_venda (
    id             BIGSERIAL     PRIMARY KEY,
    venda_id       BIGINT        NOT NULL REFERENCES vendas(id),
    produto_id     BIGINT        NOT NULL REFERENCES produtos(id),
    ean13_lido     VARCHAR(13),
    quantidade     NUMERIC(12,4) NOT NULL,
    preco_unitario NUMERIC(12,4) NOT NULL,
    desconto_item  NUMERIC(12,4) DEFAULT 0,
    total_item     NUMERIC(12,4) NOT NULL,
    custo_item     NUMERIC(12,4),
    tipo_entrada   VARCHAR(20)
                    CHECK (tipo_entrada IN ('BARCODE','PESAGEM_DIRETA','MANUAL'))
);

CREATE INDEX idx_itens_venda_venda ON itens_venda(venda_id);

-- ─────────────────────────────────────────────────────────────────────
-- PAGAMENTOS DE VENDA
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE pagamentos_venda (
    id              BIGSERIAL     PRIMARY KEY,
    venda_id        BIGINT        NOT NULL REFERENCES vendas(id),
    forma_pagamento VARCHAR(20)   NOT NULL
                      CHECK (forma_pagamento IN
                             ('DINHEIRO','CREDITO','DEBITO','PIX','FIADO','CONVENIO','VOUCHER')),
    valor           NUMERIC(12,2) NOT NULL,
    nsu             VARCHAR(20),
    autorizacao     VARCHAR(20),
    data_pagamento  TIMESTAMP     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────────────
-- FATURAMENTO DE CLIENTES
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE faturamento_cliente (
    id              BIGSERIAL     PRIMARY KEY,
    cliente_id      BIGINT        NOT NULL REFERENCES clientes(id),
    periodo_inicio  DATE          NOT NULL,
    periodo_fim     DATE          NOT NULL,
    total_vendas    NUMERIC(12,2) DEFAULT 0,
    total_pago      NUMERIC(12,2) DEFAULT 0,
    saldo_devedor   NUMERIC(12,2) DEFAULT 0,
    status          VARCHAR(20)   DEFAULT 'ABERTO'
                      CHECK (status IN ('ABERTO','PARCIAL','QUITADO','VENCIDO')),
    data_vencimento DATE,
    nfe_chave       VARCHAR(44),
    created_at      TIMESTAMP     DEFAULT NOW(),
    updated_at      TIMESTAMP     DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────────────
-- CONTAS A RECEBER
-- ─────────────────────────────────────────────────────────────────────
CREATE TABLE contas_a_receber (
    id              BIGSERIAL     PRIMARY KEY,
    cliente_id      BIGINT        NOT NULL REFERENCES clientes(id),
    faturamento_id  BIGINT        REFERENCES faturamento_cliente(id),
    venda_id        BIGINT        REFERENCES vendas(id),
    descricao       VARCHAR(200),
    valor           NUMERIC(12,2) NOT NULL,
    valor_pago      NUMERIC(12,2) DEFAULT 0,
    data_emissao    DATE          DEFAULT CURRENT_DATE,
    data_vencimento DATE,
    data_pagamento  DATE,
    status          VARCHAR(20)   DEFAULT 'ABERTO'
                      CHECK (status IN ('ABERTO','PARCIAL','PAGO','CANCELADO')),
    created_at      TIMESTAMP     DEFAULT NOW()
);

CREATE INDEX idx_contas_cliente_status ON contas_a_receber(cliente_id, status);

-- ─────────────────────────────────────────────────────────────────────
-- DADOS INICIAIS (seed)
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO categorias (nome) VALUES
    ('Bovino'),
    ('Suíno'),
    ('Frango'),
    ('Embutidos'),
    ('Temperos'),
    ('Subprodutos');

INSERT INTO clientes (nome, tipo_pessoa, tipo_cliente) VALUES
    ('CONSUMIDOR', 'PF', 'VAREJO');
