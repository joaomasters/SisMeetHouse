-- Controle de acesso por perfis (RBAC)
-- Perfis são uma tabela de verdade (não enum fixo), para permitir
-- criar novos perfis no futuro sem alterar código.
CREATE TABLE perfis (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(50) NOT NULL UNIQUE,
    descricao   TEXT,
    protegido   BOOLEAN NOT NULL DEFAULT FALSE, -- perfis protegidos (ex: SUPER_ADMIN) não podem ser excluídos
    ativo       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);


-- Matriz de permissões: uma linha por (perfil, módulo), com 4 ações possíveis.
-- O catálogo de módulos é fixo no código (enum), pois cada módulo corresponde
-- a telas/endpoints reais do sistema — só o SUPER_ADMIN edita QUEM tem acesso
-- a cada um, não o catálogo de módulos em si.
CREATE TABLE perfil_permissoes (
    id           BIGSERIAL PRIMARY KEY,
    perfil_id    BIGINT NOT NULL REFERENCES perfis(id) ON DELETE CASCADE,
    modulo       VARCHAR(40) NOT NULL,
    pode_ver     BOOLEAN NOT NULL DEFAULT FALSE,
    pode_criar   BOOLEAN NOT NULL DEFAULT FALSE,
    pode_editar  BOOLEAN NOT NULL DEFAULT FALSE,
    pode_excluir BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (perfil_id, modulo)
);


-- Usuários reais — substitui o login fixo (teste/teste) que existia hardcoded no código.
CREATE TABLE usuarios (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    login       VARCHAR(50) NOT NULL UNIQUE,
    senha_hash  VARCHAR(100) NOT NULL, -- BCrypt
    perfil_id   BIGINT NOT NULL REFERENCES perfis(id),
    ativo       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);


-- Perfis padrão
INSERT INTO perfis (id, nome, descricao, protegido) VALUES
    (1, 'SUPER_ADMIN', 'Acesso total ao sistema, gerencia perfis e permissões', TRUE),
    (2, 'ADMIN',       'Dono do estabelecimento — acesso amplo às operações', FALSE),
    (3, 'GESTOR',      'Gerente da loja — acesso operacional, sem gestão de usuários', FALSE),
    (4, 'CAIXA',       'Operadora de caixa — acesso restrito ao PDV', FALSE);

SELECT setval('perfis_id_seq', 4);


-- Permissões padrão do SUPER_ADMIN: acesso total a tudo.
-- (O código também garante isso independentemente destas linhas,
-- como segunda camada de segurança — ver PermissaoService.)
INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
SELECT 1, modulo, TRUE, TRUE, TRUE, TRUE FROM (VALUES
    ('PDV'), ('SANGRIA'), ('PRODUTOS'), ('RECEBIMENTO'), ('FICHAS_DESOSSA'),
    ('RATEIO_DESOSSA'), ('INVENTARIO'), ('PERDAS'), ('NF_SAIDA'), ('FATURAMENTO'),
    ('CONTAS_RECEBER'), ('CONTAS_PAGAR'), ('DRE'), ('RELATORIOS'), ('CARGA_BALANCA'),
    ('USUARIOS'), ('PERFIS')
) AS modulos(modulo);


-- Permissões padrão do ADMIN: tudo, exceto a matriz de Perfis
-- (pode gerenciar Usuários, mas não redefinir permissões de perfil).
INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
SELECT 2, modulo, TRUE, TRUE, TRUE, TRUE FROM (VALUES
    ('PDV'), ('SANGRIA'), ('PRODUTOS'), ('RECEBIMENTO'), ('FICHAS_DESOSSA'),
    ('RATEIO_DESOSSA'), ('INVENTARIO'), ('PERDAS'), ('NF_SAIDA'), ('FATURAMENTO'),
    ('CONTAS_RECEBER'), ('CONTAS_PAGAR'), ('DRE'), ('RELATORIOS'), ('CARGA_BALANCA'),
    ('USUARIOS')
) AS modulos(modulo);
INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
VALUES (2, 'PERFIS', TRUE, FALSE, FALSE, FALSE); -- ADMIN só visualiza os perfis, não edita a matriz


-- Permissões padrão do GESTOR: operacional completo, sem Usuários/Perfis.
INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
SELECT 3, modulo, TRUE, TRUE, TRUE, FALSE FROM (VALUES
    ('PDV'), ('SANGRIA'), ('PRODUTOS'), ('RECEBIMENTO'), ('FICHAS_DESOSSA'),
    ('RATEIO_DESOSSA'), ('INVENTARIO'), ('PERDAS'), ('NF_SAIDA'), ('FATURAMENTO'),
    ('CONTAS_RECEBER'), ('CONTAS_PAGAR')
) AS modulos(modulo);
INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir)
SELECT 3, modulo, TRUE, FALSE, FALSE, FALSE FROM (VALUES
    ('DRE'), ('RELATORIOS'), ('CARGA_BALANCA')
) AS modulos(modulo);


-- Permissões padrão do CAIXA: só o essencial pra operar o PDV.
INSERT INTO perfil_permissoes (perfil_id, modulo, pode_ver, pode_criar, pode_editar, pode_excluir) VALUES
    (4, 'PDV',      TRUE, TRUE,  FALSE, FALSE),
    (4, 'SANGRIA',  TRUE, TRUE,  FALSE, FALSE),
    (4, 'PRODUTOS', TRUE, FALSE, FALSE, FALSE);


-- Migra o acesso atual (teste/teste, hardcoded no código) para um
-- usuário SUPER_ADMIN real, com senha protegida por BCrypt.
-- Login e senha continuam os mesmos ("teste"/"teste") — só a forma
-- de autenticação que passa a ser de verdade.
INSERT INTO usuarios (nome, login, senha_hash, perfil_id, ativo) VALUES
    ('Administrador', 'teste', '$2b$10$16gSs7vY97TniA5xpoJmJuMh5GuGXvGbIW2Gi5iTulfIPUDeYkWOy', 1, TRUE);