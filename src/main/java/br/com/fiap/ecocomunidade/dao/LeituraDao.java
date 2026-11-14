package br.com.fiap.ecocomunidade.dao;
import br.com.fiap.ecocomunidade.model.Leitura;
import br.com.fiap.ecocomunidade.exception.NaoEncontradoException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LeituraDao {
    private final JdbcTemplate jdbc;
    public LeituraDao(DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }
    private Leitura map(ResultSet rs, int row) throws SQLException {
        Leitura item = new Leitura();
        item.setId(rs.getLong("cd_leitura"));
        item.setIdMedidor(rs.getLong("cd_medidor"));
        item.setDataHoraLeitura(rs.getTimestamp("dt_leitura").toLocalDateTime());
        item.setConsumoLitros(rs.getBigDecimal("vl_consumo_litros"));
        return item;
    }
    public List<Leitura> listar() { return jdbc.query("SELECT cd_leitura, cd_medidor, dt_leitura, vl_consumo_litros FROM tb_leitura ORDER BY cd_leitura", this::map); }
    public Leitura buscar(long id) {
        return jdbc.query("SELECT cd_leitura, cd_medidor, dt_leitura, vl_consumo_litros FROM tb_leitura WHERE cd_leitura = ?", this::map, id)
            .stream().findFirst().orElseThrow(() -> new NaoEncontradoException("Leitura nao encontrado(a)."));
    }
    public Leitura inserir(Leitura item) {
        Long id = jdbc.queryForObject("SELECT sq_tb_leitura.NEXTVAL FROM dual", Long.class);
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO tb_leitura (cd_leitura, cd_medidor, dt_leitura, vl_consumo_litros) VALUES (?, ?, ?, ?)");
            ps.setLong(1, id);
            ps.setObject(2, item.getIdMedidor());
            ps.setObject(3, item.getDataHoraLeitura());
            ps.setObject(4, item.getConsumoLitros());
            return ps;
        });
        return buscar(id);
    }
    public Leitura atualizar(long id, Leitura item) {
        if (jdbc.update("UPDATE tb_leitura SET cd_medidor = ?, dt_leitura = ?, vl_consumo_litros = ? WHERE cd_leitura = ?", item.getIdMedidor(), item.getDataHoraLeitura(), item.getConsumoLitros(), id) == 0)
            throw new NaoEncontradoException("Leitura nao encontrado(a).");
        return buscar(id);
    }
    public void excluir(long id) {
        if (jdbc.update("DELETE FROM tb_leitura WHERE cd_leitura = ?", id) == 0) throw new NaoEncontradoException("Leitura nao encontrado(a).");
    }
    public List<Leitura> porResponsavel(long id) {
        return jdbc.query("SELECT cd_leitura, cd_medidor, dt_leitura, vl_consumo_litros FROM tb_leitura WHERE cd_medidor = ? ORDER BY cd_leitura", this::map, id);
    }
}
