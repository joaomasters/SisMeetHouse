package com.acougue.repository;

import com.acougue.entity.ProcessoDesossa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessoDesossaRepository extends JpaRepository<ProcessoDesossa, Long> {

    List<ProcessoDesossa> findByFichaDesossaIdOrderByDataProcessoDesc(Long fichaDesossaId);

    List<ProcessoDesossa> findByFichaDesossaIdAndDataProcessoBetweenOrderByDataProcessoDesc(
            Long fichaDesossaId, LocalDateTime inicio, LocalDateTime fim);

    List<ProcessoDesossa> findByRecebimentoId(Long recebimentoId);
}