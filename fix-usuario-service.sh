#!/bin/bash

# ==============================================
# Script para corrigir erros na classe UsuarioService
# Sistema de Gestão de Cartões
# ==============================================

echo "🔧 Corrigindo erros na classe UsuarioService - Gestão de Cartões"
echo "==============================================================="

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

# Corrigindo arquivo UsuarioService.java
echo "Corrigindo arquivo UsuarioService.java..."

cat > /tmp/UsuarioService.java << 'EOF'
package com.empresa.gestao_cartoes.service;

import com.empresa.gestao_cartoes.model.Usuarios;
import com.empresa.gestao_cartoes.model.Perfil;
import com.empresa.gestao_cartoes.model.Pessoas;
import com.empresa.gestao_cartoes.repository.UsuarioRepository;
import com.empresa.gestao_cartoes.repository.PerfilRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Serviço para gerenciar usuários do sistema
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Salva um novo usuário no banco de dados
     */
    public Usuarios salvar(Usuarios usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }

        // Validar se o username já existe
        if (usuario.getId() == null && usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("Já existe um usuário com o username: " + usuario.getUsername());
        }

        // Validar se o email já existe
        if (usuario.getId() == null && usuarioRepository.existsByEmail(usuario.getEmailPrincipal())) {
            throw new IllegalArgumentException("Já existe um usuário com o email: " + usuario.getEmailPrincipal());
        }

        // Criptografar senha se for um novo usuário
        if (usuario.getId() == null && usuario.getSenha() != null) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        // Resetar tentativas de login ao salvar
        usuario.setTentativasLogin(0);

        return usuarioRepository.save(usuario);
    }

    /**
     * Busca um usuário pelo ID
     */
    @Transactional(readOnly = true)
    public Optional<Usuarios> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return usuarioRepository.findById(id);
    }

    /**
     * Busca um usuário pelo username
     */
    @Transactional(readOnly = true)
    public Optional<Usuarios> buscarPorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username não pode ser nulo ou vazio");
        }
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Busca um usuário pelo email
     */
    @Transactional(readOnly = true)
    public Optional<Usuarios> buscarPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser nulo ou vazio");
        }
        return usuarioRepository.findByEmailPrincipal(email);
    }

    /**
     * Lista todos os usuários ativos
     */
    @Transactional(readOnly = true)
    public List<Usuarios> listarTodosAtivos() {
        return usuarioRepository.findByBloqueadoFalse();
    }

    /**
     * Lista usuários por status
     */
    @Transactional(readOnly = true)
    public List<Usuarios> listarPorStatus(Pessoas.StatusPessoa status) {
        return usuarioRepository.findByStatus(status);
    }

    /**
     * Lista usuários por perfil
     */
    @Transactional(readOnly = true)
    public List<Usuarios> listarPorPerfil(String nomePerfil) {
        if (nomePerfil == null || nomePerfil.trim().isEmpty()) {
            return listarTodosAtivos();
        }
        return usuarioRepository.findByPerfil(nomePerfil);
    }

    /**
     * Lista usuários por nível de acesso mínimo
     */
    @Transactional(readOnly = true)
    public List<Usuarios> listarPorNivelAcessoMinimo(Integer nivelMinimo) {
        if (nivelMinimo == null || nivelMinimo < 1) {
            throw new IllegalArgumentException("Nível de acesso inválido");
        }
        return usuarioRepository.findByNivelAcessoMinimo(nivelMinimo);
    }

    /**
     * Busca usuários por termo (username ou email)
     */
    @Transactional(readOnly = true)
    public List<Usuarios> buscarPorTermo(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodosAtivos();
        }
        return usuarioRepository.findByTermo(termo);
    }

    /**
     * Adiciona um perfil a um usuário
     */
    public Usuarios adicionarPerfil(Long usuarioId, String nomePerfil) {
        Optional<Usuarios> usuarioOpt = buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId);
        }

        Optional<Perfil> perfilOpt = perfilRepository.findByNome(nomePerfil);
        if (perfilOpt.isEmpty()) {
            throw new IllegalArgumentException("Perfil não encontrado com nome: " + nomePerfil);
        }

        Usuarios usuario = usuarioOpt.get();
        usuario.adicionarPerfil(perfilOpt.get());
        return usuarioRepository.save(usuario);
    }

    /**
     * Remove um perfil de um usuário
     */
    public Usuarios removerPerfil(Long usuarioId, String nomePerfil) {
        Optional<Usuarios> usuarioOpt = buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId);
        }

        Usuarios usuario = usuarioOpt.get();
        usuario.removerPerfil(nomePerfil);
        return usuarioRepository.save(usuario);
    }

    /**
     * Verifica se um usuário tem um perfil específico
     */
    @Transactional(readOnly = true)
    public boolean temPerfil(Long usuarioId, String nomePerfil) {
        Optional<Usuarios> usuarioOpt = buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        return usuarioOpt.get().temPerfil(nomePerfil);
    }

    /**
     * Registra login de um usuário
     */
    public void registrarLogin(Long usuarioId) {
        Optional<Usuarios> usuarioOpt = buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId);
        }

        Usuarios usuario = usuarioOpt.get();
        if (!usuario.podeRealizarOperacoes()) {
            throw new IllegalStateException("Usuário não pode fazer login devido ao status atual");
        }

        usuario.registrarLogin();
        usuarioRepository.save(usuario);
    }

    /**
     * Bloqueia um usuário por excesso de tentativas de login
     */
    public Usuarios bloquearPorTentativas(Long usuarioId) {
        Optional<Usuarios> usuarioOpt = buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId);
        }

        Usuarios usuario = usuarioOpt.get();
        usuario.setBloqueado(true);
        return usuarioRepository.save(usuario);
    }

    /**
     * Reseta tentativas de login de um usuário
     */
    public Usuarios resetarTentativasLogin(Long usuarioId) {
        Optional<Usuarios> usuarioOpt = buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId);
        }

        Usuarios usuario = usuarioOpt.get();
        usuario.setTentativasLogin(0);
        return usuarioRepository.save(usuario);
    }

    /**
     * Altera a senha de um usuário
     */
    public Usuarios alterarSenha(Long usuarioId, String senhaAtual, String novaSenha) {
        Optional<Usuarios> usuarioOpt = buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + usuarioId);
        }

        Usuarios usuario = usuarioOpt.get();

        // Verificar se a senha atual está correta
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        // Validar nova senha
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("Nova senha deve ter pelo menos 6 caracteres");
        }

        // Criptografar e atualizar a senha
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setTentativasLogin(0); // Resetar tentativas após alteração de senha

        return usuarioRepository.save(usuario);
    }

    /**
     * Cria usuários padrão se não existirem
     * 
     * CORREÇÃO: Adicionado o construtor correto para criar o usuário admin
     */
    public void criarUsuariosPadrao() {
        // Criar usuário administrador padrão
        if (!usuarioRepository.existsByUsername("admin")) {
            // CORREÇÃO: Usando o construtor correto com os parâmetros necessários
            Usuarios admin = new Usuarios("admin", "admin123", "admin@empresa.com", Pessoas.StatusPessoa.ATIVO);

            // Adicionar perfil admin
            Optional<Perfil> perfilAdmin = perfilRepository.findByNome("ADMIN");
            if (perfilAdmin.isPresent()) {
                admin.adicionarPerfil(perfilAdmin.get());
            }

            salvar(admin);
        }
    }
}
EOF

