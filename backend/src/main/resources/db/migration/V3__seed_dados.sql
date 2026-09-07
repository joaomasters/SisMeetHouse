-- =====================================================================
-- V3 — Seed de dados realistas para demonstração
-- Categorias IDs 1-6 e Cliente ID 1 já existem (V1)
-- =====================================================================

-- ─────────────────────────────────────────────────────────────────────
-- CLIENTES
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO clientes (id, nome, cpf_cnpj, tipo_pessoa, telefone, tipo_cliente, limite_credito, saldo_fiado_atual) VALUES
(2, 'João Silva',        '123.456.789-00',    'PF', '(11) 98765-4321', 'VAREJO',       500.00,  120.50),
(3, 'Maria Oliveira',    '987.654.321-00',    'PF', '(11) 91234-5678', 'VAREJO',       300.00,    0.00),
(4, 'Restaurante Sabor', '12.345.678/0001-99','PJ', '(11) 3333-4444',  'RESTAURANTE', 5000.00,  850.00),
(5, 'Acougue do Ze',     '98.765.432/0001-11','PJ', '(11) 2222-3333',  'ATACADO',    10000.00,    0.00),
(6, 'Carlos Pereira',    '111.222.333-44',    'PF', '(11) 97654-3210', 'VAREJO',       200.00,   75.00),
(7, 'Mercearia Central', '55.666.777/0001-88','PJ', '(11) 4444-5555',  'ATACADO',     8000.00, 1200.00),
(8, 'Ana Santos',        '444.555.666-77',    'PF', '(11) 96543-2109', 'VAREJO',       150.00,    0.00);
SELECT setval('clientes_id_seq', 8);

-- ─────────────────────────────────────────────────────────────────────
-- PRODUTOS
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO produtos (id, codigo_interno, codigo_balanca, nome, unidade_medida, tipo_produto,
                      preco_custo, preco_venda, estoque_atual, estoque_minimo, categoria_id, tem_ficha_desossa) VALUES
(1,  'BOV001',  1,  'Picanha',               'KG', 'CORTE',           35.00,  69.90,  45.50,  5.0, 1, FALSE),
(2,  'BOV002',  2,  'Alcatra',               'KG', 'CORTE',           28.00,  52.90,  38.20,  5.0, 1, FALSE),
(3,  'BOV003',  3,  'Contrafilé',       'KG', 'CORTE',           26.00,  49.90,  52.80,  8.0, 1, FALSE),
(4,  'BOV004',  4,  'Fraldinha',             'KG', 'CORTE',           22.00,  44.90,  27.60,  3.0, 1, FALSE),
(5,  'BOV005',  5,  'Coxao Mole',            'KG', 'CORTE',           20.00,  38.90,  63.40,  8.0, 1, FALSE),
(6,  'BOV006',  6,  'Patinho',               'KG', 'CORTE',           19.00,  36.90,  41.20,  5.0, 1, FALSE),
(7,  'BOV007',  7,  'Acem',                  'KG', 'CORTE',           16.00,  28.90,  88.50, 10.0, 1, FALSE),
(8,  'BOV008',  8,  'Costela Bovina',        'KG', 'CORTE',           14.00,  26.90,  72.30, 10.0, 1, FALSE),
(9,  'BOV009',  9,  'Musculo',               'KG', 'CORTE',           12.00,  22.90,  34.10,  5.0, 1, FALSE),
(10, 'BOV010', 10,  'File Mignon',           'KG', 'CORTE',           55.00,  99.90,  18.40,  3.0, 1, FALSE),
(11, 'BOV011', 11,  'Maminha',               'KG', 'CORTE',           24.00,  45.90,  31.70,  3.0, 1, FALSE),
(12, 'BOV012', 12,  'Carne Moida Bovina',    'KG', 'INDUSTRIALIZADO', 15.00,  26.90,  55.00, 10.0, 1, FALSE),
(13, 'BOV099', NULL,'Boi Traseiro Inteiro',  'KG', 'INSUMO',          10.00,  10.00,  80.00, 50.0, 1, TRUE),
(14, 'SUI001', 21,  'Bisteca Suina',         'KG', 'CORTE',           12.00,  22.90,  48.30,  5.0, 2, FALSE),
(15, 'SUI002', 22,  'Lombo Suino',           'KG', 'CORTE',           14.00,  25.90,  36.50,  5.0, 2, FALSE),
(16, 'SUI003', 23,  'Pernil Suino',          'KG', 'CORTE',           11.00,  20.90,  42.10,  5.0, 2, FALSE),
(17, 'SUI004', 24,  'Bacon Fatiado',         'KG', 'INDUSTRIALIZADO', 16.00,  32.90,  22.40,  3.0, 2, FALSE),
(18, 'FRA001', 31,  'Frango Inteiro',        'KG', 'CORTE',            7.00,  13.90,  85.60, 15.0, 3, FALSE),
(19, 'FRA002', 32,  'Coxa e Sobrecoxa',      'KG', 'CORTE',            8.00,  15.90,  64.20, 10.0, 3, FALSE),
(20, 'FRA003', 33,  'Peito de Frango',       'KG', 'CORTE',           10.00,  19.90,  47.80, 10.0, 3, FALSE),
(21, 'EMB001', 41,  'Linguica Toscana',      'KG', 'INDUSTRIALIZADO', 14.00,  28.90,  38.20,  5.0, 4, FALSE),
(22, 'EMB002', 42,  'Chourico',              'KG', 'INDUSTRIALIZADO', 12.00,  23.90,  28.60,  5.0, 4, FALSE),
(23, 'SUB001', 51,  'Figado Bovino',         'KG', 'SUBPRODUTO',       5.00,  10.90,  15.20,  2.0, 6, FALSE),
(24, 'SUB002', 52,  'Osso para Caldo',       'KG', 'SUBPRODUTO',       2.00,   5.90,  22.40,  3.0, 6, FALSE);
SELECT setval('produtos_id_seq', 24);

