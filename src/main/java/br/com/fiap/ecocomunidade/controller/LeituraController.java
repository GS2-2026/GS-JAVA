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
@RequestMapping("/api/leituras")
public class LeituraController {
    private final LeituraService service;
    private final ModelMapper mapper;
    
    public LeituraController(LeituraService service, ModelMapper mapper) {
        this.service = service; this.mapper = mapper; 
    }
    private LeituraResponse response(Leitura item) { return mapper.map(item, LeituraResponse.class); }
    @GetMapping public List<LeituraResponse> listar() { return service.listar().stream().map(this::response).toList(); }
    @GetMapping("/{id}") public LeituraResponse buscar(@PathVariable long id) { return response(service.buscar(id)); }
    @PostMapping public ResponseEntity<LeituraResponse> criar(@Valid @RequestBody LeituraRequest request) {
        LeituraResponse result = response(service.salvar(null, mapper.map(request, Leitura.class)));
        return ResponseEntity.created(URI.create("/api/leituras/" + result.getId())).body(result);
    }
    @PutMapping("/{id}") public LeituraResponse atualizar(@PathVariable long id, @Valid @RequestBody LeituraRequest request) {
        return response(service.salvar(id, mapper.map(request, Leitura.class)));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable long id) { service.excluir(id); return ResponseEntity.noContent().build(); }
}