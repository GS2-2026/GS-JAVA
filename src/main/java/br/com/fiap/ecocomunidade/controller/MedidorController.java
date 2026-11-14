package br.com.fiap.ecocomunidade.controller;
import br.com.fiap.ecocomunidade.dto.*;
import br.com.fiap.ecocomunidade.model.*;
import br.com.fiap.ecocomunidade.service.*;
import br.com.fiap.ecocomunidade.exception.RegraNegocioException;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medidores")
public class MedidorController {
    private final MedidorService service;
    private final ModelMapper mapper;
    private final LeituraService leituras;
    public MedidorController(MedidorService service, ModelMapper mapper, LeituraService leituras) {
        this.service = service; this.mapper = mapper; this.leituras = leituras;
    }
    private MedidorResponse response(Medidor item) { return mapper.map(item, MedidorResponse.class); }
    @GetMapping public List<MedidorResponse> listar() { return service.listar().stream().map(this::response).toList(); }
    @GetMapping("/{id}") public MedidorResponse buscar(@PathVariable long id) { return response(service.buscar(id)); }
    @PostMapping public ResponseEntity<MedidorResponse> criar(@Valid @RequestBody MedidorRequest request) {
        MedidorResponse result = response(service.salvar(null, mapper.map(request, Medidor.class)));
        return ResponseEntity.created(URI.create("/api/medidores/" + result.getId())).body(result);
    }
    @PutMapping("/{id}") public MedidorResponse atualizar(@PathVariable long id, @Valid @RequestBody MedidorRequest request) {
        return response(service.salvar(id, mapper.map(request, Medidor.class)));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable long id) { service.excluir(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/{id}/leituras")
    public List<LeituraResponse> leituras(@PathVariable long id) {
        return leituras.porMedidor(id).stream().map(l -> mapper.map(l, LeituraResponse.class)).toList();
    }
    @PostMapping("/{id}/leituras")
    public ResponseEntity<LeituraResponse> criarLeitura(@PathVariable long id, @Valid @RequestBody LeituraRequest request) {
        service.buscar(id);
        if (!request.getIdMedidor().equals(id)) throw new RegraNegocioException("idMedidor deve corresponder a rota.");
        LeituraResponse result = mapper.map(leituras.salvar(null, mapper.map(request, Leitura.class)), LeituraResponse.class);
        return ResponseEntity.created(URI.create("/api/leituras/" + result.getId())).body(result);
    }
}