-- ─────────────────────────────────────────────────────────────────────
-- CAIXA
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO caixa (id, operador_id, data_abertura, data_fechamento, valor_abertura,
                   valor_fechamento_informado, valor_calculado, status, total_sangria, total_suprimento) VALUES
(1, 1, NOW()-INTERVAL'30 days', NOW()-INTERVAL'29 days 20 hours', 500.00, 3850.00, 3862.50, 'FECHADO', 200.00,   0.00),
(2, 1, NOW()-INTERVAL'15 days', NOW()-INTERVAL'14 days 21 hours', 500.00, 4120.00, 4105.75, 'FECHADO', 150.00, 100.00),
(3, 1, NOW()-INTERVAL'7 days',  NOW()-INTERVAL'6 days 22 hours',  500.00, 5230.00, 5218.40, 'FECHADO', 300.00,  50.00),
(4, 1, NOW()-INTERVAL'1 day',   NULL,                             500.00, NULL,    NULL,    'ABERTO',    0.00,   0.00);
SELECT setval('caixa_id_seq', 4);

-- ─────────────────────────────────────────────────────────────────────
-- SANGRIA E SUPRIMENTO
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO sangria_caixa (id, caixa_id, tipo, valor, motivo, operador_id, created_at) VALUES
(1, 1, 'SANGRIA',    200.00, 'Retirada para cofre',       1, NOW()-INTERVAL'29 days 15 hours'),
(2, 2, 'SANGRIA',    150.00, 'Retirada diaria',           1, NOW()-INTERVAL'14 days 16 hours'),
(3, 2, 'SUPRIMENTO', 100.00, 'Troco para abertura',       1, NOW()-INTERVAL'15 days 10 hours'),
(4, 3, 'SANGRIA',    200.00, 'Deposito bancario',         1, NOW()-INTERVAL'6 days 14 hours'),
(5, 3, 'SANGRIA',    100.00, 'Pagamento fornecedor',      1, NOW()-INTERVAL'6 days 17 hours'),
(6, 3, 'SUPRIMENTO',  50.00, 'Reposicao troco quebrado',  1, NOW()-INTERVAL'7 days 11 hours');
SELECT setval('sangria_caixa_id_seq', 6);

-- ─────────────────────────────────────────────────────────────────────
-- VENDAS
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO vendas (id, numero_cupom, cliente_id, tipo_venda, status,
                    subtotal, desconto, total, troco, data_venda, operador_id, caixa_id) VALUES
