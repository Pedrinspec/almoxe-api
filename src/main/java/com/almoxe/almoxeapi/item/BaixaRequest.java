package com.almoxe.almoxeapi.item;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record BaixaRequest(
        @Positive
        BigDecimal quantidade,

        UUID obraId,

        @Size(max = 500)
        String observacao
) {}
