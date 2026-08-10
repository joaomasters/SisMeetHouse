package com.acougue.repository;

import com.acougue.entity.InventarioFisicoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioFisicoItemRepository extends JpaRepository<InventarioFisicoItem, Long> {
    List<InventarioFisicoItem> findByInventarioIdOrderByProdutoNome(Long inventarioId);
    Optional<InventarioFisicoItem> findByInventarioIdAndProdutoId(Long inventarioId, Long produtoId);
}
