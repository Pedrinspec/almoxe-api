package com.almoxe.almoxeapi.item;

import com.almoxe.almoxeapi.produto.ProdutoResponse;
import com.almoxe.almoxeapi.usuario.UsuarioResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemEstoqueResponse(
        UUID id,
        ProdutoResponse produto,
        UsuarioResponse responsavel,
        StatusItem status,
        BigDecimal quantidade,
        String numeroSerie,
        String lote,
        String notaFiscal,
        String numeroRi
) {

    public static ItemEstoqueResponse from(ItemEstoque item) {
        UsuarioResponse responsavel = item.getResponsavel() == null
                ? null
                : UsuarioResponse.from(item.getResponsavel());
        return new ItemEstoqueResponse(
                item.getId(),
                ProdutoResponse.from(item.getProduto()),
                responsavel,
                item.getStatus(),
                item.getQuantidade(),
                item.getNumeroSerie(),
                item.getLote(),
                item.getNotaFiscal(),
                item.getNumeroRi()
        );
    }
}
