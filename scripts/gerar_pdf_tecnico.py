"""Gera a documentação técnica a partir da versão implementada da API."""
from pathlib import Path
import json, re
from reportlab.pdfgen import canvas
from reportlab.lib.colors import HexColor, white
from reportlab.lib.styles import ParagraphStyle
from reportlab.platypus import Paragraph, Table, TableStyle
from reportlab.lib.enums import TA_LEFT

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / 'output/pdf/EcoComunidade_Documentacao_Tecnica.pdf'
OUT.parent.mkdir(parents=True, exist_ok=True)
W,H=595.28,841.89
C=canvas.Canvas(str(OUT),pagesize=(W,H))
C.setTitle('EcoComunidade | Documentação técnica da API Java')
C.setAuthor('Equipe EcoComunidade - FIAP')
NAVY=HexColor('#123448'); GREEN=HexColor('#087F78'); GRAY=HexColor('#526372'); LIGHT=HexColor('#EDF5F5')
page=0; y=0
style=ParagraphStyle('body',fontName='Helvetica',fontSize=10,leading=15,textColor=NAVY,spaceAfter=8)
small=ParagraphStyle('small',parent=style,fontSize=8.4,leading=12)
def p(text, size=None):
    global y
    s=style if size is None else ParagraphStyle('custom',parent=style,fontSize=size,leading=size*1.45)
    item=Paragraph(text,s); _,h=item.wrap(499,700)
    assert y-h>53,(page,text[:60],y,h)
    item.drawOn(C,48,y-h); y-=h+10
def title(text):
    global y
    C.setFillColor(GREEN); C.setFont('Helvetica-Bold',12); C.drawString(48,y,text); y-=24
def start(name, subtitle):
    global page,y
    if page:C.showPage()
    page+=1
    C.setFillColor(GREEN); C.rect(0,H-10,W,10,fill=1,stroke=0)
    C.setFillColor(GRAY); C.setFont('Helvetica-Bold',9); C.drawString(48,H-43,'ECOCOMUNIDADE / API JAVA')
    C.setFillColor(NAVY); C.setFont('Helvetica-Bold',23); C.drawString(48,H-83,name)
    C.setFont('Helvetica',10); C.setFillColor(GRAY); C.drawString(48,H-106,subtitle)
    C.setStrokeColor(HexColor('#D8E3E6')); C.line(48,47,547,47)
    C.setFont('Helvetica',8); C.drawString(48,32,'FIAP | Global Solution 2026/2 | 14 de novembro de 2026')
    C.drawRightString(547,32,f'{page:02}')
    y=H-140
def table(rows,widths):
    global y
    data=[[Paragraph(str(v),small) for v in row] for row in rows]
    t=Table(data,colWidths=widths,hAlign='LEFT')
    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),LIGHT),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7),('LINEBELOW',(0,0),(-1,-1),.4,HexColor('#D8E3E6'))]))
    _,h=t.wrap(499,700); assert y-h>55,(page,'table',h,y)
    t.drawOn(C,48,y-h);y-=h+16
def code(text):
    global y
    lines=text.splitlines(); h=len(lines)*12+18
    assert y-h>55,(page,'code',h,y)
    C.setFillColor(LIGHT);C.roundRect(48,y-h,499,h,5,fill=1,stroke=0)
    C.setFont('Courier',8.5);C.setFillColor(NAVY)
    for i,line in enumerate(lines):
        assert C.stringWidth(line,'Courier',8.5)<475,line
        C.drawString(60,y-16-i*12,line)
    y-=h+14
def box(x,top,w,name,lines):
    h=31+len(lines)*14
    C.setFillColor(LIGHT);C.setStrokeColor(GREEN);C.roundRect(x,top-h,w,h,5,fill=1,stroke=1)
    C.setFillColor(NAVY);C.setFont('Helvetica-Bold',10);C.drawString(x+10,top-19,name)
    C.setFont('Helvetica',8.5)
    for i,line in enumerate(lines):C.drawString(x+10,top-38-i*14,line)
    return top-h
