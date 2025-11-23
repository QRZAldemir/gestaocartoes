package com.empresa.gestao_cartoes.repository;

import com.empresa.gestao_cartoes.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuarios, Long> {

    Optional<Usuarios> findByUsername(String username);

    Optional<Usuarios> findByEmailPrincipal(String email);

    List<Usuarios> findByStatus(Usuarios.StatusPessoa status);

    List<Usuarios> findByBloqueadoFalse();

    @Query("SELECT u FROM Usuarios u WHERE u.username LIKE %:termo% OR u.emailPrincipal LIKE %:termo%")
    List<Usuarios> findByTermo(@Param("termo") String termo);

    @Query("SELECT u FROM Usuarios u JOIN u.perfis p WHERE p.nome = :perfil")
    List<Usuarios> findByPerfil(@Param("perfil") String perfil);

    @Query("SELECT u FROM Usuarios u JOIN u.perfis p WHERE p.nivelAcesso >= :nivel")
    List<Usuarios> findByNivelAcessoMinimo(@Param("nivel") Integer nivel);

    @Query("SELECT COUNT(u) > 0 FROM Usuarios u WHERE u.username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u) > 0 FROM Usuarios u WHERE u.emailPrincipal = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM Usuarios u WHERE u.tentativasLogin >= 5")
    List<Usuarios> findUsuariosBloqueadosPorTentativas();
}
