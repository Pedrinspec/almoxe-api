package com.almoxe.almoxeapi.obra;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/obras")
public class ObraController {

    private final ObraService service;

    public ObraController(ObraService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ObraResponse> criar(@Valid @RequestBody ObraRequest request) {
        ObraResponse criada = service.criar(request);
        return ResponseEntity.created(URI.create("/obras/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<ObraResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ObraResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ObraResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ObraRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        service.deletar(id);
    }
}
