package com.almoxe.almoxeapi.produto;

import java.math.BigDecimal;

public record EstoqueBaixoResponse(
        ProdutoResponse produto,
        BigDecimal quantidadeDisponivel,
        Integer estoqueMinimo
) {

    public static EstoqueBaixoResponse from(EstoqueBaixoProjecao projecao) {
        return new EstoqueBaixoResponse(
                ProdutoResponse.from(projecao.produto()),
                projecao.quantidadeDisponivel(),
                projecao.produto().getEstoqueMinimo()
        );
    }
}
