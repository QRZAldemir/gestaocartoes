#!/bin/bash

# ==============================================
# Script aprimorado para corrigir erros de compilação
# Sistema de Gestão de Cartões
# ==============================================

echo "🔧 Corrigindo erros de compilação - Gestão de Cartões (Versão Aprimorada)"
echo "====================================================================="

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

# Corrigir arquivo PerfilController.java
echo "Corrigindo arquivo PerfilController.java..."
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

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /**
     * Lista todos os perfis ativos
     */
    @GetMapping
    public ResponseEntity<List<Perfil>> listarTodos() {
        List<Perfil> perfis = perfilService.listarTodosAtivos();
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

# Corrigir arquivo PerfilService.java
echo "Corrigindo arquivo PerfilService.java..."
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
     */
    @Transactional(readOnly = true)
    public List<Perfil> listarTodosAtivos() {
        return perfilRepository.findByAtivoTrue();
    }

    /**
     * Lista perfis por status (ativo/inativo)
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

# Corrigir arquivo DatabaseUpdater.java
echo "Corrigindo arquivo DatabaseUpdater.java..."
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

            // Adicionar perfil ao usuário usando o método correto
            admin.adicionarPerfil(perfilAdmin);

            entityManager.persist(admin);
        }
    }
}
EOF

# Substituir o arquivo original
mv /tmp/DatabaseUpdater.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/util/DatabaseUpdater.java

# Corrigir arquivo Usuarios.java para adicionar métodos necessários
echo "Corrigindo arquivo Usuarios.java..."
cat > /tmp/Usuarios.java << 'EOF'
package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@DiscriminatorValue("USUARIO")
public class Usuarios extends Pessoas {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Column(name = "data_ultimo_login")
    private LocalDateTime dataUltimoLogin;

    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado = false;

    @Column(name = "tentativas_login", nullable = false)
    private Integer tentativasLogin = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_perfis",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfis = new HashSet<>();

    // Construtores
    public Usuarios() {
        super();
    }

    public Usuarios(String username, String emailPrincipal, String senha) {
        super();
        this.username = username;
        this.senha = senha;
        this.setEmailPrincipal(emailPrincipal);
        this.setStatus(StatusPessoa.ATIVO);
    }

    // Getters e Setters
    public String getUsername() { return username; }
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username não pode ser nulo ou vazio");
        }
        this.username = username.trim();
    }

    public String getSenha() { return senha; }
    public void setSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        this.senha = senha;
    }

    public LocalDateTime getDataUltimoLogin() { return dataUltimoLogin; }
    public void setDataUltimoLogin(LocalDateTime dataUltimoLogin) { this.dataUltimoLogin = dataUltimoLogin; }

    public Boolean getBloqueado() { return bloqueado; }
    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado != null ? bloqueado : false;
    }

    public Integer getTentativasLogin() { return tentativasLogin; }
    public void setTentativasLogin(Integer tentativasLogin) {
        this.tentativasLogin = tentativasLogin != null ? tentativasLogin : 0;
    }

    public Set<Perfil> getPerfis() { return new HashSet<>(perfis); }

    public List<String> getNomesPerfis() {
        return perfis.stream()
            .map(Perfil::getNome)
            .toList();
    }

    // Implementação dos métodos abstratos
    @Override
    public String getNomeCompletoOuRazaoSocial() {
        return this.username;
    }

    @Override
    public String getDocumentoPrincipal() {
        return this.getEmailPrincipal();
    }

    @Override
    public String getTipoPessoa() {
        return "USUARIO";
    }

    // Métodos específicos
    public void adicionarPerfil(Perfil perfil) {
        if (perfil == null || perfil.getNome() == null) {
            throw new IllegalArgumentException("Perfil não pode ser nulo ou sem nome");
        }

        if (!podeRealizarOperacoes()) {
            throw new IllegalStateException("Usuário não pode receber novos perfis devido ao status atual");
        }

        if (!this.perfis.contains(perfil)) {
            this.perfis.add(perfil);
        }
    }

    public boolean removerPerfil(Perfil perfil) {
        if (perfil == null) return false;
        return this.perfis.remove(perfil);
    }

    public boolean removerPerfil(String perfil) {
        if (perfil == null) return false;
        return this.perfis.removeIf(p -> p.getNome().equals(perfil.trim().toUpperCase()));
    }

    public boolean possuiPerfil(String perfil) {
        if (perfil == null) return false;
        return this.perfis.stream().anyMatch(p -> p.getNome().equals(perfil.trim().toUpperCase()));
    }

    public void registrarLogin() {
        if (!podeRealizarOperacoes()) {
            throw new IllegalStateException("Usuário não pode fazer login devido ao status atual");
        }
        this.dataUltimoLogin = LocalDateTime.now();
    }

    public boolean podeRealizarLogin() {
        return podeRealizarOperacoes() &&
               !bloqueado &&
               username != null && !username.trim().isEmpty() &&
               senha != null && !senha.isEmpty() &&
               getEmailPrincipal() != null && !getEmailPrincipal().trim().isEmpty();
    }

    public boolean isAdministrador() {
        return possuiPerfil("ADMIN") || possuiPerfil("ADMINISTRADOR");
    }

    @Override
    public String toString() {
        return "Usuario[id=%d, username=%s, email=%s, status=%s]".formatted(
                getId(), username, getEmailPrincipal(), getStatus());
    }
}
EOF

# Substituir o arquivo original
mv /tmp/Usuarios.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/model/Usuarios.java

echo "✅ Correções aplicadas com sucesso!"
echo "Os seguintes arquivos foram corrigidos:"
echo "1. PerfilController.java"
echo "2. PerfilService.java"
echo "3. DatabaseUpdater.java"
echo "4. Usuarios.java"
echo ""
echo "Agora você pode tentar compilar a aplicação novamente com:"
echo "mvn clean compile"