def arrow(x1,y1,x2,y2,label=''):
    C.setStrokeColor(GREEN);C.line(x1,y1,x2,y2)
    C.line(x2,y2,x2-4,y2+6);C.line(x2,y2,x2+4,y2+6)
    if label:C.setFont('Helvetica-Bold',8);C.setFillColor(GREEN);C.drawString(x1+8,(y1+y2)/2,label)

start('Documentação técnica','Domain Driven Design Using Java | Versão 0.0.1-SNAPSHOT')
title('EcoComunidade')
p('API REST para registrar e consultar o consumo de água de residências em comunidades. Persistência no Oracle da FIAP, com Java, Spring Boot e JDBC.',13)
table([['Integrante','RM','Turma'],['Lucas Fortunato Brandão de Pinho','572860','1TDSPI'],['Lucas Costa','571016','1TDSPI'],['Henrique Soares Serra','573618','1TDSPI'],['Arthur Chang Skolimoski','572510','1TDSPH']],[309,90,100])
title('Acesso e situação da entrega')
p('Endereço local: <link href="http://localhost:8080/api/residencias" color="#087F78">http://localhost:8080/api/residencias</link>.<br/>URL pública no Render: <b>pendente de publicação</b>.<br/>Link do vídeo: <b>pendente de gravação e disponibilização</b>.')
p('Este documento descreve a implementação existente. Banco instalado e CRUD validado no Oracle em 14/11/2026. Integração com React e deploy público ainda precisam ser concluídos.')
title('Sumário')
table([['Seção','Página'],['Solução, arquitetura e pacotes','2'],['Diagrama de classes','3'],['Modelo relacional e integridade','4'],['Endpoints e contrato HTTP','5'],['Exemplos JSON de entrada e saída','6'],['Validações, regras e erros','7'],['Execução, configuração e deploy','8'],['Verificação, limites e referências','9']],[425,74])

start('Solução e arquitetura','01 / Contexto, fluxo de dados e responsabilidades')
p('Moradores e associações precisam organizar dados de consumo para acompanhar o histórico das residências. A API centraliza cadastros e registros manuais de água e fornece dados para uma interface web. O projeto se relaciona aos ODS 11 e 13, sem comprovar economia de água apenas pelo cadastro de leituras.')
title('Fluxo de uma requisição')
code('Front-end / cliente HTTP\n    | HTTP + JSON\nController -> DTO validado -> ModelMapper -> Service\n    | regra de negocio + transacao\nDAO -> DataSource / JDBC -> Oracle FIAP\n    |\nResponse DTO -> JSON + status HTTP')
p('O controller recebe DTOs com @Valid. ModelMapper converte DTOs e models. O service aplica regras e delimita transações. Cada DAO recebe DataSource no construtor e usa JdbcTemplate, com PreparedStatement para inserção e parâmetros vinculados nas operações. A resposta expõe DTOs, preservando o contrato HTTP.')
table([['Pacote','Responsabilidade'],['model','Residencia, Medidor e Leitura: atributos do domínio.'],['dto','Requests e Responses separados para cada entidade.'],['controller','Rotas REST, @Valid, status e cabeçalho Location.'],['service','Validação de referências, regras e @Transactional.'],['dao','CRUD JDBC e consultas por relacionamento.'],['exception / handler','Exceções próprias e @RestControllerAdvice com erro JSON.'],['config','Conversão estrita do ModelMapper e CORS para o front.']],[125,374])
p('Tecnologias: Java 21 (alvo de compilação), Spring Boot 3.5.7, Maven, Oracle JDBC, Bean Validation e ModelMapper 3.2.4. A validação local foi executada com JDK 25. H2 é dependência exclusiva de testes.')

start('Diagrama de classes','02 / Models implementados e relacionamentos por identificador')
b=box(100,690,395,'Residencia',['id: Long','idComunidade: Long','codigoResidencia: String','endereco: String','qtdMoradores: Integer'])
arrow(298,b,298,b-44,'1 para 0..N')
b=box(100,b-44,395,'Medidor',['id: Long','idResidencia: Long','numeroSerie: String','dataInstalacao: LocalDate','status: String'])
arrow(298,b,298,b-44,'1 para 0..N')
b=box(100,b-44,395,'Leitura',['id: Long','idMedidor: Long','dataHoraLeitura: LocalDateTime','consumoLitros: BigDecimal'])
y=b-26
p('Os models têm getters e setters. Os vínculos são representados por IDs; não há anotações JPA, coleções ORM ou carregamento automático de entidades relacionadas. Uma comunidade possui residências; uma residência possui medidores; cada medidor possui leituras.')
p('<b>Significado do valor:</b> consumoLitros representa litros consumidos no período informado. Não é o mostrador acumulado do hidrômetro. Valores podem cair entre períodos e a API não calcula a diferença entre registros.')

