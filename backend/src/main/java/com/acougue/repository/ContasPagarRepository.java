package com.acougue.repository;

import com.acougue.entity.ContasPagar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContasPagarRepository extends JpaRepository<ContasPagar, Long> {
    List<ContasPagar> findByStatusOrderByDataVencimentoAsc(String status);
    List<ContasPagar> findByDataVencimentoBetweenOrderByDataVencimentoAsc(LocalDate inicio, LocalDate fim);
    List<ContasPagar> findByDataVencimentoBeforeAndStatusOrderByDataVencimentoAsc(LocalDate data, String status);
}
