CREATE TABLE IF NOT EXISTS "carteiras" (
    user_id UUID primary key,
    dinheiro int NOT NULL DEFAULT 0,
    username VARCHAR(16) NOT NULL,
    criado_em DATE NOT NULL,
    CONSTRAINT dinheiro_positivo CHECK (dinheiro >= 0)
);