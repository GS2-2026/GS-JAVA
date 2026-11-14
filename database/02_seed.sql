-- Dados demonstrativos do banco fornecido: 60 registros. Executar uma unica vez.
-- tb_comunidade (4)
INSERT INTO tb_comunidade VALUES (1, 'Vila Esperanca',  'Sao Paulo',     'SP', 'Associacao de Moradores da Vila Esperanca');
INSERT INTO tb_comunidade VALUES (2, 'Jardim das Flores', 'Guarulhos',   'SP', 'Associacao Jardim das Flores');
INSERT INTO tb_comunidade VALUES (3, 'Morro Alto',      'Osasco',        'SP', 'Uniao de Moradores do Morro Alto');
INSERT INTO tb_comunidade VALUES (4, 'Recanto Verde',   'Santo Andre',   'SP', 'Associacao Recanto Verde');

-- tb_residencia (10) - a tb_comunidade 4 ainda nao tem residencias cadastradas
INSERT INTO tb_residencia VALUES (1,  1, 'RES-001', 'Rua das Acacias, 10',      4);
INSERT INTO tb_residencia VALUES (2,  1, 'RES-002', 'Rua das Acacias, 24',      3);
INSERT INTO tb_residencia VALUES (3,  1, 'RES-003', 'Beco do Sol, 5',           5);
INSERT INTO tb_residencia VALUES (4,  2, 'RES-004', 'Rua das Margaridas, 100',  2);
INSERT INTO tb_residencia VALUES (5,  2, 'RES-005', 'Rua das Margaridas, 150',  4);
INSERT INTO tb_residencia VALUES (6,  2, 'RES-006', 'Travessa das Rosas, 8',    6);
INSERT INTO tb_residencia VALUES (7,  3, 'RES-007', 'Ladeira do Morro, 33',     3);
INSERT INTO tb_residencia VALUES (8,  3, 'RES-008', 'Ladeira do Morro, 47',     4);
INSERT INTO tb_residencia VALUES (9,  3, 'RES-009', 'Viela da Fonte, 2',        2);
INSERT INTO tb_residencia VALUES (10, 3, 'RES-010', 'Viela da Fonte, 19',       5);

-- tb_medidor (10) - um por tb_residencia; os medidores 9 e 10 nao tem leituras
INSERT INTO tb_medidor VALUES (1,  1,  'HID-2025-0001', TO_DATE('2025-03-10', 'YYYY-MM-DD'), 'ATIVO');
INSERT INTO tb_medidor VALUES (2,  2,  'HID-2025-0002', TO_DATE('2025-03-10', 'YYYY-MM-DD'), 'ATIVO');
INSERT INTO tb_medidor VALUES (3,  3,  'HID-2025-0003', TO_DATE('2025-03-12', 'YYYY-MM-DD'), 'ATIVO');
INSERT INTO tb_medidor VALUES (4,  4,  'HID-2025-0004', TO_DATE('2025-05-20', 'YYYY-MM-DD'), 'ATIVO');
INSERT INTO tb_medidor VALUES (5,  5,  'HID-2025-0005', TO_DATE('2025-05-20', 'YYYY-MM-DD'), 'ATIVO');
INSERT INTO tb_medidor VALUES (6,  6,  'HID-2025-0006', TO_DATE('2025-05-22', 'YYYY-MM-DD'), 'ATIVO');
INSERT INTO tb_medidor VALUES (7,  7,  'HID-2025-0007', TO_DATE('2025-08-05', 'YYYY-MM-DD'), 'ATIVO');
INSERT INTO tb_medidor VALUES (8,  8,  'HID-2025-0008', TO_DATE('2025-08-05', 'YYYY-MM-DD'), 'ATIVO');
INSERT INTO tb_medidor VALUES (9,  9,  'HID-2025-0009', TO_DATE('2025-08-07', 'YYYY-MM-DD'), 'INATIVO');
INSERT INTO tb_medidor VALUES (10, 10, 'HID-2025-0010', TO_DATE('2025-08-07', 'YYYY-MM-DD'), 'MANUTENCAO');