# Substituir o arquivo original
mv /tmp/UsuarioService.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/service/UsuarioService.java
log "✅ Arquivo UsuarioService.java corrigido."

# Corrigindo arquivo Usuarios.java
echo "Corrigindo arquivo Usuarios.java..."

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
public class Usuarios extends Pessoas {

    @Column(nullable = false)
    private String senha;

    @Column(name = "tentativas_login")
    private Integer tentativasLogin = 0;

    @Column(name = "bloqueado")
    private Boolean bloqueado = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuarios_perfis",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfis = new HashSet<>();

    // Construtores
    public Usuarios() {
        super(); // Chama o construtor da classe pai (Pessoas)
    }

    /**
     * Construtor para criar um usuário com os dados básicos
     * 
     * @param username Nome de usuário
     * @param senha Senha do usuário
     * @param emailPrincipal Email principal
     * @param status Status do usuário
     */
    public Usuarios(String username, String senha, String emailPrincipal, Pessoas.StatusPessoa status) {
        super(); // Chama o construtor da classe pai (Pessoas)
        this.username = username;
        this.senha = senha;
        this.emailPrincipal = emailPrincipal;
        this.status = status;
    }

    // Getters e Setters
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
        setDataAtualizacao(LocalDateTime.now());
    }

    public Integer getTentativasLogin() {
        return tentativasLogin;
    }

    public void setTentativasLogin(Integer tentativasLogin) {
        this.tentativasLogin = tentativasLogin;
        setDataAtualizacao(LocalDateTime.now());
    }

    public Boolean getBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado;
        setDataAtualizacao(LocalDateTime.now());
    }

    public Set<Perfil> getPerfis() {
        return perfis;
    }

    public void setPerfis(Set<Perfil> perfis) {
        this.perfis = perfis != null ? perfis : new HashSet<>();
        setDataAtualizacao(LocalDateTime.now());
    }

    // Métodos para adicionar e remover perfis
    public void adicionarPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil não pode ser nulo");
        }

        this.perfis.add(perfil);
        setDataAtualizacao(LocalDateTime.now());
    }

    public void removerPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil não pode ser nulo");
        }

        this.perfis.remove(perfil);
        setDataAtualizacao(LocalDateTime.now());
    }

    public void removerPerfil(String nomePerfil) {
        if (nomePerfil == null || nomePerfil.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do perfil não pode ser nulo ou vazio");
        }

        this.perfis.removeIf(p -> nomePerfil.equals(p.getNome()));
        setDataAtualizacao(LocalDateTime.now());
    }

    // Métodos auxiliares
    public boolean temPerfil(String nomePerfil) {
        if (nomePerfil == null || nomePerfil.trim().isEmpty()) {
            return false;
        }
        return perfis.stream().anyMatch(p -> nomePerfil.equals(p.getNome()));
    }

    public boolean temPerfil(Perfil perfil) {
        if (perfil == null) {
            return false;
        }
        return perfis.contains(perfil);
    }

    public boolean possuiPerfil(String nomePerfil) {
        return temPerfil(nomePerfil);
    }

    public boolean podeRealizarOperacoes() {
        // Usar o método da classe pai
        return super.podeRealizarOperacoes() && !Boolean.TRUE.equals(this.bloqueado);
    }

    public void registrarLogin() {
        setDataAtualizacao(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Usuarios{" +
                "id=" + getId() +
                ", username='" + username + ''' +
                ", email='" + getEmailPrincipal() + ''' +
                ", status=" + getStatus() +
                ", bloqueado=" + bloqueado +
                ", perfis=" + perfis.size() +
                '}';
    }
}
EOF

# Substituir o arquivo original
mv /tmp/Usuarios.java /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/src/main/java/com/empresa/gestao_cartoes/model/Usuarios.java
log "✅ Arquivo Usuarios.java corrigido."

echo "✅ Correções na classe UsuarioService concluídas!"
echo "✅ Correções na classe Usuarios concluídas!"
echo ""
echo "Próximos passos:"
echo "1. Execute 'mvn clean compile' para verificar se os erros foram resolvidos"
echo "2. Se houver outros erros, execute os outros scripts de correção"
EOF

# Substituir o arquivo original
mv /tmp/fix-usuario-service.sh /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/fix-usuario-service.sh

# Dar permissão de execução
chmod +x /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/fix-usuario-service.sh

log "✅ Script fix-usuario-service.sh criado com sucesso."
log "✅ Para executar, use: sudo ./fix-usuario-service.sh"
