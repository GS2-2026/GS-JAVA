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
@RequestMapping("/api/residencias")
public class ResidenciaController {
    private final ResidenciaService service;
    private final ModelMapper mapper;
    private final MedidorService medidores;
    public ResidenciaController(ResidenciaService service, ModelMapper mapper, MedidorService medidores) {
        this.service = service; this.mapper = mapper; this.medidores = medidores;
    }
    private ResidenciaResponse response(Residencia item) { return mapper.map(item, ResidenciaResponse.class); }
    @GetMapping public List<ResidenciaResponse> listar() { return service.listar().stream().map(this::response).toList(); }
    @GetMapping("/{id}") public ResidenciaResponse buscar(@PathVariable long id) { return response(service.buscar(id)); }
    @PostMapping public ResponseEntity<ResidenciaResponse> criar(@Valid @RequestBody ResidenciaRequest request) {
        ResidenciaResponse result = response(service.salvar(null, mapper.map(request, Residencia.class)));
        return ResponseEntity.created(URI.create("/api/residencias/" + result.getId())).body(result);
    }
    @PutMapping("/{id}") public ResidenciaResponse atualizar(@PathVariable long id, @Valid @RequestBody ResidenciaRequest request) {
        return response(service.salvar(id, mapper.map(request, Residencia.class)));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable long id) { service.excluir(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/{id}/medidores")
    public List<MedidorResponse> medidores(@PathVariable long id) {
        return medidores.porResidencia(id).stream().map(m -> mapper.map(m, MedidorResponse.class)).toList();
    }
    @PostMapping("/{id}/medidores")
    public ResponseEntity<MedidorResponse> criarMedidor(@PathVariable long id, @Valid @RequestBody MedidorRequest request) {
        service.buscar(id);
        if (!request.getIdResidencia().equals(id)) throw new RegraNegocioException("idResidencia deve corresponder a rota.");
        MedidorResponse result = mapper.map(medidores.salvar(null, mapper.map(request, Medidor.class)), MedidorResponse.class);
        return ResponseEntity.created(URI.create("/api/medidores/" + result.getId())).body(result);
    }
}
