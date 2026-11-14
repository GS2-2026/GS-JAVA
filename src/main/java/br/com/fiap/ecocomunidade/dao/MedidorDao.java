package br.com.fiap.ecocomunidade.dao;
import br.com.fiap.ecocomunidade.model.Medidor;
import br.com.fiap.ecocomunidade.exception.NaoEncontradoException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MedidorDao {
    private final JdbcTemplate jdbc;
    public MedidorDao(DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }
    private Medidor map(ResultSet rs, int row) throws SQLException {
        Medidor item = new Medidor();
        item.setId(rs.getLong("cd_medidor"));
        item.setIdResidencia(rs.getLong("cd_residencia"));
        item.setNumeroSerie(rs.getString("ds_numero_serie"));
        item.setDataInstalacao(rs.getDate("dt_instalacao").toLocalDate());
        item.setStatus(rs.getString("ds_status"));
        return item;
    }
    public List<Medidor> listar() { return jdbc.query("SELECT cd_medidor, cd_residencia, ds_numero_serie, dt_instalacao, ds_status FROM tb_medidor ORDER BY cd_medidor", this::map); }
    public Medidor buscar(long id) {
        return jdbc.query("SELECT cd_medidor, cd_residencia, ds_numero_serie, dt_instalacao, ds_status FROM tb_medidor WHERE cd_medidor = ?", this::map, id)
            .stream().findFirst().orElseThrow(() -> new NaoEncontradoException("Medidor nao encontrado(a)."));
    }
    public Medidor inserir(Medidor item) {
        Long id = jdbc.queryForObject("SELECT sq_tb_medidor.NEXTVAL FROM dual", Long.class);
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO tb_medidor (cd_medidor, cd_residencia, ds_numero_serie, dt_instalacao, ds_status) VALUES (?, ?, ?, ?, ?)");
            ps.setLong(1, id);
            ps.setObject(2, item.getIdResidencia());
            ps.setObject(3, item.getNumeroSerie());
            ps.setObject(4, item.getDataInstalacao());
            ps.setObject(5, item.getStatus());
            return ps;
        });
        return buscar(id);
    }
    public Medidor atualizar(long id, Medidor item) {
        if (jdbc.update("UPDATE tb_medidor SET cd_residencia = ?, ds_numero_serie = ?, dt_instalacao = ?, ds_status = ? WHERE cd_medidor = ?", item.getIdResidencia(), item.getNumeroSerie(), item.getDataInstalacao(), item.getStatus(), id) == 0)
            throw new NaoEncontradoException("Medidor nao encontrado(a).");
        return buscar(id);
    }
    public void excluir(long id) {
        if (jdbc.update("DELETE FROM tb_medidor WHERE cd_medidor = ?", id) == 0) throw new NaoEncontradoException("Medidor nao encontrado(a).");
    }
    public List<Medidor> porResponsavel(long id) {
        return jdbc.query("SELECT cd_medidor, cd_residencia, ds_numero_serie, dt_instalacao, ds_status FROM tb_medidor WHERE cd_residencia = ? ORDER BY cd_medidor", this::map, id);
    }
    public Medidor bloquear(long id) {
        return jdbc.query("SELECT * FROM tb_medidor WHERE cd_medidor = ? FOR UPDATE", this::map, id)
            .stream().findFirst().orElseThrow(() -> new NaoEncontradoException("Medidor nao encontrado."));
    }
}
