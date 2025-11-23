package com.empresa.gestao_cartoes.util;

import com.empresa.gestao_cartoes.model.*;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseUpdater {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void updateDatabase() {
        LocalDateTime now = LocalDateTime.now();

        // Criar ou atualizar perfis padrão
        createOrUpdatePerfil("ADMIN", "Administrador do Sistema", 10, now);
        createOrUpdatePerfil("GERENTE", "Gerente", 8, now);
        createOrUpdatePerfil("ATENDENTE", "Atendente", 5, now);
        createOrUpdatePerfil("CLIENTE", "Cliente", 1, now);

        // Criar ou atualizar permissões padrão
        createOrUpdatePermissao("GERENCIAR_USUARIOS", "Gerenciar usuários do sistema", "SISTEMA", now);
        createOrUpdatePermissao("ACESSO_TOTAL", "Acesso total ao sistema", "SISTEMA", now);
        createOrUpdatePermissao("GERENCIAR_CLIENTES", "Gerenciar clientes", "CLIENTES", now);
        createOrUpdatePermissao("BLOQUEAR_CARTOES", "Bloquear cartões", "CARTOES", now);
        createOrUpdatePermissao("VISUALIZAR_RELATORIOS", "Visualizar relatórios", "RELATORIOS", now);

        // Associar permissões aos perfis
        associatePermissionWithProfile("GERENCIAR_USUARIOS", "ADMIN");
        associatePermissionWithProfile("ACESSO_TOTAL", "ADMIN");
        associatePermissionWithProfile("GERENCIAR_CLIENTES", "GERENTE");
        associatePermissionWithProfile("GERENCIAR_CLIENTES", "ATENDENTE");
        associatePermissionWithProfile("BLOQUEAR_CARTOES", "ATENDENTE");
        associatePermissionWithProfile("VISUALIZAR_RELATORIOS", "GERENTE");
        associatePermissionWithProfile("VISUALIZAR_RELATORIOS", "ADMIN");

        // Criar usuário administrador padrão se não existir
        createDefaultAdmin();
    }

    private void createOrUpdatePerfil(String nome, String descricao, int nivelAcesso, LocalDateTime now) {
        Perfil existing = entityManager.createQuery(
            "SELECT p FROM Perfil p WHERE p.nome = :nome", Perfil.class)
            .setParameter("nome", nome)
            .getResultStream()
            .findFirst()
            .orElse(null);

        if (existing == null) {
            // Criar novo perfil
            Perfil perfil = new Perfil();
            perfil.setNome(nome);
            perfil.setDescricao(descricao);
            perfil.setNivelAcesso(nivelAcesso);
            perfil.setDataCriacao(now);
            perfil.setDataAtualizacao(now);
            entityManager.persist(perfil);
        } else {
            // Atualizar perfil existente
            existing.setDescricao(descricao);
            existing.setNivelAcesso(nivelAcesso);
            existing.setDataAtualizacao(now);
            entityManager.merge(existing);
        }
    }

    private void createOrUpdatePermissao(String nome, String descricao, String modulo, LocalDateTime now) {
        Permissao existing = entityManager.createQuery(
            "SELECT p FROM Permissao p WHERE p.nome = :nome", Permissao.class)
            .setParameter("nome", nome)
            .getResultStream()
            .findFirst()
            .orElse(null);

        if (existing == null) {
            // Criar nova permissão
            Permissao permissao = new Permissao();
            permissao.setNome(nome);
            permissao.setDescricao(descricao);
            permissao.setModulo(modulo);
            permissao.setDataCriacao(now);
            permissao.setDataAtualizacao(now);
            entityManager.persist(permissao);
        } else {
            // Atualizar permissão existente
            existing.setDescricao(descricao);
            existing.setModulo(modulo);
            existing.setDataAtualizacao(now);
            entityManager.merge(existing);
        }
    }

    private void associatePermissionWithProfile(String permissaoNome, String perfilNome) {
        Permissao permissao = entityManager.createQuery(
            "SELECT p FROM Permissao p WHERE p.nome = :nome", Permissao.class)
            .setParameter("nome", permissaoNome)
            .getSingleResult();

        Perfil perfil = entityManager.createQuery(
            "SELECT p FROM Perfil p WHERE p.nome = :nome", Perfil.class)
            .setParameter("nome", perfilNome)
            .getSingleResult();

        // Verifica se a permissão já não está associada ao perfil
        if (!perfil.getPermissoes().contains(permissao)) {
            perfil.getPermissoes().add(permissao);
            entityManager.merge(perfil);
        }
    }

    private void createDefaultAdmin() {
        // Verificar se já existe um usuário admin
        Long adminCount = entityManager.createQuery(
            "SELECT COUNT(u) FROM Usuarios u WHERE u.username = :username", Long.class)
            .setParameter("username", "admin")
            .getSingleResult();

        if (adminCount == 0) {
            // Criar usuário admin
            Usuarios admin = new Usuarios();
            admin.setUsername("admin");
            admin.setSenha("admin123"); // Será criptografado pelo serviço
            admin.setEmailPrincipal("admin@empresa.com");
            admin.setStatus(Pessoas.StatusPessoa.ATIVO);
            admin.setDataCadastro(LocalDateTime.now());
            admin.setDataAtualizacao(LocalDateTime.now());

            // Associar perfil admin
            Perfil perfilAdmin = entityManager.createQuery(
                "SELECT p FROM Perfil p WHERE p.nome = :nome", Perfil.class)
                .setParameter("nome", "ADMIN")
                .getSingleResult();

            // Criar um Set para armazenar os perfis
            Set<Perfil> perfis = new HashSet<>();
            perfis.add(perfilAdmin);

            // Correção: Usar o método correto para associar perfis ao usuário
            // ANTES: admin.setPerfis(perfis) - MÉTODO INEXISTENTE
            // DEPOIS: admin.adicionarPerfil(perfilAdmin) - MÉTODO CORRETO
            // Explicação: O método setPerfis() não existe na classe Usuarios.
            // Em vez disso, usamos o método adicionarPerfil() para associar perfis ao usuário.
            admin.adicionarPerfil(perfilAdmin);

            entityManager.persist(admin);
        }
    }
}
