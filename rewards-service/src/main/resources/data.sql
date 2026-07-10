insert into rewards (dia_ciclo, tipo_reward, quantidade_moedas_base, tier_pacote_base) values
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
    on conflict (dia_ciclo) do nothing;

insert into pacote_probabilidades (tier_pacote, raridade, porcentagem) values
                                                                           ('BASICO',   'C', 70), ('BASICO',   'B', 25), ('BASICO',   'A', 5),  ('BASICO',   'S', 0),  ('BASICO',   'P', 0),
                                                                           ('NORMAL',   'C', 50), ('NORMAL',   'B', 35), ('NORMAL',   'A', 10), ('NORMAL',   'S', 4),  ('NORMAL',   'P', 1),
                                                                           ('ESPECIAL', 'C', 20), ('ESPECIAL', 'B', 40), ('ESPECIAL', 'A', 30), ('ESPECIAL', 'S', 8),  ('ESPECIAL', 'P', 2),
                                                                           ('EPICO',    'C', 10), ('EPICO',    'B', 30), ('EPICO',    'A', 40), ('EPICO',    'S', 15), ('EPICO',    'P', 5),
                                                                           ('MITICO',   'C', 5),  ('MITICO',   'B', 15), ('MITICO',   'A', 30), ('MITICO',   'S', 35), ('MITICO',   'P', 15),
                                                                           ('LENDARIO', 'C', 0),  ('LENDARIO', 'B', 0),  ('LENDARIO', 'A', 20), ('LENDARIO', 'S', 35), ('LENDARIO', 'P', 45)
    on conflict (tier_pacote, raridade) do nothing;

insert into cartas (id_carta, nome, raridade) values
                                                  ('11111111-0000-0000-0000-000000000001', 'Goblin Comum',        'C'),
                                                  ('11111111-0000-0000-0000-000000000002', 'Rato de Esgoto',      'C'),
                                                  ('11111111-0000-0000-0000-000000000003', 'Soldado Raso',        'C'),
                                                  ('22222222-0000-0000-0000-000000000001', 'Arqueira Élfica',     'B'),
                                                  ('22222222-0000-0000-0000-000000000002', 'Lobo da Neve',        'B'),
                                                  ('33333333-0000-0000-0000-000000000001', 'Mago do Trovão',      'A'),
                                                  ('33333333-0000-0000-0000-000000000002', 'Golem de Pedra',      'A'),
                                                  ('44444444-0000-0000-0000-000000000001', 'Dragão Carmesim',     'S'),
                                                  ('44444444-0000-0000-0000-000000000002', 'Fênix Eterna',        'S'),
                                                  ('55555555-0000-0000-0000-000000000001', 'Jungkook, o Lendário','P'),
                                                  ('55555555-0000-0000-0000-000000000002', 'Titã do Abismo',      'P')
    on conflict (id_carta) do nothing;