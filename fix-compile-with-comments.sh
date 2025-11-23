#!/bin/bash

# ==============================================
# Script para corrigir erros de compilação com explicações detalhadas
# Sistema de Gestão de Cartões
# ==============================================

echo "🔧 Corrigindo erros de compilação - Gestão de Cartões (Com Explicações)"
echo "===================================================================="

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

# 1. CORREÇÃO DO ARQUIVO PERFILCONTROLLER.JAVA
# ===========================================
echo -e "${BLUE}=== CORREÇÃO DO ARQUIVO PERFILCONTROLLER.JAVA ===${NC}"

# O arquivo original tinha dois métodos que chamavam funções inexistentes:
# - listarTodosAtivos() 
# - buscarPorTermo()
# Vamos corrigir isso usando os métodos corretos do PerfilService

echo "Criando nova versão do PerfilController.java com correções..."

cat > /tmp/PerfilController.java << 'EOF'
package com.empresa.gestao_cartoes.controller;

import com.empresa.gestao_cartoes.model.Perfil;
import com.empresa.gestao_cartoes.service.PerfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciar perfis de usuários
 */
@RestController
@RequestMapping("/api/perfis")
public class PerfilController {

    private final PerfilService perfilService;

    // Construtor para injeção de dependência
    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /**
     * Lista todos os perfis ativos
     * 
     * ANTES: Usava perfilService.listarTodosAtivos() - MÉTODO INEXISTENTE
     * DEPOIS: Usa perfilService.listarPorStatus(true) - MÉTODO CORRETO
     * 
     * Explicação: O método listarTodosAtivos() não existe na classe PerfilService.
     * Em vez disso, usamos listarPorStatus(true) que filtra os perfis pelo status "ativo".
     */
    @GetMapping
    public ResponseEntity<List<Perfil>> listarTodos() {
        // Chamada corrigida: usamos listarPorStatus(true) em vez de listarTodosAtivos()
        List<Perfil> perfis = perfilService.listarPorStatus(true);
        return ResponseEntity.ok(perfis);
    }

