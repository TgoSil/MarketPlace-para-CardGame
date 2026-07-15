CREATE TABLE IF NOT EXISTS "carteiras" (
    user_id UUID primary key,
    dinheiro int NOT NULL DEFAULT 0,
    CONSTRAINT dinheiro_positivo CHECK (dinheiro >= 0)
);