(1,  'PDV-001001', 1, 'PDV',         'FECHADA',  125.60,   0.00,  125.60,  4.40, NOW()-INTERVAL'29 days 10 hours', 1, 1),
(2,  'PDV-001002', 2, 'PDV',         'FECHADA',  234.90,  10.00,  224.90,  5.10, NOW()-INTERVAL'29 days 11 hours', 1, 1),
(3,  'PDV-001003', 1, 'PDV',         'FECHADA',   89.70,   0.00,   89.70,  0.30, NOW()-INTERVAL'29 days 14 hours', 1, 1),
(4,  'FAT-000001', 4, 'FATURAMENTO', 'FECHADA', 1580.00,  80.00, 1500.00,  0.00, NOW()-INTERVAL'28 days',          1, 1),
(5,  'PDV-001004', 1, 'PDV',         'FECHADA',  156.30,   0.00,  156.30, 43.70, NOW()-INTERVAL'27 days 9 hours',  1, 1),
(6,  'PDV-001005', 3, 'PDV',         'FECHADA',   78.50,   0.00,   78.50,  1.50, NOW()-INTERVAL'25 days 15 hours', 1, 1),
(7,  'PDV-001006', 1, 'PDV',         'FECHADA',  312.40,  12.40,  300.00,  0.00, NOW()-INTERVAL'20 days 10 hours', 1, 2),
(8,  'PDV-001007', 2, 'PDV',         'FECHADA',  145.80,   0.00,  145.80, 54.20, NOW()-INTERVAL'18 days 11 hours', 1, 2),
(9,  'FAT-000002', 7, 'FATURAMENTO', 'FECHADA', 2340.00, 100.00, 2240.00,  0.00, NOW()-INTERVAL'15 days',          1, 2),
(10, 'PDV-001008', 1, 'PDV',         'FECHADA',   67.90,   0.00,   67.90,  2.10, NOW()-INTERVAL'14 days 9 hours',  1, 2),
(11, 'PDV-001009', 6, 'PDV',         'FECHADA',  189.40,   0.00,  189.40,  0.60, NOW()-INTERVAL'12 days 14 hours', 1, 2),
(12, 'PDV-001010', 1, 'PDV',         'FECHADA',  223.50,   0.00,  223.50, 26.50, NOW()-INTERVAL'10 days 10 hours', 1, 3),
(13, 'PDV-001011', 3, 'PDV',         'FECHADA',   99.90,   0.00,   99.90,  0.10, NOW()-INTERVAL'9 days 11 hours',  1, 3),
(14, 'PDV-001012', 8, 'PDV',         'FECHADA',  134.70,   4.70,  130.00,  0.00, NOW()-INTERVAL'8 days 13 hours',  1, 3),
(15, 'FAT-000003', 5, 'FATURAMENTO', 'FECHADA', 3120.00, 120.00, 3000.00,  0.00, NOW()-INTERVAL'7 days',           1, 3),
(16, 'PDV-001013', 2, 'PDV',         'FECHADA',  178.20,   0.00,  178.20, 21.80, NOW()-INTERVAL'5 days 10 hours',  1, 3),
(17, 'PDV-001014', 1, 'PDV',         'FECHADA',  256.80,   6.80,  250.00,  0.00, NOW()-INTERVAL'4 days 14 hours',  1, 3),
(18, 'PDV-001015', 6, 'PDV',         'FECHADA',   88.40,   0.00,   88.40, 11.60, NOW()-INTERVAL'3 days 9 hours',   1, 3),
(19, 'PDV-001016', 1, 'PDV',         'FECHADA',  167.90,   0.00,  167.90, 32.10, NOW()-INTERVAL'2 days 10 hours',  1, 4),
(20, 'PDV-001017', 3, 'PDV',         'FECHADA',  312.60,  12.60,  300.00,  0.00, NOW()-INTERVAL'1 day 11 hours',   1, 4);
SELECT setval('vendas_id_seq', 20);

