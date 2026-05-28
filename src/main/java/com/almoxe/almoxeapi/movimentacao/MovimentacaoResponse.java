package com.almoxe.almoxeapi.movimentacao;

import com.almoxe.almoxeapi.usuario.UsuarioResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovimentacaoResponse(
        UUID id,
        UUID itemEstoqueId,
        TipoMovimentacao tipo,
        BigDecimal quantidade,
        Instant dataHora,
        UsuarioResponse usuario,
        UUID obraId,
        String observacao
) {

    public static MovimentacaoResponse from(Movimentacao mov) {
        return new MovimentacaoResponse(
                mov.getId(),
                mov.getItemEstoque().getId(),
                mov.getTipo(),
                mov.getQuantidade(),
                mov.getDataHora(),
                UsuarioResponse.from(mov.getUsuario()),
                mov.getObraId(),
                mov.getObservacao()
        );
    }
}
