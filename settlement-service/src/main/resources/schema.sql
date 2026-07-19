CREATE TABLE IF NOT EXISTS "transacoes" (
    id UUID PRIMARY KEY,
    ordem_compra_id UUID NOT NULL,
    ordem_venda_id UUID NOT NULL,
    comprador_id UUID NOT NULL,
    vendedor_id UUID NOT NULL,
    carta_id UUID NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL,
    razao_falha TEXT,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transacoes_status ON transacoes(status);
CREATE INDEX IF NOT EXISTS idx_transacoes_comprador ON transacoes(comprador_id);
CREATE INDEX IF NOT EXISTS idx_transacoes_vendedor ON transacoes(vendedor_id);