package com.almoxe.almoxeapi.foto;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class FotoController {

    private final FotoService service;

    public FotoController(FotoService service) {
        this.service = service;
    }

    @PostMapping(value = "/movimentacoes/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<FotoResponse> anexar(@PathVariable UUID id,
                                     @RequestParam("arquivos") List<MultipartFile> arquivos) {
        return service.anexar(id, arquivos);
    }

    @GetMapping("/movimentacoes/{id}/fotos")
    public List<FotoResponse> listar(@PathVariable UUID id) {
        return service.listar(id);
    }

    @GetMapping("/fotos/{id}")
    public ResponseEntity<Resource> baixar(@PathVariable UUID id) {
        FotoArquivo arquivo = service.baixar(id);
        return ResponseEntity.ok()
                .contentType(arquivo.contentType())
                .body(arquivo.conteudo());
    }
}
