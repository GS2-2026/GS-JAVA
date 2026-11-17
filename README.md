# EcoComunidade API

API de monitoramento de consumo de agua para a Global Solution FIAP. Java 21, Spring Boot 3.5.7, Oracle JDBC, Bean Validation e ModelMapper. Persistencia por DAO com DataSource e PreparedStatement; sem JPA/Hibernate.

## Integrantes

| Nome | RM | Turma |
| --- | --- | --- |
| Lucas Fortunato Brandão de Pinho | 572860 | 1TDSPI |
| Lucas Costa | 571016 | 1TDSPI |
| Henrique Soares Serra | 573618 | 1TDSPI |
| Arthur Chang Skolimoski | 572510 | 1TDSPK |

## Links da entrega

- API publicada: [EcoComunidade no Render](https://ecocomunidade-api.onrender.com).
- Consulta de residencias: [GET /api/residencias](https://ecocomunidade-api.onrender.com/api/residencias).
- Front-end publicado: [EcoComunidade no Vercel](https://front-two-self.vercel.app).
- Repositorio do front-end: [GS2-2026/Front](https://github.com/GS2-2026/Front).
- Video de demonstracao: [assistir no Google Drive](https://drive.google.com/file/d/1MfjANvEr8DI3tfJ3YRQbDPAR3N0D7MZ_/view?usp=sharing).

A raiz `/` nao possui endpoint e retorna 404. Os recursos estao sob `/api`.

## Rodar localmente

Requisitos: JDK 21+ e Maven 3.9+ instalados, com JAVA_HOME configurado e mvn disponivel no PATH.

```shell
mvn test
mvn spring-boot:run
```

API em `http://localhost:8080/api`. Para gerar o JAR: `mvn clean package`.

As credenciais sao configuradas pelas variaveis DB_USERNAME e DB_PASSWORD ou pelo arquivo local `config/application-local.properties`, ignorado pelo Git e pelo Docker. A estrutura esta em `config/application-local.properties.example`.

| Variavel | Uso |
| --- | --- |
| DB_URL | Padrao `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` (SID ORCL) |
| DB_USERNAME | Usuario Oracle, sem valor no codigo versionado |
| DB_PASSWORD | Senha Oracle, sem valor no codigo versionado |
| PORT | Porta HTTP; padrao 8080 |
| CORS_ALLOWED_ORIGINS | Origens do front separadas por virgula; padrao `http://localhost:5173` |

## Banco de dados

Modelo adaptado do SQL de agua fornecido em `BD/GS-DATABASE-main.zip`. Nao modifica as tabelas antigas sem prefixo. Sete tabelas, seis sequences, PK/FK, CHECK, UNIQUE, indices e 60 registros demonstrativos. Em instalacoes novas, IDs continuam apos o seed: comunidade 5, residencia 11, medidor 11, leitura 21, tipo de alerta 5 e alerta 6. Executar apenas uma vez, em ordem:

1. `database/01_schema.sql`: tabelas e sequences.
2. `database/02_seed.sql`: dados demonstrativos.
3. `database/03_consultas.sql`: dez consultas, somente leitura.

Execute os arquivos pelo SQL Developer conectado ao Oracle da FIAP. A criacao e a carga inicial devem ocorrer somente em um schema novo, sem objetos do projeto. Nao repita esses scripts no banco ja instalado. Oracle confirma DDL implicitamente; falhas parciais exigem revisao do schema. A inicializacao da API nao cria nem apaga tabelas automaticamente.

Bases existentes preservam seus IDs e sequences: a versao anterior iniciava em 1000 (100 para tipos de alerta). Um ID como 1002 e valido e nao representa a quantidade de registros. Nao reiniciar sequences nem renumerar registros existentes, pois IDs ja emitidos podem estar referenciados. Lacunas por exclusao ou rollback sao normais; a API continua usando NEXTVAL para evitar colisoes entre cadastros concorrentes. A alteracao do schema vale para novas instalacoes, sem modificar o Oracle ja instalado.

`dataHoraLeitura` usa horario local sem sufixo de fuso (`LocalDateTime`), e `dataInstalacao` usa somente a data (`LocalDate`). O frontend preserva esses valores sem converte-los para UTC. A validacao de datas passadas ou atuais utiliza o relogio de America/Sao_Paulo, independentemente do fuso do servidor.

Verificacao em 17/11/2026: o front envia o horario digitado por `dataHoraParaApi`, sem `toISOString()` na gravacao. Os testes locais confirmaram a preservacao de `21:15:00` e a rejeicao de horario futuro em Sao Paulo mesmo com o servidor configurado em UTC. O contrato considera horarios locais de Sao Paulo.

## Contrato HTTP

Para cada recurso `residencias`, `medidores`, `leituras`:

| Metodo | Rota | Resposta |
| --- | --- | --- |
| POST | `/api/{recurso}` | 201, corpo e Location |
| GET | `/api/{recurso}` | 200, lista ordenada por ID |
| GET | `/api/{recurso}/{id}` | 200 ou 404 |
| PUT | `/api/{recurso}/{id}` | 200, substitui os campos editaveis |
| DELETE | `/api/{recurso}/{id}` | 204, 404 ou 409 se houver dependencias |

Tambem ha GET e POST em `/api/residencias/{id}/medidores` e `/api/medidores/{id}/leituras`. O ID do pai no JSON deve ser igual ao da rota. Erros: 400 para dados invalidos; 404 para recurso inexistente; 405 para metodo nao suportado; 409 para duplicidade, dependencias ou regra de negocio; 503 para indisponibilidade do banco.

Exemplos de POST (a resposta acrescenta `id`):

```json
{"idComunidade":1,"codigoResidencia":"RES-NOVA","endereco":"Rua das Flores, 10","qtdMoradores":4}
```

```json
{"idResidencia":1,"numeroSerie":"HID-NOVO-001","dataInstalacao":"2026-01-01","status":"ATIVO"}
```

```json
{"idMedidor":1,"dataHoraLeitura":"2026-11-01T06:00:00","consumoLitros":580.50}
```

Exemplo de erro:

```json
{"timestamp":"2026-11-14T23:00:00Z","status":409,"mensagem":"Apenas medidores ativos podem receber leituras.","caminho":"/api/leituras","detalhes":[]}
```

## Regras de negocio

O campo consumoLitros representa consumo do periodo em litros. Valores podem diminuir entre periodos; a API nao calcula diferencas entre leituras acumuladas e nao registra energia.

- Somente medidor ATIVO recebe cadastro/alteracao de leitura.
- Leitura nao pode anteceder a instalacao nem estar no futuro.
- Nao pode haver duas leituras do mesmo medidor no mesmo instante.
- Consumo nao negativo, ate seis digitos inteiros e duas casas decimais.
- Medidor com historico nao muda de residencia; instalacao nao pode ultrapassar leituras existentes.
- Remocao de registros com filhos retorna 409; nao ha exclusao em cascata.
- Validacao e escrita de leitura usam transacao e bloqueio do medidor para coordenar alteracoes concorrentes na API.

## Estrutura e verificacao

`model`: entidades; `dto`: entrada/saida; `dao`: JDBC; `service`: regras e transacoes; `controller`: HTTP; `exception`/`handler`: erros JSON; `config`: ModelMapper e CORS.

Os testes de integracao usam H2 em modo Oracle, carregam os mesmos scripts de schema/seed e exercitam CRUD, DTOs, relacionamentos, integridade, regras, CORS e status HTTP. H2 e apenas para testes e nao substitui a verificacao real no Oracle.

Validacao realizada em 14/11/2026: conexao autenticada ao Oracle FIAP, schema instalado, 60 registros conferidos, tres testes de integracao aprovados e JAR gerado. A verificacao HTTP tambem confirmou GET, POST, PUT e DELETE reais das tres entidades pelo servidor local, incluindo rotas aninhadas; os registros temporarios foram removidos ao final.

Para verificar o CRUD real, use Postman ou Insomnia com a API ativa, seguindo os endpoints e exemplos JSON deste documento. Crie registros de teste e remova primeiro as leituras, depois os medidores e por ultimo as residencias. IDs consumidos pelas sequences nao sao reutilizados.

Validacao remota em 15/11/2026: a verificacao contra a API publicada no Render confirmou GET, POST, PUT e DELETE de residencias, medidores e leituras no Oracle, incluindo as rotas aninhadas. As respostas foram 200 para consultas e atualizacoes, 201 para cadastros e 204 para exclusoes. Os registros temporarios foram removidos ao final.

Para testar a API publicada no Postman ou Insomnia, use a base `https://ecocomunidade-api.onrender.com/api`, sem necessidade de iniciar a API local.

## Deploy e entrega

API publicada no Render como Web Service Docker, com Dockerfile multi-stage. A configuracao utiliza DB_USERNAME, DB_PASSWORD e CORS_ALLOWED_ORIGINS; DB_URL pode ser sobrescrita por ambiente. A comunicacao do servico publicado com o Oracle FIAP foi validada pelo teste remoto.

A documentacao tecnica esta em [pdf/EcoComunidade_Documentacao_Tecnica.pdf](pdf/EcoComunidade_Documentacao_Tecnica.pdf), com integrantes, diagramas, endpoints, regras e execucao.

Integracao com o front React no Vercel confirmada pelo grupo. O PDF tecnico foi atualizado em 17/11/2026 com os links publicos, o video de demonstracao informado pelo grupo, o deploy concluido e a validacao do CRUD remoto. O link do video esta incluido neste README e no PDF.

Pendencias para a entrega:

- Publicacao do video no YouTube como nao listado, conforme o enunciado de Java (ate cinco minutos). O link disponibilizado atualmente e do Google Drive.
- Montagem do ZIP final sem target, .tools e credenciais locais.

Referencias tecnicas: [Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Oracle JDBC](https://docs.oracle.com/en/database/oracle/oracle-database/26/jjdbc/data-sources-and-URLs.html).
