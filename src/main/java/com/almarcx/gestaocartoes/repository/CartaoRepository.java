package com.empresa.gestao_cartoes.repository;

import com.empresa.gestao_cartoes.model.Cartoes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartaoRepository extends JpaRepository<Cartoes, Long> {

    // Consultas básicas por atributos
    List<Cartoes> findByStatus(Cartoes.StatusCartao status);
    List<Cartoes> findByBandeira(Cartoes.BandeiraCartao bandeira); 
    List<Cartoes> findByTipoCartao(Cartoes.TipoCartao tipo);
    
    // CORREÇÃO: Mantenha apenas um método para busca por número do cartão
    Optional<Cartoes> findByNumeroCartao(String numeroCartao);
    
    List<Cartoes> findByPrincipal(Boolean principal);
    List<Cartoes> findByContactless(Boolean contactless);
    
    // Consultas por relacionamentos
    List<Cartoes> findByClienteId(Long clienteId);
    List<Cartoes> findByContratoId(Long contratoId);
    
    // Consultas por datas
    List<Cartoes> findByDataValidadeAfter(LocalDate date);
    List<Cartoes> findByDataValidadeBefore(LocalDate date);
    List<Cartoes> findByDataValidadeBetween(LocalDate start, LocalDate end);
    
    // Consultas por valores monetários
    List<Cartoes> findByLimiteGreaterThan(BigDecimal valor);
    List<Cartoes> findByLimiteLessThan(BigDecimal valor);
    List<Cartoes> findByLimiteBetween(BigDecimal min, BigDecimal max);
    List<Cartoes> findByLimiteUtilizadoGreaterThan(BigDecimal valor);
    
    // Consultas combinadas
    List<Cartoes> findByStatusAndBandeira(Cartoes.StatusCartao status, Cartoes.BandeiraCartao bandeira);
    
    // Consultas com JPQL
    @Query("SELECT c FROM Cartoes c WHERE c.dataValidade < CURRENT_DATE")
    List<Cartoes> findCartoesVencidos();
    
    @Query("SELECT c FROM Cartoes c WHERE c.cliente.id = :clienteId AND c.principal = true")
    List<Cartoes> findCartaoPrincipalDoCliente(@Param("clienteId") Long clienteId);
    
    @Query("SELECT c FROM Cartoes c WHERE LOWER(c.nomePortador) LIKE LOWER(concat('%', :nome, '%'))")
    List<Cartoes> findByNomePortadorSimilar(@Param("nome") String nome);
    
    // Consulta para encontrar cartões com limite disponível
    @Query("SELECT c FROM Cartoes c WHERE (c.limite - c.limiteUtilizado) >= :valorMinimo")
    List<Cartoes> findComLimiteDisponivel(@Param("valorMinimo") BigDecimal valorMinimo);
    
    // Consulta para encontrar cartões ativos de um cliente específico
    @Query("SELECT c FROM Cartoes c WHERE c.cliente.id = :clienteId AND c.status = 'ATIVO'")
    List<Cartoes> findCartoesAtivosPorCliente(@Param("clienteId") Long clienteId);
    
    // Consulta para verificar se um número de cartão já existe
    @Query("SELECT COUNT(c) > 0 FROM Cartoes c WHERE c.numeroCartao = :numeroCartao")
    boolean existsByNumeroCartao(@Param("numeroCartao") String numeroCartao);
    
 
}