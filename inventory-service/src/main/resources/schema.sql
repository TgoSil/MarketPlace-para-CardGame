CREATE TABLE IF NOT EXISTS "cartas" (
    id_carta UUID primary key,
    nome varchar(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS "inventarios" (
    user_id UUID NOT NULL,
    id_carta UUID NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    FOREIGN KEY (id_carta) REFERENCES cartas(id_carta),
    PRIMARY KEY (user_id, id_carta),
    CONSTRAINT quantidade_positiva CHECK (quantidade >= 0)
);