-- tb_leitura (20) - consumo em litros desde a tb_leitura anterior
INSERT INTO tb_leitura VALUES (1,  1, TO_DATE('2026-10-01 06:00', 'YYYY-MM-DD HH24:MI'),  580.00);
INSERT INTO tb_leitura VALUES (2,  1, TO_DATE('2026-10-02 06:00', 'YYYY-MM-DD HH24:MI'), 1850.00);
INSERT INTO tb_leitura VALUES (3,  1, TO_DATE('2026-10-03 06:00', 'YYYY-MM-DD HH24:MI'), 2350.00);
INSERT INTO tb_leitura VALUES (4,  2, TO_DATE('2026-10-01 06:00', 'YYYY-MM-DD HH24:MI'),  450.00);
INSERT INTO tb_leitura VALUES (5,  2, TO_DATE('2026-10-02 06:00', 'YYYY-MM-DD HH24:MI'),  470.00);
INSERT INTO tb_leitura VALUES (6,  2, TO_DATE('2026-10-03 06:00', 'YYYY-MM-DD HH24:MI'),  465.25);
INSERT INTO tb_leitura VALUES (7,  3, TO_DATE('2026-10-01 06:00', 'YYYY-MM-DD HH24:MI'),  720.00);
INSERT INTO tb_leitura VALUES (8,  3, TO_DATE('2026-10-02 06:00', 'YYYY-MM-DD HH24:MI'),  735.00);
INSERT INTO tb_leitura VALUES (9,  3, TO_DATE('2026-10-03 06:00', 'YYYY-MM-DD HH24:MI'),    0.00);
INSERT INTO tb_leitura VALUES (10, 4, TO_DATE('2026-10-01 06:00', 'YYYY-MM-DD HH24:MI'),  280.00);
INSERT INTO tb_leitura VALUES (11, 4, TO_DATE('2026-10-02 06:00', 'YYYY-MM-DD HH24:MI'),  480.00);
INSERT INTO tb_leitura VALUES (12, 5, TO_DATE('2026-10-01 06:00', 'YYYY-MM-DD HH24:MI'),  590.00);
INSERT INTO tb_leitura VALUES (13, 5, TO_DATE('2026-10-02 06:00', 'YYYY-MM-DD HH24:MI'), 3100.00);
INSERT INTO tb_leitura VALUES (14, 6, TO_DATE('2026-10-01 06:00', 'YYYY-MM-DD HH24:MI'),  880.00);
INSERT INTO tb_leitura VALUES (15, 6, TO_DATE('2026-10-02 06:00', 'YYYY-MM-DD HH24:MI'),  905.00);
INSERT INTO tb_leitura VALUES (16, 7, TO_DATE('2026-10-01 06:00', 'YYYY-MM-DD HH24:MI'),  430.00);
INSERT INTO tb_leitura VALUES (17, 7, TO_DATE('2026-10-02 06:00', 'YYYY-MM-DD HH24:MI'), 1750.00);
INSERT INTO tb_leitura VALUES (18, 7, TO_DATE('2026-10-03 06:00', 'YYYY-MM-DD HH24:MI'), 1980.00);
INSERT INTO tb_leitura VALUES (19, 8, TO_DATE('2026-10-01 06:00', 'YYYY-MM-DD HH24:MI'),  540.00);
INSERT INTO tb_leitura VALUES (20, 8, TO_DATE('2026-10-02 06:00', 'YYYY-MM-DD HH24:MI'),  560.00);

-- tb_tipo_alerta (4) - o tipo 4 ainda nao foi disparado
INSERT INTO tb_tipo_alerta VALUES (1, 'VAZAMENTO_SUSPEITO',    'Consumo elevado e continuo que indica possivel vazamento na tb_residencia', 'ALTA');
INSERT INTO tb_tipo_alerta VALUES (2, 'CONSUMO_ACIMA_MEDIA',   'Consumo muito acima da media por morador da tb_comunidade',                 'MEDIA');
INSERT INTO tb_tipo_alerta VALUES (3, 'LEITURA_ZERADA',        'tb_medidor registrou consumo zero, possivel falha ou ligacao interrompida', 'BAIXA');
INSERT INTO tb_tipo_alerta VALUES (4, 'PICO_NOTURNO',          'Consumo relevante em horario de madrugada, quando ninguem deveria usar agua', 'ALTA');

-- tb_alerta (5)
INSERT INTO tb_alerta VALUES (1, 1, TO_DATE('2026-10-03', 'YYYY-MM-DD'), 'ABERTO',     'tb_medidor HID-2025-0001 com consumo alto por dois dias seguidos');
INSERT INTO tb_alerta VALUES (2, 2, TO_DATE('2026-10-02', 'YYYY-MM-DD'), 'EM_ANALISE', 'tb_residencia RES-005 com consumo muito acima da media por morador');
INSERT INTO tb_alerta VALUES (3, 3, TO_DATE('2026-10-03', 'YYYY-MM-DD'), 'RESOLVIDO',  'tb_medidor HID-2025-0003 registrou zero litros; falha no sensor corrigida');
INSERT INTO tb_alerta VALUES (4, 2, TO_DATE('2026-10-03', 'YYYY-MM-DD'), 'ABERTO',     'tb_residencia RES-007 acima da media em duas leituras consecutivas');
INSERT INTO tb_alerta VALUES (5, 1, TO_DATE('2026-10-02', 'YYYY-MM-DD'), 'EM_ANALISE', 'Pico de 3100 litros em RES-005 sugere vazamento');

-- tb_alerta_leitura (7) - N:N entre tb_alerta e tb_leitura
INSERT INTO tb_alerta_leitura VALUES (1, 2);
INSERT INTO tb_alerta_leitura VALUES (1, 3);
INSERT INTO tb_alerta_leitura VALUES (2, 13);
INSERT INTO tb_alerta_leitura VALUES (3, 9);
INSERT INTO tb_alerta_leitura VALUES (4, 17);
INSERT INTO tb_alerta_leitura VALUES (4, 18);
INSERT INTO tb_alerta_leitura VALUES (5, 13);

COMMIT;


