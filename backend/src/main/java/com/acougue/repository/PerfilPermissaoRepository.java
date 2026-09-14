package com.acougue.repository;

import com.acougue.entity.Modulo;
import com.acougue.entity.PerfilPermissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilPermissaoRepository extends JpaRepository<PerfilPermissao, Long> {

    List<PerfilPermissao> findByPerfilId(Long perfilId);

    Optional<PerfilPermissao> findByPerfilIdAndModulo(Long perfilId, Modulo modulo);

    void deleteByPerfilId(Long perfilId);
}