CREATE TABLE carga_agendada (
    id              BIGSERIAL    PRIMARY KEY,
    tipo_balanca    VARCHAR(30)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
    produtos_count  INT          NOT NULL DEFAULT 0,
    conteudo_carga  TEXT         NOT NULL,
    ip_balanca      VARCHAR(50),
    porta_balanca   INT          NOT NULL DEFAULT 8000,
    criado_em       TIMESTAMP    NOT NULL DEFAULT NOW(),
    aplicado_em     TIMESTAMP,
    erro_mensagem   TEXT
);

CREATE INDEX idx_carga_agendada_status ON carga_agendada(status);
