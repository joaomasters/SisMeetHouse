-- Rastreia cobranças PIX geradas via Mercado Pago
CREATE TABLE pagamento_pix (
    id              BIGSERIAL PRIMARY KEY,
    venda_id        BIGINT,
    mp_payment_id   BIGINT,
    valor           NUMERIC(12,2) NOT NULL,
    qr_code         TEXT,
    qr_code_base64  TEXT,
    status          VARCHAR(20) DEFAULT 'PENDENTE',   -- PENDENTE | APROVADO | EXPIRADO | ERRO
    created_at      TIMESTAMP   DEFAULT NOW(),
    confirmed_at    TIMESTAMP
);
