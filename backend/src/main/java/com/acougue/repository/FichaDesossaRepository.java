package com.acougue.repository;

import com.acougue.entity.FichaDesossa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaDesossaRepository extends JpaRepository<FichaDesossa, Long> {

    List<FichaDesossa> findByAtivoTrue();

    Optional<FichaDesossa> findByProdutoPaiIdAndAtivoTrue(Long produtoPaiId);
}
