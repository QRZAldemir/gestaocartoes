package com.empresa.gestao_cartoes.repository;

import com.empresa.gestao_cartoes.model.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Long> {

    Optional<Permissao> findByNome(String nome);

    List<Permissao> findByModulo(String modulo);

    List<Permissao> findByAtivoTrue();

    @Query("SELECT p FROM Permissao p WHERE p.nome LIKE %:termo% OR p.descricao LIKE %:termo%")
    List<Permissao> findByTermo(@Param("termo") String termo);

    @Query("SELECT p FROM Permissao p WHERE p.modulo = :modulo ORDER BY p.nome")
    List<Permissao> findByModuloOrderByNome(@Param("modulo") String modulo);

    @Query("SELECT DISTINCT p.modulo FROM Permissao p ORDER BY p.modulo")
    List<String> findModulosDistinct();

    boolean existsByNome(String nome);
}