start('Modelo relacional','03 / DER do schema Oracle e restrições principais')
box(48,690,235,'tb_comunidade',['PK cd_comunidade NUMBER(4)','nm_nome VARCHAR2(80) UNIQUE','nm_municipio VARCHAR2(60)','sg_uf CHAR(2)','nm_associacao_responsavel VARCHAR2(80)'])
box(312,690,235,'tb_tipo_alerta',['PK cd_tipo_alerta NUMBER(3)','nm_nome VARCHAR2(40) UNIQUE','ds_descricao VARCHAR2(150)','ds_severidade VARCHAR2(10)'])
arrow(165,589,165,557,'1:N')
arrow(429,603,429,557,'1:N')
box(48,557,235,'tb_residencia',['PK cd_residencia NUMBER(6)','FK cd_comunidade NUMBER(4)','ds_codigo_residencia VARCHAR2(15) UQ','ds_endereco VARCHAR2(120)','qt_moradores NUMBER(2)'])
box(312,557,235,'tb_alerta',['PK cd_alerta NUMBER(6)','FK cd_tipo_alerta NUMBER(3)','dt_geracao DATE','ds_status VARCHAR2(12)','ds_descricao VARCHAR2(200)'])
arrow(165,456,165,424,'1:N')
arrow(429,456,429,291,'1:N')
box(48,424,235,'tb_medidor',['PK cd_medidor NUMBER(6)','FK cd_residencia NUMBER(6)','ds_numero_serie VARCHAR2(20) UQ','dt_instalacao DATE','ds_status VARCHAR2(10)'])
arrow(165,323,165,291,'1:N')
box(48,291,235,'tb_leitura',['PK cd_leitura NUMBER(8)','FK cd_medidor NUMBER(6)','dt_leitura DATE','vl_consumo_litros NUMBER(8,2)'])
box(312,291,235,'tb_alerta_leitura',['PK/FK cd_alerta NUMBER(6)','PK/FK cd_leitura NUMBER(8)'])
C.setStrokeColor(GREEN);C.line(283,245,312,245);C.setFont('Helvetica-Bold',8);C.drawString(288,255,'1:N')
y=181
p('PK = chave primária; FK = chave estrangeira; UQ = unicidade. Todos os campos são NOT NULL. A associativa resolve o N:N entre alertas e leituras. As tabelas de comunidade e alertas estão no banco, mas ainda não têm endpoints próprios.',9)
p('CHECK: moradores &gt; 0; consumo &gt;= 0; status e severidade restritos. UNIQUE adicional: medidor + instante da leitura. Seis sequences geram IDs; não há exclusão em cascata. O script inclui índices para referências e 60 registros demonstrativos.',9)

start('Endpoints REST','04 / Base local: http://localhost:8080/api')
rows=[['Método','Rota','Sucesso']]
for r in ['residencias','medidores','leituras']:
    for method,path,status in [('POST',f'/{r}','201 + Location'),('GET',f'/{r}','200 (lista)'),('GET',f'/{r}/{{id}}','200'),('PUT',f'/{r}/{{id}}','200'),('DELETE',f'/{r}/{{id}}','204')]:rows.append([method,path,status])
for r in ['/residencias/{id}/medidores','/medidores/{id}/leituras']:
    rows.extend([['GET',r,'200 (lista)'],['POST',r,'201 + Location']])
