CREATE TABLE IF NOT EXISTS "cartas" (
    carta_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    raridade TEXT,
    vida INT NOT NULL,
    descricao TEXT,
    imagem_url VARCHAR(255),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);