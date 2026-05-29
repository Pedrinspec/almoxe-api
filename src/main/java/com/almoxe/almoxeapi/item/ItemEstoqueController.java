package com.almoxe.almoxeapi.item;

import com.almoxe.almoxeapi.movimentacao.MovimentacaoResponse;
import com.almoxe.almoxeapi.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ItemEstoqueResponse> darEntrada(@Valid @RequestBody EntradaRequest request,
                                                          @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        ItemEstoqueResponse item = service.darEntrada(request, autenticado);
        return ResponseEntity.created(URI.create("/itens/" + item.id())).body(item);
    }

    @GetMapping
    public List<ItemEstoqueResponse> listar(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.listar(autenticado);
    }

    @GetMapping("/{id}")
    public ItemEstoqueResponse buscarPorId(@PathVariable UUID id,
                                           @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.buscarPorId(id, autenticado);
    }

    @GetMapping("/{id}/movimentacoes")
    public List<MovimentacaoResponse> historico(@PathVariable UUID id,
                                                @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.historico(id, autenticado);
    }

    @PostMapping("/{id}/alocacao")
    public ItemEstoqueResponse alocar(@PathVariable UUID id, @Valid @RequestBody AlocacaoRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.alocar(id, request, autenticado);
    }

    @PostMapping("/{id}/uso")
    public ItemEstoqueResponse usar(@PathVariable UUID id, @Valid @RequestBody OperacaoRequest request,
                                    @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.usar(id, request, autenticado);
    }

    @PostMapping("/{id}/retorno")
    public ItemEstoqueResponse retornar(@PathVariable UUID id, @Valid @RequestBody OperacaoRequest request,
                                        @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.retornar(id, request, autenticado);
    }

    @PostMapping("/{id}/baixa")
    public ItemEstoqueResponse baixar(@PathVariable UUID id, @Valid @RequestBody BaixaRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.baixar(id, request, autenticado);
    }
}
