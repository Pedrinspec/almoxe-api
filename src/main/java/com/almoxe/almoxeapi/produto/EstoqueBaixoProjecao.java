package com.almoxe.almoxeapi.produto;

import java.math.BigDecimal;

public record EstoqueBaixoProjecao(Produto produto, BigDecimal quantidadeDisponivel) {
}