table(rows,[65,310,124])
p('POST cria um registro e devolve seu DTO com id. Location aponta para o recurso criado. PUT exige todos os campos editáveis. DELETE tem corpo vazio. GET de coleção devolve array; listas gerais são ordenadas por ID e não possuem paginação.',9)
p('Nas rotas aninhadas de POST, idResidencia ou idMedidor deve aparecer no JSON e coincidir com o ID da rota. Pai inexistente retorna 404; divergência de IDs retorna 409. Erros comuns: 400, 404, 405 e 409. Consultas de listas podem retornar 503 se o banco estiver indisponível.',9)

start('Exemplos JSON','05 / Corpos de POST e PUT; respostas ilustrativas de sucesso')
for resource,body in [('residencias',{'idComunidade':1,'codigoResidencia':'RES-NOVA','endereco':'Rua das Flores, 10','qtdMoradores':4}),('medidores',{'idResidencia':1,'numeroSerie':'HID-NOVO-001','dataInstalacao':'2026-01-01','status':'ATIVO'}),('leituras',{'idMedidor':1,'dataHoraLeitura':'2026-11-01T06:00:00','consumoLitros':580.5})]:
    title(f'POST /api/{resource}')
    code(json.dumps(body,ensure_ascii=False,indent=2))
    p(f'Resposta 201: mesmo objeto acima, acrescido de <b>"id": 1001</b>. Cabeçalho <b>Location: /api/{resource}/1001</b>. GET por ID e PUT devolvem esse mesmo formato de Response DTO.',9)
p('IDs e valores são ilustrativos; o ID real vem da sequence de cada tabela. GET de lista devolve esses objetos dentro de um array JSON. Datas usam ISO 8601 e dataHoraLeitura não contém fuso horário. Enviar Content-Type: application/json.')

start('Validações e regras','06 / Bean Validation, integridade e tratamento de erros')
table([['DTO / campo','Validação aplicada'],['ResidenciaRequest','idComunidade: obrigatório, positivo, até 9999; codigoResidencia: não vazio, até 15 caracteres; endereco: não vazio, até 120; qtdMoradores: obrigatório, de 1 a 99.'],['MedidorRequest','idResidencia: obrigatório, positivo, até 999999; numeroSerie: não vazio, até 20 caracteres; dataInstalacao: obrigatória, passada ou atual; status: ATIVO, INATIVO ou MANUTENCAO.'],['LeituraRequest','idMedidor: obrigatório, positivo, até 999999; dataHoraLeitura: obrigatória, passada ou atual; consumoLitros: obrigatório, não negativo, até 6 dígitos inteiros e 2 decimais.']],[124,375])
p('Somente medidor ATIVO recebe cadastro ou alteração de leitura. A leitura não pode anteceder a instalação. Medidor com histórico não pode mudar de residência, e sua instalação não pode ser movida para depois de uma leitura existente. Há bloqueio do medidor (SELECT FOR UPDATE) nas gravações relevantes, dentro de transação.')
table([['Status','Situação'],['400','JSON inválido, campo inválido ou parâmetro incompatível.'],['404 / 405','Recurso inexistente / método HTTP não suportado.'],['409','Regra violada, duplicidade ou remoção com dependências.'],['415 / 500 / 503','Mídia não suportada / falha interna / falha de acesso ao banco.']],[100,399])
code('''{
  "timestamp": "2026-11-14T23:00:00Z",
  "status": 409,
  "mensagem": "Apenas medidores ativos podem receber leituras.",
  "caminho": "/api/leituras",
  "detalhes": []
}''')
p('RegraNegocioException e NaoEncontradoException representam falhas do domínio. O handler global devolve mensagens JSON sem stack trace ou SQL. Erros de validação incluem o campo e a mensagem em detalhes.',9)

