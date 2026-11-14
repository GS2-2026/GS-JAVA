import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Instala somente objetos novos. Nao apaga nem altera tabelas existentes. */
public class DatabaseSetup {
    public static void main(String[] args) throws Exception {
        Properties p = new Properties();
        Path config = Path.of("config/application-local.properties");
        if (Files.exists(config)) try (var in = Files.newInputStream(config)) { p.load(in); }
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL");
        String user = System.getenv().getOrDefault("DB_USERNAME", p.getProperty("spring.datasource.username", ""));
        String password = System.getenv().getOrDefault("DB_PASSWORD", p.getProperty("spring.datasource.password", ""));
        Properties connection = new Properties();
        connection.setProperty("user", user);
        connection.setProperty("password", password);
        connection.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");
        connection.setProperty("oracle.jdbc.ReadTimeout", "20000");
        try (Connection c = DriverManager.getConnection(url, connection)) {
            System.out.println("Conexao Oracle autenticada.");
            String[] tables = {"TB_COMUNIDADE", "TB_RESIDENCIA", "TB_MEDIDOR", "TB_LEITURA", "TB_TIPO_ALERTA", "TB_ALERTA", "TB_ALERTA_LEITURA"};
            int objects;
            try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT COUNT(*) FROM user_objects WHERE object_name IN ('TB_COMUNIDADE','TB_RESIDENCIA','TB_MEDIDOR','TB_LEITURA','TB_TIPO_ALERTA','TB_ALERTA','TB_ALERTA_LEITURA','SQ_TB_COMUNIDADE','SQ_TB_RESIDENCIA','SQ_TB_MEDIDOR','SQ_TB_LEITURA','SQ_TB_TIPO_ALERTA','SQ_TB_ALERTA')")) {
                r.next(); objects = r.getInt(1);
            }
            if (args.length == 1 && args[0].equals("--apply")) {
                if (objects != 0) throw new IllegalStateException("Ja existem objetos do projeto. Instalacao interrompida para preservar os dados; revise o schema manualmente.");
                // Oracle confirma DDL implicitamente. Em falha parcial, revisar objetos antes de repetir.
                execute(c, Path.of("database/01_schema.sql"));
                c.setAutoCommit(false);
                try { execute(c, Path.of("database/02_seed.sql")); c.commit(); }
                catch (Exception e) { c.rollback(); throw e; }
                System.out.println("Schema criado e 60 registros demonstrativos inseridos.");
            } else {
                System.out.println("Modo consulta. Objetos do projeto encontrados: " + objects);
            }
            if (objects == 13 || (args.length == 1 && args[0].equals("--apply"))) {
                for (String table : tables) try (Statement st = c.createStatement(); ResultSet r = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    r.next(); System.out.println(table + ": " + r.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Falha Oracle. Codigo: " + e.getErrorCode() + "; SQLState: " + e.getSQLState());
            System.exit(1);
        }
    }
    private static void execute(Connection c, Path path) throws Exception {
        String script = Files.readString(path).replaceAll("(?m)^\\s*--.*$", "");
        StringBuilder sql = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < script.length(); i++) {
            char ch = script.charAt(i);
            if (ch == '\'') {
                if (quoted && i + 1 < script.length() && script.charAt(i + 1) == '\'') { sql.append("''"); i++; continue; }
                quoted = !quoted;
            }
            if (ch == ';' && !quoted) {
                if (!sql.toString().isBlank()) try (Statement st = c.createStatement()) { st.execute(sql.toString()); }
                sql.setLength(0);
            } else sql.append(ch);
        }
        if (!sql.toString().isBlank()) try (Statement st = c.createStatement()) { st.execute(sql.toString()); }
    }
}