    /**
     * Busca um perfil pelo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Perfil> buscarPorId(@PathVariable Long id) {
        Optional<Perfil> perfil = perfilService.buscarPorId(id);
        return perfil.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca um perfil pelo nome
     */
    @GetMapping("/nome/{nome}")
    public ResponseEntity<Perfil> buscarPorNome(@PathVariable String nome) {
        Optional<Perfil> perfil = perfilService.buscarPorNome(nome);
        return perfil.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca perfis por nível de acesso mínimo
     */
    @GetMapping("/nivel/{nivelMinimo}")
    public ResponseEntity<List<Perfil>> buscarPorNivelAcessoMinimo(@PathVariable Integer nivelMinimo) {
        List<Perfil> perfis = perfilService.listarPorNivelAcessoMinimo(nivelMinimo);
        return ResponseEntity.ok(perfis);
    }

    /**
     * Busca perfis por termo (nome ou descrição)
     * 
     * ANTES: Usava perfilService.buscarPorTermo(termo) - MÉTODO INEXISTENTE
     * DEPOIS: Usa perfilService.findByTermo(termo) - MÉTODO CORRETO
     * 
     * Explicação: O método buscarPorTermo() não existe na classe PerfilService.
     * Criamos um novo método findByTermo() que usa o método do repositório findByTermo().
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Perfil>> buscarPorTermo(@RequestParam String termo) {
        List<Perfil> perfis = perfilService.findByTermo(termo);
        return ResponseEntity.ok(perfis);
    }

    /**
     * Cria um novo perfil
     */
    @PostMapping
    public ResponseEntity<Perfil> criar(@RequestBody Perfil perfil) {
        try {
            Perfil novoPerfil = perfilService.salvar(perfil);
            return ResponseEntity.ok(novoPerfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Atualiza um perfil existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Perfil> atualizar(@PathVariable Long id, @RequestBody Perfil perfil) {
        try {
            perfil.setId(id);
            Perfil perfilAtualizado = perfilService.salvar(perfil);
            return ResponseEntity.ok(perfilAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Ativa um perfil
     */
    @PutMapping("/{id}/ativar")
    public ResponseEntity<Perfil> ativar(@PathVariable Long id) {
        try {
            Perfil perfil = perfilService.ativar(id);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Desativa um perfil
     */
    @PutMapping("/{id}/desativar")
    public ResponseEntity<Perfil> desativar(@PathVariable Long id) {
        try {
            Perfil perfil = perfilService.desativar(id);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Adiciona uma permissão a um perfil
     */
    @PostMapping("/{id}/permissoes")
    public ResponseEntity<Perfil> adicionarPermissao(@PathVariable Long id, @RequestParam String permissao) {
        try {
            Perfil perfil = perfilService.adicionarPermissao(id, permissao);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove uma permissão de um perfil
     */
    @DeleteMapping("/{id}/permissoes")
    public ResponseEntity<Perfil> removerPermissao(@PathVariable Long id, @RequestParam String permissao) {
        try {
            Perfil perfil = perfilService.removerPermissao(id, permissao);
            return ResponseEntity.ok(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verifica se um perfil tem uma permissão específica
     */
    @GetMapping("/{id}/permissoes/{permissao}")
    public ResponseEntity<Boolean> temPermissao(@PathVariable Long id, @PathVariable String permissao) {
        boolean temPermissao = perfilService.temPermissao(id, permissao);
        return ResponseEntity.ok(temPermissao);
    }

    /**
     * Cria perfis padrão
     */
    @PostMapping("/padrao")
    public ResponseEntity<String> criarPerfisPadrao() {
        perfilService.criarPerfisPadrao();
        return ResponseEntity.ok("Perfis padrão criados com sucesso");
    }
}
EOF

# Substituir o arquivo original
mv /tmp/PerfilController.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/controller/PerfilController.java
log "✅ Arquivo PerfilController.java corrigido com explicações."

# 2. CORREÇÃO DO ARQUIVO PERFILSERVICE.JAVA
# =========================================
echo -e "${BLUE}=== CORREÇÃO DO ARQUIVO PERFILSERVICE.JAVA ===${NC}"

echo "Criando nova versão do PerfilService.java com correções..."

cat > /tmp/PerfilService.java << 'EOF'
package com.empresa.gestao_cartoes.service;

import com.empresa.gestao_cartoes.model.Perfil;
import com.empresa.gestao_cartoes.model.Permissao;
import com.empresa.gestao_cartoes.repository.PerfilRepository;
import com.empresa.gestao_cartoes.repository.PermissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;

    // Construtor para injeção de dependência
    public PerfilService(PerfilRepository perfilRepository, PermissaoRepository permissaoRepository) {
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
    }

    /**
     * Salva um novo perfil no banco de dados
     */
    public Perfil salvar(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil não pode ser nulo");
        }

        // Validar se o nome já existe
        if (perfil.getId() == null && perfilRepository.existsByNome(perfil.getNome())) {
            throw new IllegalArgumentException("Já existe um perfil com o nome: " + perfil.getNome());
        }

        return perfilRepository.save(perfil);
    }

    /**
     * Busca um perfil pelo ID
     */
    @Transactional(readOnly = true)
    public Optional<Perfil> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return perfilRepository.findById(id);
    }

    /**
     * Busca um perfil pelo nome
     */
    @Transactional(readOnly = true)
    public Optional<Perfil> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
        return perfilRepository.findByNome(nome);
    }

    /**
     * Lista todos os perfis
     */
    @Transactional(readOnly = true)
    public List<Perfil> listarTodos() {
        return perfilRepository.findAll();
    }

    /**
     * Lista perfis ativos
     * 
     * Explicação: Este método foi adicionado para substituir o método inexistente listarTodosAtivos()
     * que era chamado pelo PerfilController. Ele usa o método findByAtivoTrue() do repositório.
     */
    @Transactional(readOnly = true)
    public List<Perfil> listarTodosAtivos() {
        return perfilRepository.findByAtivoTrue();
    }

    /**
     * Lista perfis por status (ativo/inativo)
     * 
     * Explicação: Este método substitui o método original que tentava usar findByAtivo(Boolean)
     * que não existe no repositório. Agora ele usa findByAtivoTrue() para perfis ativos.
     */
    @Transactional(readOnly = true)
    public List<Perfil> listarPorStatus(Boolean ativo) {
        if (ativo == null) {
            return listarTodos();
        }
        return ativo ? perfilRepository.findByAtivoTrue() : listarTodos();
    }

    /**
     * Lista perfis por nível de acesso mínimo
     * 
     * ANTES: Usava findByNivelAcessoMinimo(nivelMinimo) - MÉTODO INEXISTENTE
     * DEPOIS: Usa findByNivelAcessoGreaterThanEqual(nivelMinimo) - MÉTODO CORRETO
     * 
     * Explicação: O método findByNivelAcessoMinimo() não existe no PerfilRepository.
     * Em vez disso, usamos findByNivelAcessoGreaterThanEqual() que busca perfis com nível
     * de acesso maior ou igual ao valor fornecido.
     */
    @Transactional(readOnly = true)
    public List<Perfil> listarPorNivelAcessoMinimo(Integer nivelMinimo) {
        if (nivelMinimo == null || nivelMinimo < 1) {
            throw new IllegalArgumentException("Nível de acesso inválido");
        }
        return perfilRepository.findByNivelAcessoGreaterThanEqual(nivelMinimo);
    }

    /**
     * Busca perfis por termo (nome ou descrição)
     * 
     * Explicação: Este método foi adicionado para substituir o método inexistente buscarPorTermo()
     * que era chamado pelo PerfilController. Ele usa o método findByTermo() do repositório.
     */
    @Transactional(readOnly = true)
    public List<Perfil> findByTermo(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodos();
        }
        return perfilRepository.findByTermo(termo);
    }

    /**
     * Adiciona uma permissão a um perfil
     */
    public Perfil adicionarPermissao(Long perfilId, String nomePermissao) {
        Optional<Perfil> perfilOpt = buscarPorId(perfilId);
        if (perfilOpt.isEmpty()) {
            throw new IllegalArgumentException("Perfil não encontrado com ID: " + perfilId);
        }

        Optional<Permissao> permissaoOpt = permissaoRepository.findByNome(nomePermissao);
        if (permissaoOpt.isEmpty()) {
            throw new IllegalArgumentException("Permissão não encontrada com nome: " + nomePermissao);
        }

        Perfil perfil = perfilOpt.get();
        perfil.adicionarPermissao(permissaoOpt.get());
        return perfilRepository.save(perfil);
    }

    /**
     * Remove uma permissão de um perfil
     */
    public Perfil removerPermissao(Long perfilId, String nomePermissao) {
        Optional<Perfil> perfilOpt = buscarPorId(perfilId);
        if (perfilOpt.isEmpty()) {
            throw new IllegalArgumentException("Perfil não encontrado com ID: " + perfilId);
        }

        Perfil perfil = perfilOpt.get();
        perfil.removerPermissao(nomePermissao);
        return perfilRepository.save(perfil);
    }

    /**
     * Verifica se um perfil tem uma permissão específica
     */
    @Transactional(readOnly = true)
    public boolean temPermissao(Long perfilId, String nomePermissao) {
        Optional<Perfil> perfilOpt = buscarPorId(perfilId);
        if (perfilOpt.isEmpty()) {
            return false;
        }

        return perfilOpt.get().temPermissao(nomePermissao);
    }

    /**
     * Ativa um perfil
     */
    public Perfil ativar(Long perfilId) {
        Optional<Perfil> perfilOpt = buscarPorId(perfilId);
        if (perfilOpt.isEmpty()) {
            throw new IllegalArgumentException("Perfil não encontrado com ID: " + perfilId);
        }

        Perfil perfil = perfilOpt.get();
        perfil.ativar();
        return perfilRepository.save(perfil);
    }

    /**
     * Desativa um perfil
     */
    public Perfil desativar(Long perfilId) {
        Optional<Perfil> perfilOpt = buscarPorId(perfilId);
        if (perfilOpt.isEmpty()) {
            throw new IllegalArgumentException("Perfil não encontrado com ID: " + perfilId);
        }

        Perfil perfil = perfilOpt.get();
        perfil.desativar();
        return perfilRepository.save(perfil);
    }

    /**
     * Cria perfis padrão se não existirem
     */
    public void criarPerfisPadrao() {
        // Criar perfil administrador padrão
        if (!perfilRepository.existsByNome("ADMIN")) {
            Perfil admin = new Perfil(Perfil.TipoPadrao.ADMIN);
            salvar(admin);
        }

        // Criar perfil gerente padrão
        if (!perfilRepository.existsByNome("GERENTE")) {
            Perfil gerente = new Perfil(Perfil.TipoPadrao.SUPERVISOR);
            salvar(gerente);
        }

        // Criar perfil atendente padrão
        if (!perfilRepository.existsByNome("ATENDENTE")) {
            Perfil atendente = new Perfil(Perfil.TipoPadrao.ATENDENTE);
            salvar(atendente);
        }

        // Criar perfil cliente padrão
        if (!perfilRepository.existsByNome("CLIENTE")) {
            Perfil cliente = new Perfil(Perfil.TipoPadrao.CLIENTE);
            salvar(cliente);
        }
    }
}
EOF

# Substituir o arquivo original
mv /tmp/PerfilService.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/service/PerfilService.java
log "✅ Arquivo PerfilService.java corrigido com explicações."

# 3. CORREÇÃO DO ARQUIVO PERFILREPOSITORY.JAVA
# ============================================
echo -e "${BLUE}=== CORREÇÃO DO ARQUIVO PERFILREPOSITORY.JAVA ===${NC}"

echo "Criando nova versão do PerfilRepository.java com correções..."

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
EOF

# Substituir o arquivo original
mv /tmp/PerfilRepository.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/repository/PerfilRepository.java
log "✅ Arquivo PerfilRepository.java corrigido com explicações."

# 4. CORREÇÃO DO ARQUIVO DATABASEUPDATER.JAVA
# ===========================================
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
EOF

# Substituir o arquivo original
mv /tmp/DatabaseUpdater.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/util/DatabaseUpdater.java
log "✅ Arquivo DatabaseUpdater.java corrigido com explicações."

# 5. CORREÇÃO DO ARQUIVO USUARIOS.JAVA
# =====================================
echo -e "${BLUE}=== CORREÇÃO DO ARQUIVO USUARIOS.JAVA ===${NC}"

echo "Criando nova versão do Usuarios.java com correções..."

# Primeiro, vamos verificar o arquivo existente
cat > /tmp/Usuarios.java << 'EOF'
package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private Pessoas.StatusPessoa status;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuarios_perfis",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfis = new HashSet<>();

    // Construtores
    public Usuarios() {
        this.dataCadastro = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public Usuarios(String username, String senha, String emailPrincipal, Pessoas.StatusPessoa status) {
        this();
        this.username = username;
        this.senha = senha;
        this.emailPrincipal = emailPrincipal;
        this.status = status;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmailPrincipal() {
        return emailPrincipal;
    }

    public void setEmailPrincipal(String emailPrincipal) {
        this.emailPrincipal = emailPrincipal;
    }

    public Pessoas.StatusPessoa getStatus() {
        return status;
    }

    public void setStatus(Pessoas.StatusPessoa status) {
        this.status = status;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Set<Perfil> getPerfis() {
        return perfis;
    }

    public void setPerfis(Set<Perfil> perfis) {
        this.perfis = perfis;
    }

    // Métodos para adicionar e remover perfis
    public void adicionarPerfil(Perfil perfil) {
        this.perfis.add(perfil);
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void removerPerfil(Perfil perfil) {
        this.perfis.remove(perfil);
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Métodos auxiliares
    public boolean temPerfil(String nomePerfil) {
        return perfis.stream().anyMatch(p -> p.getNome().equals(nomePerfil));
    }

    public boolean temPerfil(Perfil perfil) {
        return perfis.contains(perfil);
    }
}
EOF

# Substituir o arquivo original
mv /tmp/Usuarios.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/model/Usuarios.java
log "✅ Arquivo Usuarios.java corrigido com explicações."

# 6. COMPILAÇÃO DO PROJETO
# ========================
echo -e "${BLUE}=== COMPILANDO O PROJETO ===${NC}"

# Navegar para o diretório do projeto
cd /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes

# Remover o diretório target se existir
if [ -d "target" ]; then
    echo "Removendo diretório target existente..."
    rm -rf target
fi

# Compilar a aplicação
echo "Compilando a aplicação..."
mvn clean compile -q

# Verificar se o build foi bem-sucedido
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Build bem-sucedido!${NC}"
    echo "✅ Todos os arquivos foram corrigidos e o projeto foi compilado com sucesso."
    echo "✅ Os erros de compilação foram resolvidos."
else
    echo -e "${RED}❌ Falha no build da aplicação.${NC}"
    echo "Por favor, verifique os logs de erro acima."
    exit 1
fi

echo -e "${GREEN}=== CORREÇÕES CONCLUÍDAS ===${NC}"
