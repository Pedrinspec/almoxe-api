package com.almoxe.almoxeapi.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, UUID> {

    Optional<ItemEstoque> findFirstByProdutoIdAndStatus(UUID produtoId, StatusItem status);
}
