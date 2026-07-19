-- Inserção das cartas do elemento Água
INSERT INTO "cartas" (carta_id, nome, tipo, raridade, vida, descricao, imagem_url) VALUES
('11111111-1111-1111-1111-000000000001', 'Voltante Azul', 'Agua', 'Básica', 100, 'Essa cobra é azul e básica, mas ela tem poderes de água irados.', 'https://drive.google.com/uc?export=view&id=15cvxFDQ3TXIiskglzwN7Cp3Bj-qDMuL9'),
('11111111-1111-1111-1111-000000000002', 'Voltante de Praia', 'Agua', 'Rara', 250, 'Essa cobra está curtindo as férias na praia. Cuidado com as ondas, elas são radicais!', 'https://drive.google.com/uc?export=view&id=1dNWDotfD5ZzlRfFlgVPgiXKksAaIaoPe'),
('11111111-1111-1111-1111-000000000003', 'Voltante Mestre D''água', 'Agua', 'Lendária', 500, 'Essa cobra manipula a água como um verdadeiro mestre.', 'https://drive.google.com/uc?export=view&id=1POxOUIddhw4sAbG6gyr9G1HyqCi--RI0')
ON CONFLICT (carta_id) DO NOTHING;

-- Inserção das cartas do elemento Fogo
INSERT INTO "cartas" (carta_id, nome, tipo, raridade, vida, descricao, imagem_url) VALUES
('22222222-2222-2222-2222-000000000001', 'Voltante Vermelho', 'Fogo', 'Básica', 100, 'Essa cobra é vermelha e básica, mas ela tem poderes de fogo irados.', 'https://drive.google.com/uc?export=view&id=1EjHp_wOVh2TRPQ-ubc43rGOoIyM1qlP8'),
('22222222-2222-2222-2222-000000000002', 'Voltante Vulcânico', 'Fogo', 'Rara', 250, 'Essa cobra reside no fundo de vulcões, a lava é seu spa natural.', 'https://drive.google.com/uc?export=view&id=1xVvP122NY7bJEl6zZ3jo1NWiZQSsqEeb'),
('22222222-2222-2222-2222-000000000003', 'Voltante Mestre do Fogo', 'Fogo', 'Lendária', 500, 'Essa cobra é capaz de queimar tudo que alcança seu olhar.', 'https://drive.google.com/uc?export=view&id=11eKRVwynS7YmrJNfX5VAWtVyH_xzgDNq')
ON CONFLICT (carta_id) DO NOTHING;

-- Inserção das cartas do elemento Grama
INSERT INTO "cartas" (carta_id, nome, tipo, raridade, vida, descricao, imagem_url) VALUES
('33333333-3333-3333-3333-000000000001', 'Voltante Verde', 'Grama', 'Básica', 100, 'Essa cobra é verde e básica, mas ela tem poderes de grama irados.', 'https://drive.google.com/uc?export=view&id=1xfJ0ibdf56P7K7pWAAVOSIIwpCRFgDcb'),
('33333333-3333-3333-3333-000000000002', 'Voltante Jardineiro', 'Grama', 'Rara', 250, 'Essa cobra cuida das flores em seu jardim, elas podem crescer e ser uma ameaça.', 'https://drive.google.com/uc?export=view&id=19TVAc-cL2KrKvRMgfblvBiynOCooWyeQ'),
('33333333-3333-3333-3333-000000000003', 'Voltante Mestre da Grama', 'Grama', 'Lendária', 500, 'Essa cobra conhece os vegetais e sente tudo que toca o solo com as vibrações.', 'https://drive.google.com/uc?export=view&id=1ti-rCmW_iOZ6jRaWMdP2k0oH7NJfBXls')
ON CONFLICT (carta_id) DO NOTHING;

-- Inserção das cartas do elemento Elétrico
INSERT INTO "cartas" (carta_id, nome, tipo, raridade, vida, descricao, imagem_url) VALUES
('44444444-4444-4444-4444-000000000001', 'Voltante Amarelo', 'Eletrico', 'Básica', 100, 'Essa cobra é amarela e básica, mas ela tem poderes de raio irados.', 'https://drive.google.com/uc?export=view&id=1gziakcIn6dAckbdK1l1OyjIPCuoNh7iz'),
('44444444-4444-4444-4444-000000000002', 'Voltante Tempestade', 'Eletrico', 'Rara', 250, 'Essa cobra surfa nos relâmpagos que caem nas noites tempestuosas.', 'https://drive.google.com/uc?export=view&id=10iGK-MCzqEOHDZh6uLRid7pJ4wPDMxAf'),
('44444444-4444-4444-4444-000000000003', 'Voltante Mestre dos Raios', 'Eletrico', 'Lendária', 500, 'Essas cobras trazem a tempestade, cada uma de suas cabeças possuí um olhar diferente.', 'https://drive.google.com/uc?export=view&id=1ae4QQCeVxIThESAR5IablLSQj05r4xtQ')
ON CONFLICT (carta_id) DO NOTHING;