package com.acougue.repository;

import com.acougue.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByAtivoTrue();

    List<Cliente> findByTipoClienteAndAtivoTrue(String tipoCliente);

    Optional<Cliente> findByCpfCnpj(String cpfCnpj);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND c.ativo = true")
    List<Cliente> buscarPorNome(@Param("nome") String nome);
}
