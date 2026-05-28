package com.almoxe.almoxeapi.categoria;

import com.almoxe.almoxeapi.common.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNome(request.nome());
        return CategoriaResponse.from(repository.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAll().stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .map(CategoriaResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + id));
    }

    public CategoriaResponse atualizar(UUID id, CategoriaRequest request) {
        Categoria categoria = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + id));
        categoria.setNome(request.nome());
        return CategoriaResponse.from(categoria);
    }

    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Categoria não encontrada: " + id);
        }
        repository.deleteById(id);
    }
}
