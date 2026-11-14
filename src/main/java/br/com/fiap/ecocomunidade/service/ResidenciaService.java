package br.com.fiap.ecocomunidade.service;
import br.com.fiap.ecocomunidade.dao.*;
import br.com.fiap.ecocomunidade.model.*;
import br.com.fiap.ecocomunidade.exception.*;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResidenciaService {
    private final ResidenciaDao dao;
    private final JdbcTemplate jdbc;
    private final MedidorDao medidores;
    public ResidenciaService(ResidenciaDao dao, DataSource ds, MedidorDao medidores) {
        this.dao = dao; this.jdbc = new JdbcTemplate(ds); this.medidores = medidores;
    }
    public List<Residencia> listar() { return dao.listar(); }
    public Residencia buscar(long id) { return dao.buscar(id); }
    @Transactional public Residencia salvar(Long id, Residencia item) {
        if (id != null) dao.buscar(id);
        validarComunidade(item.getIdComunidade());
        return id == null ? dao.inserir(item) : dao.atualizar(id, item);
    }
    @Transactional public void excluir(long id) { dao.excluir(id); }
    private void validarComunidade(long id) {
        if (jdbc.queryForObject("SELECT COUNT(*) FROM tb_comunidade WHERE cd_comunidade = ?", Integer.class, id) == 0)
            throw new NaoEncontradoException("Comunidade nao encontrada.");
    }
}