start('Execução e deploy','07 / Configuração local e procedimento previsto para publicação')
title('Executar localmente')
p('Instale JDK 21+ e Maven 3.9+. No computador atual, o script encontra o JDK em .jdks e o Maven em .tools. O Java 8 do PATH não atende ao projeto. Configure as credenciais pelo ambiente ou por config/application-local.properties, usando o arquivo .example como referência.')
code('.\\scripts\\run.ps1 -Test\n.\\scripts\\run.ps1\n# Alternativa com Maven instalado:\nmvn test\nmvn spring-boot:run')
table([['Variável','Finalidade'],['DB_URL','URL JDBC do Oracle. Pode ser sobrescrita por ambiente.'],['DB_USERNAME / DB_PASSWORD','Credenciais Oracle; não incluir valores no código ou PDF.'],['PORT','Porta HTTP (padrão local 8080).'],['CORS_ALLOWED_ORIGINS','Origens do front, separadas por vírgula. Padrão local: http://localhost:5173.']],[175,324])
title('Instalação do banco')
p('Em schema novo: executar database/01_schema.sql e depois 02_seed.sql. O script scripts/database.ps1 consulta a conexão; com -Apply, instala apenas se não encontrar objetos do projeto. Não repetir a carga em banco já instalado. DDL Oracle confirma implicitamente; falhas parciais exigem revisão manual.')
title('Publicação no Render - ainda pendente')
p('Criar Web Service com o repositório e runtime Docker. O Dockerfile compila com Maven/JDK 21 e copia o JAR para uma imagem JRE 21, executando com usuário 10001. Configurar as variáveis do banco e a origem pública do React. A rede do serviço deve alcançar o Oracle FIAP. Após publicar, testar as rotas e registrar a URL no README e neste documento.')
p('CORS permite GET, POST, PUT, DELETE e OPTIONS em /api/** e expõe Location. O front deverá consumir dados reais com Fetch. A publicação e a integração com React não foram validadas nesta versão.',9)

start('Verificação e limites','08 / Evidências da versão, pendências e rastreabilidade')
table([['Verificação em 14/11/2026','Resultado registrado'],['Oracle FIAP','Autenticação confirmada; 7 tabelas, 6 sequences e 60 registros após a limpeza dos testes.'],['Testes automatizados','ApiIntegrationTest: 3 testes, 0 falhas e 0 erros. H2 em modo Oracle, usando os mesmos scripts SQL.'],['CRUD real via HTTP','scripts/smoke.ps1 confirmou GET, POST, PUT e DELETE das 3 entidades, incluindo rotas aninhadas. Registros temporários excluídos.'],['Empacotamento','JAR executável gerado com Maven. Dockerfile disponível; imagem e deploy remoto não foram testados.']],[175,324])
p('Os testes cobrem DTOs, CRUD, relacionamentos, dependências, CORS e respostas 201, 200, 204, 400, 404, 405 e 409. Os resultados podem ser reproduzidos com scripts/run.ps1 -Test; o teste real exige a API ativa e scripts/smoke.ps1. H2 não reproduz todos os comportamentos do Oracle.')
title('Limites e próximas etapas')
p('Não há login de moradores, autorização por residência, paginação, previsão por IA, cálculo por leitura acumulada ou geração automática de alertas. O schema de alertas está preparado, mas a API expõe somente residências, medidores e leituras. A conta Oracle serve à conexão do sistema. Usar dados demonstrativos até implementar controle de acesso.')
p('Para fechar a entrega: publicar no Render, integrar o React e gravar o vídeo de até cinco minutos. Atualizar capa e README com os links reais. Não apresentar essas etapas como concluídas antes da verificação.')
title('Fontes e uso de IA')
p('Fontes locais: código em src/main/java, pom.xml, database/01_schema.sql, README.md, scripts/smoke.ps1 e relatórios em target/surefire-reports. Requisitos de Java: enunciado FIAP Global Solution 2026/2, páginas 47 a 55. A modelagem de água deriva do SQL fornecido pelo grupo.',9)
p('Referências técnicas: <link href="https://docs.spring.io/spring-boot/3.5/system-requirements.html" color="#087F78">Spring Boot - requisitos de sistema</link>; <link href="https://docs.oracle.com/en/database/oracle/oracle-database/26/jjdbc/data-sources-and-URLs.html" color="#087F78">Oracle JDBC - Data Sources and URLs</link>. Links para consulta técnica.',9)
p('Uso de IA: API e documento produzidos com apoio do Codex a partir dos arquivos do projeto e instruções do grupo. A conferência técnica incluiu testes automatizados e CRUD no Oracle. A equipe deve revisar o material, compreender o código e registrar sua própria revisão crítica e aprendizado na entrega de Software Engineering.',9)
C.save()
print(OUT)
