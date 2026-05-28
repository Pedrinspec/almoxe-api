package com.almoxe.almoxeapi.produto;

import com.almoxe.almoxeapi.categoria.Categoria;
import com.almoxe.almoxeapi.categoria.CategoriaRepository;
import com.almoxe.almoxeapi.common.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProdutoService {

    private final ProdutoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository repository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    public ProdutoResponse criar(ProdutoRequest request) {
        Produto produto = new Produto();
        aplicar(request, produto);
        return ProdutoResponse.from(repository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return repository.findAll().stream()
                .map(ProdutoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .map(ProdutoResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + id));
    }

    public ProdutoResponse atualizar(UUID id, ProdutoRequest request) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + id));
        aplicar(request, produto);
        return ProdutoResponse.from(produto);
    }

    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Produto não encontrado: " + id);
        }
        repository.deleteById(id);
    }

    private void aplicar(ProdutoRequest request, Produto produto) {
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setTipoControle(request.tipoControle());
        produto.setEstoqueMinimo(request.estoqueMinimo());
        produto.setCategoria(resolverCategoria(request.categoriaId()));
    }

    private Categoria resolverCategoria(UUID categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + categoriaId));
    }
}
