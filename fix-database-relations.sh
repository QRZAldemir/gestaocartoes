#!/bin/bash

# ==============================================
# Script para corrigir relacionamentos entre tabelas no banco de dados
# Sistema de Gestão de Cartões
# ==============================================

echo "🔧 Corrigindo relacionamentos entre tabelas - Gestão de Cartões"
echo "============================================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# 1. CORREÇÃO DA CLASSE USUARIOS.JAVA
# ====================================
echo -e "${BLUE}=== CORREÇÃO DA CLASSE USUARIOS.JAVA ===${NC}"

echo "Criando nova versão da classe Usuarios.java com métodos para gerenciar perfis..."

cat > /tmp/Usuarios.java << 'EOF'
package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa um usuário do sistema
 */
@Entity
@Table(name = "usuarios")
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String senha;

    @Column(unique = true, nullable = false)
    private String emailPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPessoa status;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuarios_perfis",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfis = new HashSet<>();

    // Construtor padrão
    public Usuarios() {
        this.dataCadastro = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.status = StatusPessoa.ATIVO;
    }

    // Métodos getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public String getEmailPrincipal() {
        return emailPrincipal;
    }

    public void setEmailPrincipal(String emailPrincipal) {
        this.emailPrincipal = emailPrincipal;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public StatusPessoa getStatus() {
        return status;
    }

    public void setStatus(StatusPessoa status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    /**
     * Obtém o conjunto de perfis associados a este usuário
     * 
     * @return Set de perfis
     */
    public Set<Perfil> getPerfis() {
        return perfis;
    }

    /**
     * Define o conjunto de perfis para este usuário
     * 
     * @param perfis Set de perfis a ser associado
     */
    public void setPerfis(Set<Perfil> perfis) {
        this.perfis = perfis != null ? perfis : new HashSet<>();
        this.dataAtualizacao = LocalDateTime.now();
    }

    /**
     * Adiciona um perfil ao usuário
     * 
     * @param perfil Perfil a ser adicionado
     * @return true se o perfil foi adicionado com sucesso, false caso contrário
     */
    public boolean adicionarPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil não pode ser nulo");
        }

        boolean adicionado = this.perfis.add(perfil);
        if (adicionado) {
            this.dataAtualizacao = LocalDateTime.now();
        }
        return adicionado;
    }

    /**
     * Remove um perfil do usuário
     * 
     * @param perfil Perfil a ser removido
     * @return true se o perfil foi removido com sucesso, false caso contrário
     */
    public boolean removerPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil não pode ser nulo");
        }

        boolean removido = this.perfis.remove(perfil);
        if (removido) {
            this.dataAtualizacao = LocalDateTime.now();
        }
        return removido;
    }

    /**
     * Verifica se o usuário tem um perfil específico
     * 
     * @param perfil Perfil a ser verificado
     * @return true se o usuário possui o perfil, false caso contrário
     */
    public boolean temPerfil(Perfil perfil) {
        if (perfil == null) {
            return false;
        }
        return this.perfis.contains(perfil);
    }

    /**
     * Verifica se o usuário tem um perfil pelo nome
     * 
     * @param nomePerfil Nome do perfil a ser verificado
     * @return true se o usuário possui o perfil, false caso contrário
     */
    public boolean temPerfil(String nomePerfil) {
        if (nomePerfil == null || nomePerfil.trim().isEmpty()) {
            return false;
        }

        return this.perfis.stream()
            .anyMatch(p -> nomePerfil.equals(p.getNome()));
    }

    /**
     * Ativa a conta do usuário
     */
    public void ativar() {
        this.status = StatusPessoa.ATIVO;
        this.dataAtualizacao = LocalDateTime.now();
    }

    /**
     * Desativa a conta do usuário
     */
    public void desativar() {
        this.status = StatusPessoa.INATIVO;
        this.dataAtualizacao = LocalDateTime.now();
    }

    /**
     * Verifica se a conta do usuário está ativa
     * 
     * @return true se a conta está ativa, false caso contrário
     */
    public boolean isAtivo() {
        return this.status == StatusPessoa.ATIVO;
    }

    @Override
    public String toString() {
        return "Usuarios{" +
                "id=" + id +
                ", username='" + username + ''' +
                ", email='" + emailPrincipal + ''' +
                ", status=" + status +
                ", perfis=" + perfis.size() +
                '}';
    }
}
EOF

