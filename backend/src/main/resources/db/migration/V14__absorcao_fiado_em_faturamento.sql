-- Consolidação Faturamento x Contas a Receber.
--
-- Hoje existiam duas formas independentes de gerar uma ContasAReceber pro
-- mesmo cliente: (1) o PDV cria uma na hora, por venda, quando a forma de
-- pagamento é FIADO; (2) "Gerar Fechamento" em Faturamento somava TODAS as
-- vendas do período (inclusive as já quitadas na hora e as que já tinham
-- virado conta avulsa em (1)), criando uma segunda cobrança sobre o mesmo
-- valor — cobrança em duplicidade.
--
-- A partir de agora, "Gerar Fechamento" só agrupa contas de fiado avulso
-- (as de (1)) que ainda estão em aberto e ainda não foram agrupadas em
-- nenhum outro fechamento. As contas agrupadas passam para o status
-- 'AGRUPADO' e ficam referenciando o fechamento que as absorveu — saem da
-- lista de "a receber" solta, mas continuam rastreáveis pra auditoria.
ALTER TABLE contas_a_receber
    ADD COLUMN absorvido_por_faturamento_id BIGINT REFERENCES faturamento_cliente(id);

CREATE INDEX idx_contas_absorvido_por_faturamento ON contas_a_receber(absorvido_por_faturamento_id);