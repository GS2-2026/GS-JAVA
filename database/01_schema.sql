-- Oracle FIAP: executar apenas em schema sem estas tabelas. Nenhum DROP e executado.
CREATE TABLE tb_comunidade (
    cd_comunidade          NUMBER(4)     NOT NULL,
    nm_nome                   VARCHAR2(80)  NOT NULL,
    nm_municipio              VARCHAR2(60)  NOT NULL,
    sg_uf                     CHAR(2)       NOT NULL,
    nm_associacao_responsavel VARCHAR2(80)  NOT NULL,
    CONSTRAINT pk_comunidade PRIMARY KEY (cd_comunidade),
    CONSTRAINT uq_comunidade_nome UNIQUE (nm_nome)
);
COMMENT ON TABLE tb_comunidade IS 'Bairro ou conjunto de moradias atendido pela solucao, com a associacao de moradores responsavel.';

CREATE TABLE tb_residencia (
    cd_residencia     NUMBER(6)     NOT NULL,
    cd_comunidade     NUMBER(4)     NOT NULL,
    ds_codigo_residencia VARCHAR2(15)  NOT NULL,
    ds_endereco          VARCHAR2(120) NOT NULL,
    qt_moradores     NUMBER(2)     NOT NULL,
    CONSTRAINT pk_residencia PRIMARY KEY (cd_residencia),
    CONSTRAINT uq_residencia_codigo UNIQUE (ds_codigo_residencia),
    CONSTRAINT fk_residencia_comunidade FOREIGN KEY (cd_comunidade)
        REFERENCES tb_comunidade (cd_comunidade),
    CONSTRAINT ck_residencia_moradores CHECK (qt_moradores > 0)
);
COMMENT ON TABLE tb_residencia IS 'Casa ou unidade monitorada; pertence a uma tb_comunidade (1:N).';

CREATE TABLE tb_medidor (
    cd_medidor      NUMBER(6)    NOT NULL,
    cd_residencia   NUMBER(6)    NOT NULL,
    ds_numero_serie    VARCHAR2(20) NOT NULL,
    dt_instalacao DATE         NOT NULL,
    ds_status          VARCHAR2(10) NOT NULL,
    CONSTRAINT pk_medidor PRIMARY KEY (cd_medidor),
    CONSTRAINT uq_medidor_serie UNIQUE (ds_numero_serie),
    CONSTRAINT fk_medidor_residencia FOREIGN KEY (cd_residencia)
        REFERENCES tb_residencia (cd_residencia),
    CONSTRAINT ck_medidor_status CHECK (ds_status IN ('ATIVO', 'INATIVO', 'MANUTENCAO'))
);
COMMENT ON TABLE tb_medidor IS 'Hidrometro instalado em uma tb_residencia (1:N); registra o consumo de agua.';

CREATE TABLE tb_leitura (
    cd_leitura        NUMBER(8)    NOT NULL,
    cd_medidor        NUMBER(6)    NOT NULL,
    dt_leitura DATE         NOT NULL,
    vl_consumo_litros    NUMBER(8,2)  NOT NULL,
    CONSTRAINT pk_leitura PRIMARY KEY (cd_leitura),
    CONSTRAINT fk_leitura_medidor FOREIGN KEY (cd_medidor)
        REFERENCES tb_medidor (cd_medidor),
    CONSTRAINT ck_leitura_consumo CHECK (vl_consumo_litros >= 0)
);
COMMENT ON TABLE tb_leitura IS 'Medicao de um tb_medidor: litros consumidos desde a tb_leitura anterior (1:N com tb_medidor).';

CREATE TABLE tb_tipo_alerta (
    cd_tipo_alerta NUMBER(3)     NOT NULL,
    nm_nome           VARCHAR2(40)  NOT NULL,
    ds_descricao      VARCHAR2(150) NOT NULL,
    ds_severidade     VARCHAR2(10)  NOT NULL,
    CONSTRAINT pk_tipo_alerta PRIMARY KEY (cd_tipo_alerta),
    CONSTRAINT uq_tipo_alerta_nome UNIQUE (nm_nome),
    CONSTRAINT ck_tipo_alerta_severidade CHECK (ds_severidade IN ('BAIXA', 'MEDIA', 'ALTA'))
);
COMMENT ON TABLE tb_tipo_alerta IS 'Catalogo de anomalias detectaveis (vazamento, consumo acima da media etc.) com sua ds_severidade.';

CREATE TABLE tb_alerta (
    cd_alerta      NUMBER(6)     NOT NULL,
    cd_tipo_alerta NUMBER(3)     NOT NULL,
    dt_geracao   DATE          NOT NULL,
    ds_status         VARCHAR2(12)  NOT NULL,
    ds_descricao      VARCHAR2(200) NOT NULL,
    CONSTRAINT pk_alerta PRIMARY KEY (cd_alerta),
    CONSTRAINT fk_alerta_tipo FOREIGN KEY (cd_tipo_alerta)
        REFERENCES tb_tipo_alerta (cd_tipo_alerta),
    CONSTRAINT ck_alerta_status CHECK (ds_status IN ('ABERTO', 'EM_ANALISE', 'RESOLVIDO'))
);
COMMENT ON TABLE tb_alerta IS 'Ocorrencia de anomalia gerada pela solucao; classificada por um tipo de tb_alerta (1:N).';

CREATE TABLE tb_alerta_leitura (
    cd_alerta  NUMBER(6) NOT NULL,
    cd_leitura NUMBER(8) NOT NULL,
    CONSTRAINT pk_alerta_leitura PRIMARY KEY (cd_alerta, cd_leitura),
    CONSTRAINT fk_al_alerta  FOREIGN KEY (cd_alerta)  REFERENCES tb_alerta (cd_alerta),
    CONSTRAINT fk_al_leitura FOREIGN KEY (cd_leitura) REFERENCES tb_leitura (cd_leitura)
);
COMMENT ON TABLE tb_alerta_leitura IS 'Tabela associativa do N:N entre tb_alerta e tb_leitura: quais leituras dispararam cada tb_alerta.';



ALTER TABLE tb_leitura ADD CONSTRAINT uq_leitura_medidor_data UNIQUE (cd_medidor, dt_leitura);
CREATE INDEX ix_residencia_comunidade ON tb_residencia(cd_comunidade);
CREATE INDEX ix_medidor_residencia ON tb_medidor(cd_residencia);
CREATE INDEX ix_alerta_tipo ON tb_alerta(cd_tipo_alerta);
CREATE INDEX ix_alerta_leitura_leitura ON tb_alerta_leitura(cd_leitura);
CREATE SEQUENCE sq_tb_comunidade START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_residencia START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_medidor START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_leitura START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_tipo_alerta START WITH 100 INCREMENT BY 1 MAXVALUE 999 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_alerta START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
