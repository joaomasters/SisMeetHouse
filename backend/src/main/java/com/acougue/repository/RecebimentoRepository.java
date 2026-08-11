package com.acougue.repository;

import com.acougue.entity.RecebimentoMercadoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecebimentoRepository extends JpaRepository<RecebimentoMercadoria, Long> {
    List<RecebimentoMercadoria> findAllByOrderByCreatedAtDesc();
    List<RecebimentoMercadoria> findByFornecedorContainingIgnoreCaseOrderByCreatedAtDesc(String fornecedor);
}
