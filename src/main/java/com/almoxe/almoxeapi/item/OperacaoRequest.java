package com.almoxe.almoxeapi.item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OperacaoRequest(
        @NotNull
        UUID usuarioId,

        @Size(max = 500)
        String observacao
) {}
