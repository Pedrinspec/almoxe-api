package com.almoxe.almoxeapi.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    @Query("""
            SELECT new com.almoxe.almoxeapi.produto.EstoqueBaixoProjecao(p, COALESCE(SUM(i.quantidade), 0))
            FROM Produto p
            LEFT JOIN ItemEstoque i ON i.produto = p AND i.status = com.almoxe.almoxeapi.item.StatusItem.DISPONIVEL
            GROUP BY p
            HAVING COALESCE(SUM(i.quantidade), 0) < p.estoqueMinimo
            """)
    List<EstoqueBaixoProjecao> findAbaixoDoEstoqueMinimo();
}
