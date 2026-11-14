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
public class MedidorService {
    private final MedidorDao dao;
    private final JdbcTemplate jdbc;
    private final MedidorDao medidores;
    public MedidorService(MedidorDao dao, DataSource ds, MedidorDao medidores) {
        this.dao = dao; this.jdbc = new JdbcTemplate(ds); this.medidores = medidores;
    }
    public List<Medidor> listar() { return dao.listar(); }
    public Medidor buscar(long id) { return dao.buscar(id); }
    @Transactional public Medidor salvar(Long id, Medidor item) {
        if (id != null) dao.buscar(id);
        validarMedidor(item, id);
        return id == null ? dao.inserir(item) : dao.atualizar(id, item);
    }
    @Transactional public void excluir(long id) { dao.excluir(id); }
    private void validarMedidor(Medidor item, Long id) {
        if (jdbc.queryForObject("SELECT COUNT(*) FROM tb_residencia WHERE cd_residencia = ?", Integer.class, item.getIdResidencia()) == 0)
            throw new NaoEncontradoException("Residencia nao encontrada.");
        if (id != null) {
            Medidor atual = dao.bloquear(id);
            if (jdbc.queryForObject("SELECT COUNT(*) FROM tb_leitura WHERE cd_medidor = ? AND dt_leitura < ?", Integer.class, id, item.getDataInstalacao().atStartOfDay()) > 0)
                throw new RegraNegocioException("Instalacao nao pode ser posterior a uma leitura existente.");
            if (!atual.getIdResidencia().equals(item.getIdResidencia()) &&
                jdbc.queryForObject("SELECT COUNT(*) FROM tb_leitura WHERE cd_medidor = ?", Integer.class, id) > 0)
                throw new RegraNegocioException("Medidor com historico nao pode mudar de residencia.");
        }
    }
    public List<Medidor> porResidencia(long id) {
        if (jdbc.queryForObject("SELECT COUNT(*) FROM tb_residencia WHERE cd_residencia = ?", Integer.class, id) == 0)
            throw new NaoEncontradoException("Residencia nao encontrada.");
        return dao.porResponsavel(id);
    }
}
