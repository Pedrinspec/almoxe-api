package com.almoxe.almoxeapi.categoria;

import java.util.UUID;

public record CategoriaResponse(UUID id, String nome) {

    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
