package com.empresa.gestao_cartoes.repository;

import com.empresa.gestao_cartoes.model.Enderecos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnderecosRepository extends JpaRepository<Enderecos, Long> {
    
    List<Enderecos> findByPessoaId(Long pessoaId);
    
    List<Enderecos> findByPessoaIdAndTipo(Long pessoaId, Enderecos.TipoEndereco tipo);
    
    Optional<Enderecos> findByPessoaIdAndPrincipalTrue(Long pessoaId);
    
    @Query("SELECT e FROM Endereco e WHERE e.pessoa.id = :pessoaId AND e.principal = true")
    Optional<Enderecos> findPrincipalByPessoaId(@Param("pessoaId") Long pessoaId);
    
    boolean existsByPessoaIdAndPrincipalTrue(Long pessoaId);
    
    long countByPessoaId(Long pessoaId);
}