-- ─────────────────────────────────────────────────────────────────────
-- ITENS DE VENDA
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO itens_venda (venda_id, produto_id, quantidade, preco_unitario, desconto_item, total_item, custo_item, tipo_entrada) VALUES
(1,  1,  0.850, 69.90, 0.00,  59.42, 35.00, 'PESAGEM_DIRETA'),
(1, 18,  2.500, 13.90, 0.00,  34.75,  7.00, 'PESAGEM_DIRETA'),
(1, 21,  1.100, 28.90, 0.00,  31.79, 14.00, 'PESAGEM_DIRETA'),
(2,  3,  2.400, 49.90, 0.00, 119.76, 26.00, 'PESAGEM_DIRETA'),
(2, 19,  3.200, 15.90, 0.00,  50.88,  8.00, 'PESAGEM_DIRETA'),
(2, 22,  1.200, 23.90, 0.00,  28.68, 12.00, 'PESAGEM_DIRETA'),
(3,  8,  2.000, 26.90, 0.00,  53.80, 14.00, 'PESAGEM_DIRETA'),
(3, 23,  1.500, 10.90, 0.00,  16.35,  5.00, 'PESAGEM_DIRETA'),
(4,  5, 10.000, 38.90, 0.00, 389.00, 20.00, 'MANUAL'),
(4,  7, 15.000, 28.90, 0.00, 433.50, 16.00, 'MANUAL'),
(4,  8, 20.000, 26.90, 0.00, 538.00, 14.00, 'MANUAL'),
(4, 18, 15.500, 13.90, 0.00, 215.45,  7.00, 'MANUAL'),
(5,  2,  1.200, 52.90, 0.00,  63.48, 28.00, 'PESAGEM_DIRETA'),
(5, 14,  2.800, 22.90, 0.00,  64.12, 12.00, 'PESAGEM_DIRETA'),
(6, 20,  2.500, 19.90, 0.00,  49.75, 10.00, 'PESAGEM_DIRETA'),
(6, 17,  0.900, 32.90, 0.00,  29.61, 16.00, 'PESAGEM_DIRETA'),
(7,  1,  1.500, 69.90, 0.00, 104.85, 35.00, 'PESAGEM_DIRETA'),
(7, 10,  0.800, 99.90, 0.00,  79.92, 55.00, 'PESAGEM_DIRETA'),
(7, 12,  3.000, 26.90, 0.00,  80.70, 15.00, 'PESAGEM_DIRETA'),
(8,  3,  1.800, 49.90, 0.00,  89.82, 26.00, 'PESAGEM_DIRETA'),
(8, 15,  2.100, 25.90, 0.00,  54.39, 14.00, 'PESAGEM_DIRETA'),
(9,  5, 12.000, 38.90, 0.00, 466.80, 20.00, 'MANUAL'),
(9,  7, 18.000, 28.90, 0.00, 520.20, 16.00, 'MANUAL'),
(9, 12, 20.000, 26.90, 0.00, 538.00, 15.00, 'MANUAL'),
(9, 18, 20.000, 13.90, 0.00, 278.00,  7.00, 'MANUAL'),
(9, 19, 15.000, 15.90, 0.00, 238.50,  8.00, 'MANUAL'),
(10, 23, 2.000, 10.90, 0.00,  21.80,  5.00, 'PESAGEM_DIRETA'),
(10, 24, 3.500,  5.90, 0.00,  20.65,  2.00, 'PESAGEM_DIRETA'),
(10, 21, 0.800, 28.90, 0.00,  23.12, 14.00, 'PESAGEM_DIRETA'),
(11,  4, 2.100, 44.90, 0.00,  94.29, 22.00, 'PESAGEM_DIRETA'),
(11, 16, 2.500, 20.90, 0.00,  52.25, 11.00, 'PESAGEM_DIRETA'),
(12,  1, 1.000, 69.90, 0.00,  69.90, 35.00, 'PESAGEM_DIRETA'),
(12, 11, 1.500, 45.90, 0.00,  68.85, 24.00, 'PESAGEM_DIRETA'),
(12, 20, 2.500, 19.90, 0.00,  49.75, 10.00, 'PESAGEM_DIRETA'),
(13,  9, 2.200, 22.90, 0.00,  50.38, 12.00, 'PESAGEM_DIRETA'),
(13, 14, 2.100, 22.90, 0.00,  48.09, 12.00, 'PESAGEM_DIRETA'),
(14,  2, 1.500, 52.90, 0.00,  79.35, 28.00, 'PESAGEM_DIRETA'),
(14, 22, 1.200, 23.90, 0.00,  28.68, 12.00, 'PESAGEM_DIRETA'),
(15,  3, 20.000, 49.90, 0.00, 998.00, 26.00, 'MANUAL'),
(15,  5, 15.000, 38.90, 0.00, 583.50, 20.00, 'MANUAL'),
(15,  7, 25.000, 28.90, 0.00, 722.50, 16.00, 'MANUAL'),
(15, 18, 25.000, 13.90, 0.00, 347.50,  7.00, 'MANUAL'),
(16,  8, 3.200, 26.90, 0.00,  86.08, 14.00, 'PESAGEM_DIRETA'),
(16, 15, 1.800, 25.90, 0.00,  46.62, 14.00, 'PESAGEM_DIRETA'),
(17,  1, 2.000, 69.90, 0.00, 139.80, 35.00, 'PESAGEM_DIRETA'),
(17,  4, 1.500, 44.90, 0.00,  67.35, 22.00, 'PESAGEM_DIRETA'),
(17, 17, 1.200, 32.90, 0.00,  39.48, 16.00, 'PESAGEM_DIRETA'),
(18, 20, 2.800, 19.90, 0.00,  55.72, 10.00, 'PESAGEM_DIRETA'),
(18, 21, 1.100, 28.90, 0.00,  31.79, 14.00, 'PESAGEM_DIRETA'),
(19,  3, 1.800, 49.90, 0.00,  89.82, 26.00, 'PESAGEM_DIRETA'),
(19, 11, 1.200, 45.90, 0.00,  55.08, 24.00, 'PESAGEM_DIRETA'),
(20,  5, 3.500, 38.90, 0.00, 136.15, 20.00, 'PESAGEM_DIRETA'),
(20,  7, 4.000, 28.90, 0.00, 115.60, 16.00, 'PESAGEM_DIRETA'),
(20, 12, 2.500, 26.90, 0.00,  67.25, 15.00, 'PESAGEM_DIRETA');