# Substituir o arquivo original
mv /tmp/Usuarios.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/model/Usuarios.java
log "✅ Arquivo Usuarios.java corrigido com métodos para gerenciar perfis."

# 2. CORREÇÃO DO ARQUIVO DATABASEUPDATER.JAVA
# ============================================
echo -e "${BLUE}=== CORREÇÃO DO ARQUIVO DATABASEUPDATER.JAVA ===${NC}"

echo "Criando nova versão do DatabaseUpdater.java com correções..."

cat > /tmp/DatabaseUpdater.java << 'EOF'
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

    /**
     * Atualiza o banco de dados com dados iniciais
     */
    @Transactional
    public void updateDatabase() {
        LocalDateTime now = LocalDateTime.now();
        log("Iniciando atualização do banco de dados em: " + now);

        // Criar ou atualizar perfis padrão
        log("Criando perfis padrão...");
        createOrUpdatePerfil("ADMIN", "Administrador do Sistema", 10, now);
        createOrUpdatePerfil("GERENTE", "Gerente", 8, now);
        createOrUpdatePerfil("ATENDENTE", "Atendente", 5, now);
        createOrUpdatePerfil("CLIENTE", "Cliente", 1, now);

        // Criar ou atualizar permissões padrão
        log("Criando permissões padrão...");
        createOrUpdatePermissao("GERENCIAR_USUARIOS", "Gerenciar usuários do sistema", "SISTEMA", now);
        createOrUpdatePermissao("ACESSO_TOTAL", "Acesso total ao sistema", "SISTEMA", now);
        createOrUpdatePermissao("GERENCIAR_CLIENTES", "Gerenciar clientes", "CLIENTES", now);
        createOrUpdatePermissao("BLOQUEAR_CARTOES", "Bloquear cartões", "CARTOES", now);
        createOrUpdatePermissao("VISUALIZAR_RELATORIOS", "Visualizar relatórios", "RELATORIOS", now);

        // Associar permissões aos perfis
        log("Associando permissões aos perfis...");
        associatePermissionWithProfile("GERENCIAR_USUARIOS", "ADMIN");
        associatePermissionWithProfile("ACESSO_TOTAL", "ADMIN");
        associatePermissionWithProfile("GERENCIAR_CLIENTES", "GERENTE");
        associatePermissionWithProfile("GERENCIAR_CLIENTES", "ATENDENTE");
        associatePermissionWithProfile("BLOQUEAR_CARTOES", "ATENDENTE");
        associatePermissionWithProfile("VISUALIZAR_RELATORIOS", "GERENTE");
        associatePermissionWithProfile("VISUALIZAR_RELATORIOS", "ADMIN");

        // Criar usuário administrador padrão se não existir
        log("Criando usuário administrador padrão...");
        createDefaultAdmin();

        log("Atualização do banco de dados concluída!");
    }

    /**
     * Cria ou atualiza um perfil
     */
    private void createOrUpdatePerfil(String nome, String descricao, int nivelAcesso, LocalDateTime now) {
        log("Processando perfil: " + nome);

        Perfil existing = entityManager.createQuery(
            "SELECT p FROM Perfil p WHERE p.nome = :nome", Perfil.class)
            .setParameter("nome", nome)
            .getResultStream()
            .findFirst()
            .orElse(null);

        if (existing == null) {
            // Criar novo perfil
            log("Criando novo perfil: " + nome);
            Perfil perfil = new Perfil();
            perfil.setNome(nome);
            perfil.setDescricao(descricao);
            perfil.setNivelAcesso(nivelAcesso);
            perfil.setDataCriacao(now);
            perfil.setDataAtualizacao(now);
            entityManager.persist(perfil);
        } else {
            // Atualizar perfil existente
            log("Atualizando perfil existente: " + nome);
            existing.setDescricao(descricao);
            existing.setNivelAcesso(nivelAcesso);
            existing.setDataAtualizacao(now);
            entityManager.merge(existing);
        }
    }

    /**
     * Cria ou atualiza uma permissão
     */
    private void createOrUpdatePermissao(String nome, String descricao, String modulo, LocalDateTime now) {
        log("Processando permissão: " + nome);

        Permissao existing = entityManager.createQuery(
            "SELECT p FROM Permissao p WHERE p.nome = :nome", Permissao.class)
            .setParameter("nome", nome)
            .getResultStream()
            .findFirst()
            .orElse(null);

        if (existing == null) {
            // Criar nova permissão
            log("Criando nova permissão: " + nome);
            Permissao permissao = new Permissao();
            permissao.setNome(nome);
            permissao.setDescricao(descricao);
            permissao.setModulo(modulo);
            permissao.setDataCriacao(now);
            permissao.setDataAtualizacao(now);
            entityManager.persist(permissao);
        } else {
            // Atualizar permissão existente
            log("Atualizando permissão existente: " + nome);
            existing.setDescricao(descricao);
            existing.setModulo(modulo);
            existing.setDataAtualizacao(now);
            entityManager.merge(existing);
        }
    }

    /**
     * Associa uma permissão a um perfil
     */
    private void associatePermissionWithProfile(String permissaoNome, String perfilNome) {
        log("Associando permissão " + permissaoNome + " ao perfil " + perfilNome);

        Permissao permissao = entityManager.createQuery(
            "SELECT p FROM Permissao p WHERE p.nome = :nome", Permissao.class)
            .setParameter("nome", permissaoNome)
            .getSingleResult();

        Perfil perfil = entityManager.createQuery(
            "SELECT p FROM Perfil p WHERE p.nome = :nome", Perfil.class)
            .setParameter("nome", perfilNome)
            .getSingleResult();

        // Adicionar permissão ao perfil se ainda não estiver associada
        if (!perfil.getPermissoes().contains(permissao)) {
            perfil.getPermissoes().add(permissao);
            entityManager.merge(perfil);
            log("Permissão " + permissaoNome + " associada ao perfil " + perfilNome);
        } else {
            log("Permissão " + permissaoNome + " já está associada ao perfil " + perfilNome);
        }
    }

    /**
     * Cria o usuário administrador padrão
     */
    private void createDefaultAdmin() {
        log("Verificando se o usuário admin já existe...");

        // Verificar se já existe um usuário admin
        Long adminCount = entityManager.createQuery(
            "SELECT COUNT(u) FROM Usuarios u WHERE u.username = :username", Long.class)
            .setParameter("username", "admin")
            .getSingleResult();

        if (adminCount == 0) {
            // Criar usuário admin
            log("Criando novo usuário admin...");
            Usuarios admin = new Usuarios();
            admin.setUsername("admin");
            admin.setSenha("admin123"); // Será criptografado pelo serviço
            admin.setEmailPrincipal("admin@empresa.com");
            admin.setStatus(Pessoas.StatusPessoa.ATIVO);
            admin.setDataCadastro(LocalDateTime.now());
            admin.setDataAtualizacao(LocalDateTime.now());

            // Associar perfil admin
            log("Associando perfil ADMIN ao usuário admin...");
            Perfil perfilAdmin = entityManager.createQuery(
                "SELECT p FROM Perfil p WHERE p.nome = :nome", Perfil.class)
                .setParameter("nome", "ADMIN")
                .getSingleResult();

            // Usar o método corrigido para adicionar perfis ao usuário
            Set<Perfil> perfis = new HashSet<>();
            perfis.add(perfilAdmin);
            admin.setPerfis(perfis);

            entityManager.persist(admin);
            log("Usuário admin criado com sucesso!");
        } else {
            log("Usuário admin já existe. Atualizando perfis...");

            // Atualizar perfis do usuário admin existente
            Usuarios admin = entityManager.createQuery(
                "SELECT u FROM Usuarios u WHERE u.username = :username", Usuarios.class)
                .setParameter("username", "admin")
                .getSingleResult();

            Perfil perfilAdmin = entityManager.createQuery(
                "SELECT p FROM Perfil p WHERE p.nome = :nome", Perfil.class)
                .setParameter("nome", "ADMIN")
                .getSingleResult();

            // Adicionar perfil admin se não existir
            if (!admin.getPerfis().contains(perfilAdmin)) {
                admin.adicionarPerfil(perfilAdmin);
                entityManager.merge(admin);
                log("Perfil ADMIN associado ao usuário admin existente");
            } else {
                log("Perfil ADMIN já está associado ao usuário admin existente");
            }
        }
    }

    /**
     * Método auxiliar para logs
     */
    private void log(String message) {
        System.out.println("[DatabaseUpdater] " + message);
    }
}
EOF

