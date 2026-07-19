INSERT INTO "carteiras" (user_id, dinheiro, username, criado_em) VALUES
('2b0cb2b1-ed7f-43cd-93ee-4ca15cb3a72f', 0, 'adminUser', '2026-07-19')
ON CONFLICT (user_id) DO NOTHING;