-- ─────────────────────────────────────────────────────────────────────
-- PAGAMENTOS DE VENDA
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO pagamentos_venda (venda_id, forma_pagamento, valor, data_pagamento) VALUES
(1,  'DINHEIRO', 130.00, NOW()-INTERVAL'29 days 10 hours'),
(2,  'DEBITO',   224.90, NOW()-INTERVAL'29 days 11 hours'),
(3,  'PIX',       89.70, NOW()-INTERVAL'29 days 14 hours'),
(4,  'FIADO',   1500.00, NOW()-INTERVAL'28 days'),
(5,  'DINHEIRO', 200.00, NOW()-INTERVAL'27 days 9 hours'),
(6,  'CREDITO',   78.50, NOW()-INTERVAL'25 days 15 hours'),
(7,  'PIX',      300.00, NOW()-INTERVAL'20 days 10 hours'),
(8,  'DINHEIRO', 200.00, NOW()-INTERVAL'18 days 11 hours'),
(9,  'FIADO',   2240.00, NOW()-INTERVAL'15 days'),
(10, 'DINHEIRO',  70.00, NOW()-INTERVAL'14 days 9 hours'),
(11, 'DEBITO',   189.40, NOW()-INTERVAL'12 days 14 hours'),
(12, 'PIX',      223.50, NOW()-INTERVAL'10 days 10 hours'),
(13, 'CREDITO',   99.90, NOW()-INTERVAL'9 days 11 hours'),
(14, 'PIX',      130.00, NOW()-INTERVAL'8 days 13 hours'),
(15, 'FIADO',   3000.00, NOW()-INTERVAL'7 days'),
(16, 'DEBITO',   178.20, NOW()-INTERVAL'5 days 10 hours'),
(17, 'PIX',      250.00, NOW()-INTERVAL'4 days 14 hours'),
(18, 'DINHEIRO', 100.00, NOW()-INTERVAL'3 days 9 hours'),
(19, 'CREDITO',  167.90, NOW()-INTERVAL'2 days 10 hours'),
(20, 'PIX',      300.00, NOW()-INTERVAL'1 day 11 hours');

-- ─────────────────────────────────────────────────────────────────────
-- PERDAS DE ESTOQUE
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO perdas_estoque (id, produto_id, quantidade, custo_unitario, custo_total, motivo, observacao, usuario_id, created_at) VALUES
(1,  1,  0.350, 35.00, 12.25, 'VENCIMENTO', 'Picanha passou do prazo',           1, NOW()-INTERVAL'25 days'),
(2,  3,  1.200, 26.00, 31.20, 'AVARIA',     'Queda de temperatura no freezer',   1, NOW()-INTERVAL'22 days'),
(3, 18,  2.500,  7.00, 17.50, 'VENCIMENTO', 'Frango vencido',                    1, NOW()-INTERVAL'20 days'),
(4, 21,  0.800, 14.00, 11.20, 'AVARIA',     'Embalagem rompida',                 1, NOW()-INTERVAL'18 days'),
(5,  5,  0.600, 20.00, 12.00, 'FURTO',      'Diferenca no inventario',           1, NOW()-INTERVAL'15 days'),
(6,  8,  1.500, 14.00, 21.00, 'DESOSSA',    'Osso e gordura excedente',          1, NOW()-INTERVAL'12 days'),
(7, 20,  1.800, 10.00, 18.00, 'VENCIMENTO', 'Peito de frango vencido',           1, NOW()-INTERVAL'9 days'),
(8,  9,  0.400, 12.00,  4.80, 'AVARIA',     'Produto danificado na camara fria', 1, NOW()-INTERVAL'6 days'),
(9, 22,  0.500, 12.00,  6.00, 'OUTROS',     'Ajuste de estoque',                 1, NOW()-INTERVAL'4 days'),
(10, 19, 1.200,  8.00,  9.60, 'VENCIMENTO', 'Coxa e sobrecoxa vencida',          1, NOW()-INTERVAL'2 days');
SELECT setval('perdas_estoque_id_seq', 10);

