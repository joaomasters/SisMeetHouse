package com.acougue.repository;

import com.acougue.entity.CargaAgendada;
import com.acougue.entity.ItemPendenteBalanca;
import com.acougue.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemPendenteBalancaRepository extends JpaRepository<ItemPendenteBalanca, Long> {

    Optional<ItemPendenteBalanca> findByProdutoAndStatus(Produto produto, String status);

    List<ItemPendenteBalanca> findByStatusOrderByCriadoEmAsc(String status);

    long countByStatus(String status);

    @Modifying
    @Query("UPDATE ItemPendenteBalanca i SET i.status = 'ENVIADO', i.cargaAgendada = :carga, " +
            "i.enviadoEm = CURRENT_TIMESTAMP WHERE i.id IN :ids")
    void marcarComoEnviados(@Param("ids") List<Long> ids, @Param("carga") CargaAgendada carga);
}
