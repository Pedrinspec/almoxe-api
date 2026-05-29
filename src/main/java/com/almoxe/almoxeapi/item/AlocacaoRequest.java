package com.almoxe.almoxeapi.item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AlocacaoRequest(
        @NotNull
        UUID responsavelId,

        @NotNull
        UUID obraId,

        @Size(max = 500)
        String observacao
) {}