-- ─────────────────────────────────────────────────────────────────────
-- INVENTARIO FISICO
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO inventario_fisico (id, status, observacao, usuario_id, data_inicio, data_fim) VALUES
(1, 'FINALIZADO', 'Inventario mensal — primeira quinzena',   1, NOW()-INTERVAL'20 days', NOW()-INTERVAL'19 days 20 hours'),
(2, 'ABERTO',     'Inventario parcial bovinos desta semana', 1, NOW()-INTERVAL'1 day',   NULL);
SELECT setval('inventario_fisico_id_seq', 2);

INSERT INTO inventario_fisico_item (inventario_id, produto_id, saldo_sistema, saldo_contado, divergencia) VALUES
(1,  1,  55.000, 54.200, -0.800),
(1,  2,  45.000, 45.100,  0.100),
(1,  3,  65.000, 63.800, -1.200),
(1,  4,  32.000, 31.600, -0.400),
(1,  5,  75.000, 74.400, -0.600),
(1,  6,  50.000, 49.800, -0.200),
(1,  7, 100.000, 100.200, 0.200),
(1,  8,  85.000, 84.500, -0.500),
(1,  9,  40.000, 39.800, -0.200),
(1, 10,  22.000, 22.100,  0.100),
(1, 11,  38.000, 37.600, -0.400),
(1, 12,  65.000, 64.700, -0.300),
(1, 18, 100.000, 98.500, -1.500),
(1, 19,  80.000, 79.200, -0.800),
(1, 20,  60.000, 59.500, -0.500),
(2,  1,  45.500,  NULL,   NULL),
(2,  2,  38.200,  NULL,   NULL),
(2,  3,  52.800,  NULL,   NULL),
(2,  4,  27.600,  NULL,   NULL),
(2,  5,  63.400,  NULL,   NULL),
(2,  7,  88.500,  NULL,   NULL),
(2,  8,  72.300,  NULL,   NULL),
(2, 10,  18.400,  NULL,   NULL),
(2, 11,  31.700,  NULL,   NULL),
(2, 12,  55.000,  NULL,   NULL);

-- ─────────────────────────────────────────────────────────────────────
-- CONTAS A PAGAR
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO contas_pagar (id, descricao, fornecedor, valor, valor_pago, data_vencimento, data_pagamento, categoria, status) VALUES
(1,  'Compra de bovinos — Fazenda Sao Joao',   'Fazenda Sao Joao',  4500.00, 4500.00, CURRENT_DATE-28, CURRENT_DATE-26, 'Materia-prima',  'PAGO'),
(2,  'Aluguel do ponto comercial',             'Imobiliaria BRC',   2800.00, 2800.00, CURRENT_DATE-30, CURRENT_DATE-29, 'Infraestrutura', 'PAGO'),
(3,  'Manutencao camara fria',                 'FrioTec Servicos',   350.00,  350.00, CURRENT_DATE-20, CURRENT_DATE-19, 'Manutencao',     'PAGO'),
(4,  'Compra suinos — Frigorifico Delta',       'Frigorifico Delta', 3200.00, 1600.00, CURRENT_DATE-15, NULL,            'Materia-prima',  'PARCIAL'),
(5,  'Energia eletrica',                       'CEMIG',              680.00,    0.00, CURRENT_DATE-5,  NULL,            'Utilidades',     'ABERTO'),
(6,  'Embalagens e bandejas',                  'EmbalaMax',          420.00,    0.00, CURRENT_DATE-3,  NULL,            'Insumos',        'ABERTO'),
(7,  'Compra frangos — Aviario Bela Vista',    'Aviario Bela Vista',1800.00,    0.00, CURRENT_DATE+5,  NULL,            'Materia-prima',  'ABERTO'),
(8,  'Seguro do estabelecimento',              'Seguradora Confia',   890.00,    0.00, CURRENT_DATE+10, NULL,            'Seguros',        'ABERTO'),
(9,  'Salarios — quinzena',                    'Folha de Pagamento',3200.00,    0.00, CURRENT_DATE+2,  NULL,            'Pessoal',        'ABERTO'),
(10, 'Compra embutidos — Distribuidora WS',    'Distribuidora WS',  1100.00,    0.00, CURRENT_DATE-10, NULL,            'Materia-prima',  'ABERTO');
SELECT setval('contas_pagar_id_seq', 10);

