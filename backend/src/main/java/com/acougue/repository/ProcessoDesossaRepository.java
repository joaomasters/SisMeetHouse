package com.acougue.repository;

import com.acougue.entity.ProcessoDesossa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessoDesossaRepository extends JpaRepository<ProcessoDesossa, Long> {

    List<ProcessoDesossa> findByFichaDesossaIdOrderByDataProcessoDesc(Long fichaDesossaId);

    List<ProcessoDesossa> findByRecebimentoId(Long recebimentoId);
}
