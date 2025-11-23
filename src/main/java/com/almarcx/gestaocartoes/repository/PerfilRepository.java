package com.empresa.gestao_cartoes.repository;

import com.empresa.gestao_cartoes.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    // Busca um perfil pelo nome
    Optional<Perfil> findByNome(String nome);

    // Lista perfis que estão ativos
    // ANTES: findByAtivo(Boolean) - MÉTODO INEXISTENTE
    // DEPOIS: findByAtivoTrue() - MÉTODO CORRETO
    // Explicação: O método findByAtivo(Boolean) não existe no Spring Data JPA.
    // Em vez disso, usamos findByAtivoTrue() que gera a consulta SQL correta.
    List<Perfil> findByAtivoTrue();

    // Lista perfis com nível de acesso maior ou igual ao informado
    // ANTES: findByNivelAcessoMinimo(Integer) - MÉTODO INEXISTENTE
    // DEPOIS: findByNivelAcessoGreaterThanEqual(Integer) - MÉTODO CORRETO
    // Explicação: O método findByNivelAcessoMinimo() não existe no Spring Data JPA.
    // Em vez disso, usamos findByNivelAcessoGreaterThanEqual() que busca perfis
    // com nível de acesso maior ou igual ao valor informado.
    List<Perfil> findByNivelAcessoGreaterThanEqual(Integer nivelMinimo);

    // Busca perfis por termo (nome ou descrição)
    // ANTES: Não existia
    // DEPOIS: Adicionado método com query customizada
    // Explicação: Este método foi adicionado para permitir a busca por termo que
    // pode ser parte do nome ou da descrição do perfil.
    @Query("SELECT p FROM Perfil p WHERE p.nome LIKE %:termo% OR p.descricao LIKE %:termo%")
    List<Perfil> findByTermo(@Param("termo") String termo);

    // Busca perfis por nível de acesso exato
    @Query("SELECT p FROM Perfil p WHERE p.nivelAcesso = :nivel")
    List<Perfil> findByNivelAcesso(@Param("nivel") Integer nivel);

    // Verifica se existe um perfil com o nome informado
    boolean existsByNome(String nome);
}
