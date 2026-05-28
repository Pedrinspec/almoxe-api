package com.almoxe.almoxeapi.item;

import com.almoxe.almoxeapi.movimentacao.MovimentacaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/itens")
public class ItemEstoqueController {

    private final ItemEstoqueService service;

    public ItemEstoqueController(ItemEstoqueService service) {
        this.service = service;
    }

    @PostMapping("/entrada")
    public ResponseEntity<ItemEstoqueResponse> darEntrada(@Valid @RequestBody EntradaRequest request) {
        ItemEstoqueResponse item = service.darEntrada(request);
        return ResponseEntity.created(URI.create("/itens/" + item.id())).body(item);
    }

    @GetMapping
    public List<ItemEstoqueResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ItemEstoqueResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/{id}/movimentacoes")
    public List<MovimentacaoResponse> historico(@PathVariable UUID id) {
        return service.historico(id);
    }
}
