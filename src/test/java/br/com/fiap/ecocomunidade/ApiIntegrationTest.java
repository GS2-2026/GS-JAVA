package br.com.fiap.ecocomunidade;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import javax.sql.DataSource;
import java.sql.Connection;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired DataSource dataSource;
    @Autowired ObjectMapper mapper;
    JdbcTemplate jdbc;
    @BeforeEach void schema() throws Exception {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP ALL OBJECTS"); // Somente H2 isolado do perfil test.
        try (Connection c = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(c, new FileSystemResource("database/01_schema.sql"));
            ScriptUtils.executeSqlScript(c, new FileSystemResource("database/02_seed.sql"));
        }
    }
    @Test void sequenciasContinuamAposSeed() {
        String[] tabelas = {"comunidade", "residencia", "medidor", "leitura", "tipo_alerta", "alerta"};
        for (String tabela : tabelas) {
            Long esperado = jdbc.queryForObject("SELECT MAX(cd_" + tabela + ") + 1 FROM tb_" + tabela, Long.class);
            Long proximo = jdbc.queryForObject("SELECT sq_tb_" + tabela + ".NEXTVAL FROM dual", Long.class);
            Assertions.assertEquals(esperado, proximo, tabela);
        }
    }
    @Test void leituraPreservaHorarioLocalEAceitaZero() throws Exception {
        long id = create("leituras", "{\"idMedidor\":1,\"dataHoraLeitura\":\"2026-01-02T21:15:00\",\"consumoLitros\":0}");
        Assertions.assertEquals(21, id);
        mvc.perform(get("/api/leituras/" + id)).andExpect(status().isOk())
            .andExpect(jsonPath("dataHoraLeitura").value("2026-01-02T21:15:00"))
            .andExpect(jsonPath("consumoLitros").value(0));
    }
    @Test void rejeitaHorarioFuturoEmSaoPauloMesmoComServidorUtc() throws Exception {
        var original = java.util.TimeZone.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            var futuro = java.time.LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo")).plusHours(1).withNano(0);
            mvc.perform(post("/api/leituras").contentType("application/json")
                .content("{\"idMedidor\":1,\"dataHoraLeitura\":\"" + futuro + "\",\"consumoLitros\":10}"))
                .andExpect(status().isBadRequest());
        } finally {
            java.util.TimeZone.setDefault(original);
        }
    }
    long create(String resource, String body) throws Exception {
        var result = mvc.perform(post("/api/" + resource).contentType("application/json").content(body))
            .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
    @Test void crudComRelacionamentosEIntegridade() throws Exception {
        String casa = "{\"idComunidade\":1,\"codigoResidencia\":\"TEST-001\",\"endereco\":\"Rua Teste, 1\",\"qtdMoradores\":3}";
        long residencia = create("residencias", casa);
        mvc.perform(get("/api/residencias/"+residencia)).andExpect(status().isOk()).andExpect(jsonPath("codigoResidencia").value("TEST-001"));
        mvc.perform(put("/api/residencias/"+residencia).contentType("application/json").content(casa.replace("Rua Teste, 1","Rua Teste, 2"))).andExpect(status().isOk());
        String medidor = "{\"idResidencia\":"+residencia+",\"numeroSerie\":\"TEST-HID-001\",\"dataInstalacao\":\"2025-01-01\",\"status\":\"ATIVO\"}";
        long m = create("residencias/"+residencia+"/medidores", medidor);
        mvc.perform(get("/api/residencias/"+residencia+"/medidores")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(m));
        mvc.perform(get("/api/medidores/"+m)).andExpect(status().isOk());
        mvc.perform(put("/api/medidores/"+m).contentType("application/json").content(medidor)).andExpect(status().isOk());
        String leitura = "{\"idMedidor\":"+m+",\"dataHoraLeitura\":\"2026-01-01T06:00:00\",\"consumoLitros\":123.45}";
        long l = create("medidores/"+m+"/leituras", leitura);
        mvc.perform(get("/api/leituras/"+l)).andExpect(status().isOk()).andExpect(jsonPath("consumoLitros").value(123.45));
        mvc.perform(put("/api/leituras/"+l).contentType("application/json").content(leitura.replace("123.45","100.00"))).andExpect(status().isOk());
        mvc.perform(get("/api/medidores/"+m+"/leituras")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(l));
        mvc.perform(post("/api/leituras").contentType("application/json").content(leitura)).andExpect(status().isConflict());
        mvc.perform(delete("/api/residencias/"+residencia)).andExpect(status().isConflict());
        mvc.perform(delete("/api/medidores/"+m)).andExpect(status().isConflict());
        mvc.perform(delete("/api/leituras/"+l)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/medidores/"+m)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/residencias/"+residencia)).andExpect(status().isNoContent());
        mvc.perform(get("/api/residencias/"+residencia)).andExpect(status().isNotFound());
    }
    @Test void validacoesErrosERegrasDeDominio() throws Exception {
        mvc.perform(post("/api/residencias").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("detalhes").isArray());
        mvc.perform(get("/api/residencias/abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/residencias/999999")).andExpect(status().isNotFound());
        mvc.perform(patch("/api/leituras")).andExpect(status().isMethodNotAllowed()).andExpect(header().exists("Allow"));
        mvc.perform(get("/api/inexistente")).andExpect(status().isNotFound()).andExpect(content().contentTypeCompatibleWith("application/json"));
        mvc.perform(post("/api/leituras").contentType("application/json")
            .content("{\"idMedidor\":9,\"dataHoraLeitura\":\"2026-01-01T06:00:00\",\"consumoLitros\":1}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/leituras").contentType("application/json")
            .content("{\"idMedidor\":1,\"dataHoraLeitura\":\"2020-01-01T06:00:00\",\"consumoLitros\":1}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/leituras").contentType("application/json")
            .content("{\"idMedidor\":1,\"dataHoraLeitura\":\"2026-01-01T06:00:00\",\"consumoLitros\":-1}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/residencias/999999/medidores")).andExpect(status().isNotFound());
        for (String rota : new String[]{"residencias", "medidores", "leituras"})
            mvc.perform(get("/api/"+rota)).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }
    @Test void corsDoFrontend() throws Exception {
        mvc.perform(options("/api/residencias").header("Origin","http://localhost:5173")
            .header("Access-Control-Request-Method","POST"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
    }
}
