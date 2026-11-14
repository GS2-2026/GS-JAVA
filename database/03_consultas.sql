-- CONSULTA 1 (WHERE + ORDER BY)
-- Quais residencias tem mais de 3 moradores? (maiores primeiro)
SELECT ds_codigo_residencia, ds_endereco, qt_moradores
  FROM tb_residencia
 WHERE qt_moradores > 3
 ORDER BY qt_moradores DESC, ds_codigo_residencia;

-- CONSULTA 2 (INNER JOIN + calculo no SELECT)
-- Quantos litros por pessoa cada tb_leitura representa? (so leituras com consumo)
SELECT r.ds_codigo_residencia,
       l.dt_leitura,
       l.vl_consumo_litros,
       r.qt_moradores,
       ROUND(l.vl_consumo_litros / r.qt_moradores, 1) AS litros_por_pessoa
  FROM tb_leitura l
 INNER JOIN tb_medidor m    ON m.cd_medidor = l.cd_medidor
 INNER JOIN tb_residencia r ON r.cd_residencia = m.cd_residencia
 WHERE l.vl_consumo_litros > 0
 ORDER BY litros_por_pessoa DESC;

-- CONSULTA 3 (LEFT JOIN + COUNT + GROUP BY)
-- Quantas residencias cada tb_comunidade tem? (inclui comunidades sem tb_residencia)
SELECT c.nm_nome AS tb_comunidade,
       COUNT(r.cd_residencia) AS total_residencias
  FROM tb_comunidade c
  LEFT JOIN tb_residencia r ON r.cd_comunidade = c.cd_comunidade
 GROUP BY c.nm_nome
 ORDER BY total_residencias DESC, c.nm_nome;

-- CONSULTA 4 (COUNT, SUM, AVG, MAX, MIN + GROUP BY)
-- Qual o resumo de consumo de agua de cada tb_comunidade?
SELECT c.nm_nome AS tb_comunidade,
       COUNT(l.cd_leitura)            AS qtd_leituras,
       SUM(l.vl_consumo_litros)          AS total_litros,
       ROUND(AVG(l.vl_consumo_litros),1) AS media_litros,
       MAX(l.vl_consumo_litros)          AS maior_leitura,
       MIN(l.vl_consumo_litros)          AS menor_leitura
  FROM tb_comunidade c
 INNER JOIN tb_residencia r ON r.cd_comunidade = c.cd_comunidade
 INNER JOIN tb_medidor m    ON m.cd_residencia = r.cd_residencia
 INNER JOIN tb_leitura l    ON l.cd_medidor = m.cd_medidor
 GROUP BY c.nm_nome
 ORDER BY total_litros DESC;

-- CONSULTA 5 (GROUP BY + HAVING)
-- Quais residencias tem consumo medio por tb_leitura acima de 700 litros?
SELECT r.ds_codigo_residencia,
       ROUND(AVG(l.vl_consumo_litros), 1) AS media_litros
  FROM tb_residencia r
 INNER JOIN tb_medidor m ON m.cd_residencia = r.cd_residencia
 INNER JOIN tb_leitura l ON l.cd_medidor = m.cd_medidor
 GROUP BY r.ds_codigo_residencia
HAVING AVG(l.vl_consumo_litros) > 700
 ORDER BY media_litros DESC;

-- CONSULTA 6 (subconsulta)
-- Quais leituras estao acima da media geral de consumo?
SELECT cd_leitura, cd_medidor, dt_leitura, vl_consumo_litros
  FROM tb_leitura
 WHERE vl_consumo_litros > (SELECT AVG(vl_consumo_litros) FROM tb_leitura)
 ORDER BY vl_consumo_litros DESC;

-- CONSULTA 7 (RIGHT JOIN + COUNT)
-- Quantos alertas cada tipo ja gerou? (inclui tipos que nunca foram disparados)
SELECT t.nm_nome AS tb_tipo_alerta,
       t.ds_severidade,
       COUNT(a.cd_alerta) AS total_alertas
  FROM tb_alerta a
 RIGHT JOIN tb_tipo_alerta t ON t.cd_tipo_alerta = a.cd_tipo_alerta
 GROUP BY t.nm_nome, t.ds_severidade
 ORDER BY total_alertas DESC, t.nm_nome;

-- CONSULTA 8 (LEFT JOIN + IS NULL)
-- Quais medidores nao enviaram nenhuma tb_leitura?
SELECT m.ds_numero_serie, m.ds_status, r.ds_codigo_residencia
  FROM tb_medidor m
  LEFT JOIN tb_leitura l     ON l.cd_medidor = m.cd_medidor
 INNER JOIN tb_residencia r  ON r.cd_residencia = m.cd_residencia
 WHERE l.cd_leitura IS NULL
 ORDER BY m.ds_numero_serie;

-- CONSULTA 9 (varios INNER JOIN, passando pela tabela associativa)
-- Quais alertas ainda nao foram resolvidos e que leituras os dispararam?
SELECT a.cd_alerta,
       t.nm_nome AS tb_tipo_alerta,
       t.ds_severidade,
       a.ds_status,
       c.nm_nome AS tb_comunidade,
       r.ds_codigo_residencia,
       l.dt_leitura,
       l.vl_consumo_litros
  FROM tb_alerta a
 INNER JOIN tb_tipo_alerta t    ON t.cd_tipo_alerta = a.cd_tipo_alerta
 INNER JOIN tb_alerta_leitura al ON al.cd_alerta = a.cd_alerta
 INNER JOIN tb_leitura l        ON l.cd_leitura = al.cd_leitura
 INNER JOIN tb_medidor m        ON m.cd_medidor = l.cd_medidor
 INNER JOIN tb_residencia r     ON r.cd_residencia = m.cd_residencia
 INNER JOIN tb_comunidade c     ON c.cd_comunidade = r.cd_comunidade
 WHERE a.ds_status <> 'RESOLVIDO'
 ORDER BY a.cd_alerta, l.dt_leitura;

-- CONSULTA 10 (subconsulta no FROM + CASE + GROUP BY)
-- Quantas leituras sao normais, elevadas ou criticas, pelo consumo por pessoa?
SELECT classificacao,
       COUNT(*) AS qtd_leituras
  FROM (SELECT l.cd_leitura,
               CASE
                   WHEN l.vl_consumo_litros / r.qt_moradores > 400 THEN 'CRITICO'
                   WHEN l.vl_consumo_litros / r.qt_moradores > 200 THEN 'ELEVADO'
                   ELSE 'NORMAL'
               END AS classificacao
          FROM tb_leitura l
         INNER JOIN tb_medidor m    ON m.cd_medidor = l.cd_medidor
         INNER JOIN tb_residencia r ON r.cd_residencia = m.cd_residencia)
 GROUP BY classificacao
 ORDER BY qtd_leituras DESC;
