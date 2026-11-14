package br.com.fiap.ecocomunidade.dao;
import br.com.fiap.ecocomunidade.model.Residencia;
import br.com.fiap.ecocomunidade.exception.NaoEncontradoException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ResidenciaDao {
    private final JdbcTemplate jdbc;
    public ResidenciaDao(DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }
    private Residencia map(ResultSet rs, int row) throws SQLException {
        Residencia item = new Residencia();
        item.setId(rs.getLong("cd_residencia"));
        item.setIdComunidade(rs.getLong("cd_comunidade"));
        item.setCodigoResidencia(rs.getString("ds_codigo_residencia"));
        item.setEndereco(rs.getString("ds_endereco"));
        item.setQtdMoradores(rs.getInt("qt_moradores"));
        return item;
    }
    public List<Residencia> listar() { return jdbc.query("SELECT cd_residencia, cd_comunidade, ds_codigo_residencia, ds_endereco, qt_moradores FROM tb_residencia ORDER BY cd_residencia", this::map); }
    public Residencia buscar(long id) {
        return jdbc.query("SELECT cd_residencia, cd_comunidade, ds_codigo_residencia, ds_endereco, qt_moradores FROM tb_residencia WHERE cd_residencia = ?", this::map, id)
            .stream().findFirst().orElseThrow(() -> new NaoEncontradoException("Residencia nao encontrado(a)."));
    }
    public Residencia inserir(Residencia item) {
        Long id = jdbc.queryForObject("SELECT sq_tb_residencia.NEXTVAL FROM dual", Long.class);
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO tb_residencia (cd_residencia, cd_comunidade, ds_codigo_residencia, ds_endereco, qt_moradores) VALUES (?, ?, ?, ?, ?)");
            ps.setLong(1, id);
            ps.setObject(2, item.getIdComunidade());
            ps.setObject(3, item.getCodigoResidencia());
            ps.setObject(4, item.getEndereco());
            ps.setObject(5, item.getQtdMoradores());
            return ps;
        });
        return buscar(id);
    }
    public Residencia atualizar(long id, Residencia item) {
        if (jdbc.update("UPDATE tb_residencia SET cd_comunidade = ?, ds_codigo_residencia = ?, ds_endereco = ?, qt_moradores = ? WHERE cd_residencia = ?", item.getIdComunidade(), item.getCodigoResidencia(), item.getEndereco(), item.getQtdMoradores(), id) == 0)
            throw new NaoEncontradoException("Residencia nao encontrado(a).");
        return buscar(id);
    }
    public void excluir(long id) {
        if (jdbc.update("DELETE FROM tb_residencia WHERE cd_residencia = ?", id) == 0) throw new NaoEncontradoException("Residencia nao encontrado(a).");
    }
}
