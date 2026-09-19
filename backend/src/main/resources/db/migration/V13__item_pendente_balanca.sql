-- Fila de itens pendentes de carga na balança, gerada automaticamente
-- toda vez que o preço de venda de um produto com codigo_balanca (PLU)
-- é alterado. Um item PENDENTE indica "esse preço ainda não chegou na
-- balança física". Substitui a heurística antiga baseada em
-- produtos.updated_at (que disparava para qualquer edição do produto,
-- não só mudança de preço).
CREATE TABLE item_pendente_balanca (
    id              BIGSERIAL     PRIMARY KEY,
    produto_id      BIGINT        NOT NULL REFERENCES produtos(id),
    preco_anterior  NUMERIC(12,4) NOT NULL,
    preco_novo      NUMERIC(12,4) NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
    carga_agendada_id BIGINT      REFERENCES carga_agendada(id),
    criado_em       TIMESTAMP     NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP     NOT NULL DEFAULT NOW(),
    enviado_em      TIMESTAMP,

    CONSTRAINT item_pendente_balanca_status_check
        CHECK (status IN ('PENDENTE', 'ENVIADO', 'CANCELADO'))
);

-- Só pode existir 1 item PENDENTE por produto por vez: se o preço mudar
-- de novo antes da carga ser feita, o item existente é atualizado em vez
-- de duplicado (ver ItemPendenteBalancaService).
CREATE UNIQUE INDEX idx_item_pendente_balanca_produto_pendente
    ON item_pendente_balanca(produto_id)
    WHERE status = 'PENDENTE';

CREATE INDEX idx_item_pendente_balanca_status ON item_pendente_balanca(status);
