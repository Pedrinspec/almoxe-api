package com.almoxe.almoxeapi.produto;

import com.almoxe.almoxeapi.categoria.CategoriaResponse;

import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        String nome,
        String descricao,
        TipoControle tipoControle,
        Integer estoqueMinimo,
        CategoriaResponse categoria
) {

    public static ProdutoResponse from(Produto produto) {
        CategoriaResponse categoria = produto.getCategoria() == null
                ? null
                : CategoriaResponse.from(produto.getCategoria());
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getTipoControle(),
                produto.getEstoqueMinimo(),
                categoria
        );
    }
}
