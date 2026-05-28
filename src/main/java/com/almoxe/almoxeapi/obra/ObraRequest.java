package com.almoxe.almoxeapi.obra;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ObraRequest(
        @NotBlank
        @Size(max = 150)
        String nome,

        @Size(max = 250)
        String endereco
) {}
