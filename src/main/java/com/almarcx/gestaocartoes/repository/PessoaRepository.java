package com.empresa.gestao_cartoes.repository;

import com.empresa.gestao_cartoes.model.Pessoas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoas, Long> {
    
    Optional<Pessoas> findByEmailPrincipal(String email);
    
    List<Pessoas> findByStatus(Pessoas.StatusPessoa status);
    
    @Query("SELECT p FROM Pessoas p WHERE p.tipoPessoa = :tipo")
    List<Pessoas> findByTipoPessoa(@Param("tipo") String tipo);
    
    @Query("SELECT p FROM Pessoas p WHERE p.uf = :uf")
    List<Pessoas> findByUf(@Param("uf") String uf);
    
    boolean existsByEmailPrincipal(String email);
}