# Substituir o arquivo original
mv /tmp/DatabaseUpdater.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/util/DatabaseUpdater.java
log "✅ Arquivo DatabaseUpdater.java corrigido com explicações."

# 3. CORREÇÃO DO ARQUIVO PERFILREPOSITORY.JAVA
# ============================================
echo -e "${BLUE}=== CORREÇÃO DO ARQUIVO PERFILREPOSITORY.JAVA ===${NC}"

echo "Criando nova versão do PerfilRepository.java com métodos necessários..."

cat > /tmp/PerfilRepository.java << 'EOF'
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

    /**
     * Busca um perfil pelo nome
     * 
     * @param nome Nome do perfil
     * @return Optional com o perfil encontrado
     */
    Optional<Perfil> findByNome(String nome);

    /**
     * Busca todos os perfis ativos
     * 
     * @return Lista de perfis ativos
     */
    List<Perfil> findByAtivoTrue();

    /**
     * Busca perfis com nível de acesso maior ou igual ao especificado
     * 
     * ANTES: findByNivelAcessoMinimo() - MÉTODO INEXISTENTE
     * DEPOIS: findByNivelAcessoGreaterThanEqual() - MÉTODO CORRETO
     * 
     * @param nivelMinimo Nível de acesso mínimo
     * @return Lista de perfis com nível de acesso maior ou igual
     */
    List<Perfil> findByNivelAcessoGreaterThanEqual(Integer nivelMinimo);

    /**
     * Busca perfis por termo (nome ou descrição)
     * 
     * ANTES: Não existia
     * DEPOIS: Adicionado para suportar busca por termo
     * 
     * @param termo Termo para busca
     * @return Lista de perfis que correspondem ao termo
     */
    @Query("SELECT p FROM Perfil p WHERE p.nome LIKE %:termo% OR p.descricao LIKE %:termo%")
    List<Perfil> findByTermo(@Param("termo") String termo);

    /**
     * Busca perfis por nível de acesso específico
     * 
     * @param nivel Nível de acesso
     * @return Lista de perfis com o nível de acesso especificado
     */
    @Query("SELECT p FROM Perfil p WHERE p.nivelAcesso = :nivel")
    List<Perfil> findByNivelAcesso(@Param("nivel") Integer nivel);

    /**
     * Verifica se existe um perfil com o nome especificado
     * 
     * @param nome Nome do perfil
     * @return true se existir, false caso contrário
     */
    boolean existsByNome(String nome);
}
EOF

# Substituir o arquivo original
mv /tmp/PerfilRepository.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/repository/PerfilRepository.java
log "✅ Arquivo PerfilRepository.java corrigido com explicações."

echo -e "${GREEN}✅ Todas as correções foram aplicadas com sucesso!${NC}"
echo -e "${GREEN}Os arquivos foram atualizados com explicações detalhadas.${NC}"
echo -e "${YELLOW}Para compilar a aplicação, execute:${NC}"
echo -e "${YELLOW}  mvn clean compile${NC}"