-- ─────────────────────────────────────────────────────────────────────
-- FATURAMENTO DE CLIENTES
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO faturamento_cliente (id, cliente_id, periodo_inicio, periodo_fim,
                                 total_vendas, total_pago, saldo_devedor, status, data_vencimento) VALUES
(1, 4, CURRENT_DATE-30, CURRENT_DATE-16, 1500.00, 0.00, 1500.00, 'VENCIDO', CURRENT_DATE-16),
(2, 7, CURRENT_DATE-15, CURRENT_DATE-1,  2240.00, 0.00, 2240.00, 'ABERTO',  CURRENT_DATE+5),
(3, 5, CURRENT_DATE-7,  CURRENT_DATE,    3000.00, 0.00, 3000.00, 'ABERTO',  CURRENT_DATE+7);
SELECT setval('faturamento_cliente_id_seq', 3);

-- ─────────────────────────────────────────────────────────────────────
-- CONTAS A RECEBER
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO contas_a_receber (id, cliente_id, faturamento_id, venda_id, descricao,
                               valor, valor_pago, data_vencimento, status) VALUES
(1, 4, 1, 4,  'Fatura 01 — Restaurante Sabor', 1500.00, 0.00, CURRENT_DATE-16, 'ABERTO'),
(2, 7, 2, 9,  'Fatura 02 — Mercearia Central', 2240.00, 0.00, CURRENT_DATE+5,  'ABERTO'),
(3, 5, 3, 15, 'Fatura 03 — Acougue do Ze',     3000.00, 0.00, CURRENT_DATE+7,  'ABERTO'),
(4, 2, NULL, NULL, 'Fiado Joao Silva',            120.50, 0.00, CURRENT_DATE+15, 'ABERTO'),
(5, 6, NULL, NULL, 'Fiado Carlos Pereira',          75.00, 0.00, CURRENT_DATE+10, 'ABERTO');
SELECT setval('contas_a_receber_id_seq', 5);

-- ─────────────────────────────────────────────────────────────────────
-- FICHA DE DESOSSA
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO ficha_desossa (id, produto_pai_id, nome, descricao, perda_total_percentual) VALUES
(1, 13, 'Desossa Boi Traseiro', 'Processo completo de desossa do traseiro bovino', 8.00);
SELECT setval('ficha_desossa_id_seq', 1);

INSERT INTO ficha_desossa_itens (id, ficha_desossa_id, produto_filho_id, percentual_rendimento, sequencia) VALUES
(1, 1,  1, 12.00, 1),
(2, 1,  2, 18.00, 2),
(3, 1,  4, 10.00, 3),
(4, 1, 11,  8.00, 4),
(5, 1,  3, 22.00, 5),
(6, 1,  6, 15.00, 6),
(7, 1, 24,  7.00, 7);
SELECT setval('ficha_desossa_itens_id_seq', 7);

-- ─────────────────────────────────────────────────────────────────────
-- PROCESSO DE DESOSSA
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO processo_desossa (id, ficha_desossa_id, quantidade_entrada, data_processo, usuario_id, observacao, status) VALUES
(1, 1, 100.000, NOW()-INTERVAL'10 days', 1, 'Desossa de 100kg de traseiro bovino', 'CONCLUIDO');
SELECT setval('processo_desossa_id_seq', 1);

INSERT INTO processo_desossa_resultado (id, processo_desossa_id, produto_filho_id, quantidade_prevista, quantidade_real, custo_rateado) VALUES
(1, 1,  1, 12.000, 11.800, 11800.00),
(2, 1,  2, 18.000, 17.600, 17600.00),
(3, 1,  4, 10.000,  9.800,  9800.00),
(4, 1, 11,  8.000,  7.900,  7900.00),
(5, 1,  3, 22.000, 21.500, 21500.00),
(6, 1,  6, 15.000, 14.700, 14700.00),
(7, 1, 24,  7.000,  7.100,  1420.00);
SELECT setval('processo_desossa_resultado_id_seq', 7);

