CREATE TABLE IF NOT EXISTS "inventarios" (
    user_id UUID NOT NULL,
    carta_id UUID NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    PRIMARY KEY (user_id, carta_id),
    CONSTRAINT quantidade_positiva CHECK (quantidade >= 0)
);