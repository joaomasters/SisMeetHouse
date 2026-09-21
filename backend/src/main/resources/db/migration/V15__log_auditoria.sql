-- Log de auditoria: quem fez o quê, quando, em qual módulo. Alimentado
-- automaticamente por AuditoriaAspect, que já intercepta todo endpoint
-- marcado com @ExigirPermissao (a mesma anotação que protege o acesso).
-- Só ações de CRIAR/EDITAR/EXCLUIR são logadas — VER geraria volume enorme
-- (toda tela lista dados o tempo todo) sem agregar valor de auditoria.
CREATE TABLE log_auditoria (
    id              BIGSERIAL     PRIMARY KEY,
    usuario_id      BIGINT,                    -- sem FK de propósito: log sobrevive mesmo se o usuário for excluído depois
    nome_usuario    VARCHAR(150)  NOT NULL,     -- snapshot do nome no momento da ação (não muda se o usuário for renomeado depois)
    perfil_usuario  VARCHAR(50),
    modulo          VARCHAR(40)   NOT NULL,
    acao            VARCHAR(20)   NOT NULL,
    descricao       TEXT,
    criado_em       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_log_auditoria_criado_em ON log_auditoria(criado_em DESC);
CREATE INDEX idx_log_auditoria_usuario   ON log_auditoria(usuario_id);
CREATE INDEX idx_log_auditoria_modulo    ON log_auditoria(modulo);

-- Auditoria é restrita a quem administra o negócio — SUPER_ADMIN já enxerga
-- tudo automaticamente (segunda camada de segurança no código), então só
-- precisa conceder explicitamente pro ADMIN aqui.
INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
VALUES (2, 'AUDITORIA', TRUE, FALSE, FALSE, FALSE);