package com.acougue.repository;

import com.acougue.entity.SangriaCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SangriaCaixaRepository extends JpaRepository<SangriaCaixa, Long> {
    List<SangriaCaixa> findByCaixaIdOrderByCreatedAtDesc(Long caixaId);
}
