package com.acougue.repository;

import com.acougue.entity.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaixaRepository extends JpaRepository<Caixa, Long> {

    Optional<Caixa> findFirstByOperadorIdAndStatus(Long operadorId, String status);

    Optional<Caixa> findFirstByStatusOrderByDataAberturaDesc(String status);
}
