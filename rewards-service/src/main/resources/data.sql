-- 1. Inserção dos Rewards Diários (Mantendo o ciclo de 30 dias)
INSERT INTO rewards (dia_ciclo, tipo_reward, quantidade_moedas_base, tier_pacote_base) VALUES
(1,  'MOEDAS', 50,   null),
(2,  'MOEDAS', 50,   null),
(3,  'PACOTE', null, 'BASICO'),
(4,  'MOEDAS', 100,  null),
(5,  'PACOTE', null, 'BASICO'),
(6,  'MOEDAS', 150,  null),
(7,  'MOEDAS', 150,  null),
(8,  'PACOTE', null, 'BASICO'),
(9,  'MOEDAS', 200,  null),
(10, 'PACOTE', null, 'NORMAL'),
(11, 'MOEDAS', 250,  null),
(12, 'MOEDAS', 250,  null),
(13, 'PACOTE', null, 'NORMAL'),
(14, 'MOEDAS', 300,  null),
(15, 'PACOTE', null, 'ESPECIAL'),
(16, 'MOEDAS', 350,  null),
(17, 'MOEDAS', 350,  null),
(18, 'PACOTE', null, 'ESPECIAL'),
(19, 'MOEDAS', 400,  null),
(20, 'PACOTE', null, 'EPICO'),
(21, 'MOEDAS', 450,  null),
(22, 'MOEDAS', 450,  null),
(23, 'PACOTE', null, 'EPICO'),
(24, 'MOEDAS', 500,  null),
(25, 'PACOTE', null, 'MITICO'),
(26, 'MOEDAS', 550,  null),
(27, 'MOEDAS', 550,  null),
(28, 'PACOTE', null, 'MITICO'),
(29, 'MOEDAS', 600,  null),
(30, 'PACOTE', null, 'LENDARIO')
ON CONFLICT (dia_ciclo) DO NOTHING;

-- 2. Inserção das Probabilidades de Pacotes
-- Redefinido para as raridades atuais do jogo: 'BAS' (Básica), 'RAR' (Rara) e 'LEN' (Lendária)
INSERT INTO pacote_probabilidades (tier_pacote, raridade, porcentagem) VALUES
('BASICO',   'BAS', 85.00), ('BASICO',   'RAR', 14.00), ('BASICO',   'LEN', 1.00),
('NORMAL',   'BAS', 70.00), ('NORMAL',   'RAR', 25.00), ('NORMAL',   'LEN', 5.00),
('ESPECIAL', 'BAS', 50.00), ('ESPECIAL', 'RAR', 40.00), ('ESPECIAL', 'LEN', 10.00),
('EPICO',    'BAS', 30.00), ('EPICO',    'RAR', 50.00), ('EPICO',    'LEN', 20.00),
('MITICO',   'BAS', 10.00), ('MITICO',   'RAR', 50.00), ('MITICO',   'LEN', 40.00),
('LENDARIO', 'BAS',  0.00), ('LENDARIO', 'RAR', 30.00), ('LENDARIO', 'LEN', 70.00)
ON CONFLICT (tier_pacote, raridade) DO NOTHING;

-- 3. Inserção das Cartas (Sincronizado com o projeto Voltante)
INSERT INTO cartas (id_carta, nome, raridade) VALUES
-- Elemento Água
('11111111-1111-1111-1111-000000000001', 'Voltante Azul',          'BAS'),
('11111111-1111-1111-1111-000000000002', 'Voltante de Praia',      'RAR'),
('11111111-1111-1111-1111-000000000003', 'Voltante Mestre D''água', 'LEN'),

-- Elemento Fogo
('22222222-2222-2222-2222-000000000001', 'Voltante Vermelho',       'BAS'),
('22222222-2222-2222-2222-000000000002', 'Voltante Vulcânico',      'RAR'),
('22222222-2222-2222-2222-000000000003', 'Voltante Mestre do Fogo', 'LEN'),

-- Elemento Grama
('33333333-3333-3333-3333-000000000001', 'Voltante Verde',           'BAS'),
('33333333-3333-3333-3333-000000000002', 'Voltante Jardineiro',      'RAR'),
('33333333-3333-3333-3333-000000000003', 'Voltante Mestre da Grama', 'LEN'),

-- Elemento Elétrico
('44444444-4444-4444-4444-000000000001', 'Voltante Amarelo',        'BAS'),
('44444444-4444-4444-4444-000000000002', 'Voltante Tempestade',     'RAR'),
('44444444-4444-4444-4444-000000000003', 'Voltante Mestre dos Raios','LEN')
ON CONFLICT (id_carta) DO NOTHING;