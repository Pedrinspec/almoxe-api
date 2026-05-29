package com.almoxe.almoxeapi.item;

import jakarta.validation.constraints.Size;

public record OperacaoRequest(
        @Size(max = 500)
        String observacao
) {}
