-- Demonstracao reversivel; nao executada pelo instalador.
SAVEPOINT demonstracao;
UPDATE tb_alerta SET ds_status = 'RESOLVIDO' WHERE cd_alerta = 1;
INSERT INTO tb_leitura VALUES (sq_tb_leitura.NEXTVAL, 8, TO_DATE('2026-10-03 06:00', 'YYYY-MM-DD HH24:MI'), 99999);
DELETE FROM tb_leitura WHERE cd_medidor = 8 AND dt_leitura = TO_DATE('2026-10-03 06:00', 'YYYY-MM-DD HH24:MI');
ROLLBACK TO demonstracao;
