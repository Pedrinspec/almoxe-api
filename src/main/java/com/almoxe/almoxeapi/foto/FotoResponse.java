package com.almoxe.almoxeapi.foto;

import java.time.Instant;
import java.util.UUID;

public record FotoResponse(UUID id, UUID movimentacaoId, String url, Instant dataUpload) {

    public static FotoResponse from(Foto foto) {
        return new FotoResponse(
                foto.getId(),
                foto.getMovimentacao().getId(),
                "/fotos/" + foto.getId(),
                foto.getDataUpload()
        );
    }
}
