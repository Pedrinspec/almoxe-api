package com.almoxe.almoxeapi.movimentacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, UUID> {

    List<Movimentacao> findByItemEstoqueIdOrderByDataHoraAsc(UUID itemEstoqueId);
}