-- ─────────────────────────────────────────────────────────────────────
-- CARGA BALANCA
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO carga_balanca (id, produto_id, tipo_balanca, codigo_plu, preco_enviado, validade_dias, status) VALUES
(1,  1, 'TOLEDO_MGV6',  1,  69.90, 7, 'ENVIADO'),
(2,  2, 'TOLEDO_MGV6',  2,  52.90, 7, 'ENVIADO'),
(3,  3, 'TOLEDO_MGV6',  3,  49.90, 7, 'ENVIADO'),
(4,  5, 'TOLEDO_MGV6',  5,  38.90, 7, 'ENVIADO'),
(5,  8, 'TOLEDO_MGV6',  8,  26.90, 7, 'ENVIADO'),
(6, 18, 'TOLEDO_MGV6', 31,  13.90, 3, 'ENVIADO'),
(7, 19, 'TOLEDO_MGV6', 32,  15.90, 3, 'ENVIADO'),
(8, 21, 'TOLEDO_MGV6', 41,  28.90, 7, 'ENVIADO'),
(9, 10, 'TOLEDO_MGV7', 10,  99.90, 7, 'PENDENTE'),
(10, 4, 'TOLEDO_MGV7',  4,  44.90, 7, 'PENDENTE');
SELECT setval('carga_balanca_id_seq', 10);

-- ─────────────────────────────────────────────────────────────────────
-- MOVIMENTACAO DE ESTOQUE
-- ─────────────────────────────────────────────────────────────────────
INSERT INTO movimentacao_estoque (produto_id, tipo_movimentacao, quantidade, custo_unitario, documento_ref, observacao, usuario_id) VALUES
(13, 'ENTRADA_COMPRA',   200.000, 10.00, 'NF-001',      'Compra Fazenda Sao Joao',    1),
(18, 'ENTRADA_COMPRA',   100.000,  7.00, 'NF-002',      'Compra Aviario Bela Vista',  1),
(19, 'ENTRADA_COMPRA',    80.000,  8.00, 'NF-002',      'Compra Aviario Bela Vista',  1),
(20, 'ENTRADA_COMPRA',    60.000, 10.00, 'NF-002',      'Compra Aviario Bela Vista',  1),
(14, 'ENTRADA_COMPRA',    60.000, 12.00, 'NF-003',      'Compra Frigorifico Delta',   1),
(15, 'ENTRADA_COMPRA',    50.000, 14.00, 'NF-003',      'Compra Frigorifico Delta',   1),
(16, 'ENTRADA_COMPRA',    60.000, 11.00, 'NF-003',      'Compra Frigorifico Delta',   1),
(21, 'ENTRADA_COMPRA',    50.000, 14.00, 'NF-004',      'Compra Distribuidora WS',    1),
(22, 'ENTRADA_COMPRA',    40.000, 12.00, 'NF-004',      'Compra Distribuidora WS',    1),
(13, 'SAIDA_DESOSSA',    100.000, 10.00, 'DESOSSA-001', 'Saida boi para desossa',     1),
( 1, 'ENTRADA_DESOSSA',   11.800, 35.00, 'DESOSSA-001', 'Picanha — desossa',          1),
( 2, 'ENTRADA_DESOSSA',   17.600, 28.00, 'DESOSSA-001', 'Alcatra — desossa',          1),
( 4, 'ENTRADA_DESOSSA',    9.800, 22.00, 'DESOSSA-001', 'Fraldinha — desossa',        1),
(11, 'ENTRADA_DESOSSA',    7.900, 24.00, 'DESOSSA-001', 'Maminha — desossa',          1),
( 3, 'ENTRADA_DESOSSA',   21.500, 26.00, 'DESOSSA-001', 'Contrafile — desossa',       1),
( 6, 'ENTRADA_DESOSSA',   14.700, 19.00, 'DESOSSA-001', 'Patinho — desossa',          1),
(24, 'ENTRADA_DESOSSA',    7.100,  2.00, 'DESOSSA-001', 'Osso para caldo — desossa',  1),
( 1, 'AJUSTE_NEGATIVO',    0.800, 35.00, 'INV-001',     'Ajuste inventario fisico',   1),
( 3, 'AJUSTE_NEGATIVO',    1.200, 26.00, 'INV-001',     'Ajuste inventario fisico',   1),
( 5, 'AJUSTE_NEGATIVO',    0.600, 20.00, 'INV-001',     'Ajuste inventario fisico',   1),
( 1, 'SAIDA_VENDA',        6.500, 35.00, 'VENDAS-MES',  'Resumo vendas mes',          1),
( 3, 'SAIDA_VENDA',        8.000, 26.00, 'VENDAS-MES',  'Resumo vendas mes',          1),
( 5, 'SAIDA_VENDA',       40.600, 20.00, 'VENDAS-MES',  'Resumo vendas mes',          1),
(18, 'SAIDA_VENDA',       63.000,  7.00, 'VENDAS-MES',  'Resumo vendas mes',          1),
(21, 'SAIDA_VENDA',       13.000, 14.00, 'VENDAS-MES',  'Resumo vendas mes',          1);
