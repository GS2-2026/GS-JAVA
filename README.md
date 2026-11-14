# EcoComunidade API

API de monitoramento de consumo de agua para a Global Solution FIAP. Java 21, Spring Boot 3.5.7, Oracle JDBC, Bean Validation e ModelMapper. Persistencia por DAO com DataSource e PreparedStatement; sem JPA/Hibernate.

## Integrantes

| Nome | RM | Turma |
| --- | --- | --- |
| Lucas Fortunato Brandão de Pinho | 572860 | 1TDSPI |
| Lucas Costa | 571016 | 1TDSPI |
| Henrique Soares Serra | 573618 | 1TDSPI |
| Arthur Chang Skolimoski | 572510 | 1TDSPH |

## Rodar localmente

Requisitos: JDK 21+ e Maven 3.9+. No computador atual, `scripts/run.ps1` encontra o JDK em `.jdks` e o Maven local em `.tools`. O Java 8 do PATH nao serve para este projeto.

```powershell
.\scripts\run.ps1 -Test
.\scripts\run.ps1
```

Com Maven instalado: `mvn test` e `mvn spring-boot:run`. API em `http://localhost:8080/api`.

As credenciais locais ficam em `config/application-local.properties`, ignorado pelo Git e pelo Docker. Para outra maquina, copie o arquivo `.example` e preencha seus dados, ou defina `DB_USERNAME` e `DB_PASSWORD` no ambiente. Nunca inclua o arquivo local no ZIP da entrega.

| Variavel | Uso |
| --- | --- |
| DB_URL | Padrao `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` (SID ORCL) |
| DB_USERNAME | Usuario Oracle, sem valor no codigo versionado |
| DB_PASSWORD | Senha Oracle, sem valor no codigo versionado |
| PORT | Porta HTTP; padrao 8080 |
| CORS_ALLOWED_ORIGINS | Origens do front separadas por virgula; padrao `http://localhost:5173` |

## Banco de dados

Modelo adaptado do SQL de agua fornecido em `BD/GS-DATABASE-main.zip`. Nao modifica as tabelas antigas sem prefixo. Sete tabelas, seis sequences, PK/FK, CHECK, UNIQUE, indices e 60 registros demonstrativos. IDs novos comecam em 1000; no catalogo de tipos de alerta, em 100 (coluna NUMBER(3)), para evitar colisao com o seed. Executar apenas uma vez, em ordem:

1. `database/01_schema.sql`: tabelas e sequences.
2. `database/02_seed.sql`: dados demonstrativos.
3. `database/03_consultas.sql`: dez consultas, somente leitura.
4. `database/04_crud_exemplo.sql`: demonstracao com rollback, opcional.

Pode executar no SQL Developer conectado ao Oracle da FIAP. Alternativamente, apos baixar as dependencias com os testes:

```powershell
$env:JAVA_HOME = 'C:\caminho\do\jdk'
.\scripts\database.ps1         # testa conexao e consulta objetos
.\scripts\database.ps1 -Apply  # cria schema e insere seed
```

O instalador interrompe se algum objeto do projeto ja existir. Nao faz DROP. Oracle confirma DDL implicitamente: em falha parcial, revise os objetos antes de repetir. O inicio da API nunca cria ou apaga tabelas automaticamente.

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

O SQL entregue pelo grupo usa consumo do periodo em litros, e NAO leitura acumulada. Esta API preserva esse significado: valores podem diminuir entre periodos; nao calcula diferencas entre os consumos. O plano interno sugere leituras acumuladas e energia, o que requer uma evolucao acordada do schema e do contrato antes de implementar.

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

Com a API em execucao, repita com `.\scripts\smoke.ps1`. Ele necessita da comunidade de exemplo de ID 1. IDs consumidos pelas sequences nos testes reais nao sao reutilizados.

## Deploy e proximas entregas

Dockerfile multi-stage pronto. No Render, criar Web Service Docker e configurar DB_USERNAME, DB_PASSWORD e CORS_ALLOWED_ORIGINS. DB_URL pode ser sobrescrita se necessario. A rede do ambiente de deploy precisa alcancar o Oracle FIAP.

A documentacao tecnica pronta esta em [output/pdf/EcoComunidade_Documentacao_Tecnica.pdf](output/pdf/EcoComunidade_Documentacao_Tecnica.pdf), com integrantes, diagramas, endpoints, regras e instrucoes. O projeto nao depende de Python.

Ainda pendentes: publicacao no Render, integracao com o React e video de ate cinco minutos. Atualizar o PDF com os links publicos quando estiverem disponiveis. Nao ha autenticacao de moradores: o usuario informado e a conta Oracle da faculdade. Nao publicar dados pessoais reais nesta etapa.

Referencias tecnicas: [Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Oracle JDBC](https://docs.oracle.com/en/database/oracle/oracle-database/26/jjdbc/data-sources-and-URLs.html).
