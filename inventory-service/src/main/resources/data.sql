-- 1. Sincronização da tabela de referência de cartas (Mesmos UUIDs do Catálogo e Rewards)
INSERT INTO "cartas" (id_carta, nome) VALUES
-- Cartas de Água
('11111111-1111-1111-1111-000000000001', 'Voltante Azul'),
('11111111-1111-1111-1111-000000000002', 'Voltante de Praia'),
('11111111-1111-1111-1111-000000000003', 'Voltante Mestre D''água'),

-- Cartas de Fogo
('22222222-2222-2222-2222-000000000001', 'Voltante Vermelho'),
('22222222-2222-2222-2222-000000000002', 'Voltante Vulcânico'),
('22222222-2222-2222-2222-000000000003', 'Voltante Mestre do Fogo'),

-- Cartas de Grama
('33333333-3333-3333-3333-000000000001', 'Voltante Verde'),
('33333333-3333-3333-3333-000000000002', 'Voltante Jardineiro'),
('33333333-3333-3333-3333-000000000003', 'Voltante Mestre da Grama'),

-- Cartas Elétricas
('44444444-4444-4444-4444-000000000001', 'Voltante Amarelo'),
('44444444-4444-4444-4444-000000000002', 'Voltante Tempestade'),
('44444444-4444-4444-4444-000000000003', 'Voltante Mestre dos Raios')
ON CONFLICT (id_carta) DO NOTHING;