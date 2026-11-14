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
public class LeituraService {
    private final LeituraDao dao;
    private final JdbcTemplate jdbc;
    private final MedidorDao medidores;
    public LeituraService(LeituraDao dao, DataSource ds, MedidorDao medidores) {
        this.dao = dao; this.jdbc = new JdbcTemplate(ds); this.medidores = medidores;
    }
    public List<Leitura> listar() { return dao.listar(); }
    public Leitura buscar(long id) { return dao.buscar(id); }
    @Transactional public Leitura salvar(Long id, Leitura item) {
        if (id != null) dao.buscar(id);
        validarLeitura(item);
        return id == null ? dao.inserir(item) : dao.atualizar(id, item);
    }
    @Transactional public void excluir(long id) { dao.excluir(id); }
    private void validarLeitura(Leitura item) {
        Medidor medidor = medidores.bloquear(item.getIdMedidor());
        if (!"ATIVO".equals(medidor.getStatus()))
            throw new RegraNegocioException("Apenas medidores ativos podem receber leituras.");
        if (item.getDataHoraLeitura().toLocalDate().isBefore(medidor.getDataInstalacao()))
            throw new RegraNegocioException("Leitura nao pode ser anterior a instalacao do medidor.");
    }
    public List<Leitura> porMedidor(long id) { medidores.buscar(id); return dao.porResponsavel(id); }
}
