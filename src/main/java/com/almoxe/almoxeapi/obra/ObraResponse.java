package com.almoxe.almoxeapi.obra;

import java.util.UUID;

public record ObraResponse(UUID id, String nome, String endereco, boolean ativa) {

    public static ObraResponse from(Obra obra) {
        return new ObraResponse(obra.getId(), obra.getNome(), obra.getEndereco(), obra.isAtiva());
    }
}
