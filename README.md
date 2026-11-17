# EcoComunidade API

API de monitoramento de consumo de agua para a Global Solution FIAP. Java 21, Spring Boot 3.5.7, Oracle JDBC, Bean Validation e ModelMapper. Persistencia por DAO com DataSource e PreparedStatement; sem JPA/Hibernate.

## Integrantes

| Nome | RM | Turma |
| --- | --- | --- |
| Lucas Fortunato Brandão de Pinho | 572860 | 1TDSPI |
| Lucas Costa | 571016 | 1TDSPI |
| Henrique Soares Serra | 573618 | 1TDSPI |
| Arthur Chang Skolimoski | 572510 | 1TDSPH |

## Links da entrega

- API publicada: [EcoComunidade no Render](https://ecocomunidade-api.onrender.com).
- Consulta de residencias: [GET /api/residencias](https://ecocomunidade-api.onrender.com/api/residencias).
- Front-end: pendente de integracao e disponibilizacao do link.
- Video de demonstracao: pendente de gravacao e disponibilizacao do link.

A raiz `/` nao possui endpoint e retorna 404. Os recursos estao sob `/api`.

## Rodar localmente

Requisitos: JDK 21+ e Maven 3.9+. O script `scripts/run.ps1` utiliza JAVA_HOME ou detecta um JDK em `.jdks`; o Maven pode estar no PATH ou em `.tools`.

```powershell
.\scripts\run.ps1 -Test
.\scripts\run.ps1
```

Com Maven instalado: `mvn test` e `mvn spring-boot:run`. API em `http://localhost:8080/api`.

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
4. `database/04_crud_exemplo.sql`: demonstracao com rollback, opcional.

Execucao pelo SQL Developer conectado ao Oracle da FIAP ou pelos scripts locais, apos resolucao das dependencias Maven:

```powershell
$env:JAVA_HOME = 'C:\caminho\do\jdk'
.\scripts\database.ps1         # testa conexao e consulta objetos
.\scripts\database.ps1 -Apply  # cria schema e insere seed
```

O instalador interrompe se algum objeto do projeto ja existir. Nao faz DROP. Oracle confirma DDL implicitamente; falhas parciais exigem revisao do schema. A inicializacao da API nao cria nem apaga tabelas automaticamente.

Bases existentes preservam seus IDs e sequences: a versao anterior iniciava em 1000 (100 para tipos de alerta). Um ID como 1002 e valido e nao representa a quantidade de registros. Nao reiniciar sequences nem renumerar registros existentes, pois IDs ja emitidos podem estar referenciados. Lacunas por exclusao ou rollback sao normais; a API continua usando NEXTVAL para evitar colisoes entre cadastros concorrentes. A alteracao do schema vale para novas instalacoes, sem modificar o Oracle ja instalado.

`dataHoraLeitura` usa horario local sem sufixo de fuso (`LocalDateTime`), e `dataInstalacao` usa somente a data (`LocalDate`). O frontend deve preservar esses valores sem converte-los para UTC.

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

Validacao realizada em 14/11/2026: conexao autenticada ao Oracle FIAP, schema instalado, 60 registros conferidos, tres testes de integracao aprovados e JAR gerado. O script `scripts/smoke.ps1` tambem confirmou GET, POST, PUT e DELETE reais das tres entidades pelo servidor local, incluindo rotas aninhadas; removeu os registros temporarios ao final.

O teste HTTP real e executado por `.\scripts\smoke.ps1`, com a API ativa e a comunidade de exemplo de ID 1. IDs consumidos pelas sequences nao sao reutilizados.

Validacao remota em 15/11/2026: o resultado do script executado contra a API publicada no Render confirmou GET, POST, PUT e DELETE de residencias, medidores e leituras no Oracle, incluindo as rotas aninhadas. As respostas foram 200 para consultas e atualizacoes, 201 para cadastros e 204 para exclusoes. Os registros temporarios foram removidos ao final.

Comando do teste remoto, sem necessidade de iniciar a API local:

```powershell
.\scripts\smoke.ps1 -BaseUrl "https://ecocomunidade-api.onrender.com/api"
```

## Deploy e proximas entregas

API publicada no Render como Web Service Docker, com Dockerfile multi-stage. A configuracao utiliza DB_USERNAME, DB_PASSWORD e CORS_ALLOWED_ORIGINS; DB_URL pode ser sobrescrita por ambiente. A comunicacao do servico publicado com o Oracle FIAP foi validada pelo teste remoto.

A documentacao tecnica esta em [output/pdf/EcoComunidade_Documentacao_Tecnica.pdf](output/pdf/EcoComunidade_Documentacao_Tecnica.pdf), com integrantes, diagramas, endpoints, regras e execucao.

Pendencias para a entrega:

- Integracao com o React, com consulta e operacao de escrita reais.
- Gravacao e disponibilizacao do video de ate cinco minutos.
- Inclusao dos links do front-end e do video no README.
- Atualizacao do PDF com a URL publicada da API, a validacao remota e os links finais da entrega.
- Montagem do ZIP final sem target, .tools e credenciais locais.

Referencias tecnicas: [Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Oracle JDBC](https://docs.oracle.com/en/database/oracle/oracle-database/26/jjdbc/data-sources-and-URLs.html).
