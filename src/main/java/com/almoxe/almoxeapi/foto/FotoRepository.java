package com.almoxe.almoxeapi.foto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FotoRepository extends JpaRepository<Foto, UUID> {

    List<Foto> findByMovimentacaoIdOrderByDataUploadAsc(UUID movimentacaoId);
}
