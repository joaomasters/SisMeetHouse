package com.acougue.repository;

import com.acougue.entity.PagamentoPix;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PagamentoPixRepository extends JpaRepository<PagamentoPix, Long> {
    Optional<PagamentoPix> findByMpPaymentId(Long mpPaymentId);
}
