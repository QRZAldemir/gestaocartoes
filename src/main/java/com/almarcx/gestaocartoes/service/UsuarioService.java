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
     */
    public void criarUsuariosPadrao() {
        // Criar usuário administrador padrão
        if (!usuarioRepository.existsByUsername("admin")) {
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
