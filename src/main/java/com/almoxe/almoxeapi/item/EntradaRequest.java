package com.almoxe.almoxeapi.item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record EntradaRequest(
        @NotNull
        UUID produtoId,

        @NotNull
        @Positive
        BigDecimal quantidade,

        String numeroSerie,
        String lote,
        String notaFiscal,
        String numeroRi,

        @Size(max = 500)
        String observacao
) {}
