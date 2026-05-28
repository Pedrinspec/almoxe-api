package com.almoxe.almoxeapi.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProdutoRequest(
        @NotBlank
        @Size(max = 150)
        String nome,

        @Size(max = 500)
        String descricao,

        @NotNull
        TipoControle tipoControle,

        @NotNull
        @PositiveOrZero
        Integer estoqueMinimo,

        UUID categoriaId
) {}
