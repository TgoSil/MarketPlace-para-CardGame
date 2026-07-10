-- Script do banco de dados, executado sempre que o container inicia

CREATE TABLE IF NOT EXISTS "users" (
    id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    cargo VARCHAR(16) NOT NULL
);

INSERT INTO "users" (id, username, email, senha, cargo)
SELECT '2b0cb2b1-ed7f-43cd-93ee-4ca15cb3a72f','adminUser', 'emailAdmin@admin.com',
'$2b$12$7hoRZfJrRKD2nIm2vHLs7OBETy.LWenXXMLKf99W8M4PUwO6KB7fu', 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM "users"
    WHERE id='2b0cb2b1-ed7f-43cd-93ee-4ca15cb3a72f'
    OR email='emailAdmin@admin.com'
    OR username='adminUser'
)