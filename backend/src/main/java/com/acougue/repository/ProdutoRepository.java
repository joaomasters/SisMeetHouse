package com.acougue.repository;

import com.acougue.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigoBalanca(Integer codigoBalanca);

    Optional<Produto> findByEan13(String ean13);

    Optional<Produto> findByCodigoInterno(String codigoInterno);

    List<Produto> findByAtivoTrue();

    List<Produto> findByCodigoBalancaIsNotNullAndAtivoTrue();

    @Query("SELECT p FROM Produto p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND p.ativo = true")
    List<Produto> buscarPorNome(@Param("nome") String nome);

    @Query("SELECT p FROM Produto p WHERE p.estoqueAtual <= p.estoqueMinimo AND p.ativo = true")
    List<Produto> findEstoqueAbaixoMinimo();

    List<Produto> findAllByAtivoTrue();
}
