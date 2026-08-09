package com.acougue.repository;

import com.acougue.entity.CargaBalanca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargaBalancaRepository extends JpaRepository<CargaBalanca, Long> {

    List<CargaBalanca> findByTipoBalancaAndStatus(String tipoBalanca, String status);

    List<CargaBalanca> findByProdutoIdOrderByDataEnvioDesc(Long produtoId);
}
