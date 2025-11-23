package com.empresa.gestao_cartoes.controller;

import com.empresa.gestao_cartoes.model.Usuarios;
import com.empresa.gestao_cartoes.model.Pessoas;
import com.empresa.gestao_cartoes.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciar usuários do sistema
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Lista todos os usuários ativos
     */
    @GetMapping
    public ResponseEntity<List<Usuarios>> listarTodos() {
        List<Usuarios> usuarios = usuarioService.listarTodosAtivos();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Busca um usuário pelo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuarios> buscarPorId(@PathVariable Long id) {
        Optional<Usuarios> usuario = usuarioService.buscarPorId(id);
        return usuario.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca um usuário pelo username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Usuarios> buscarPorUsername(@PathVariable String username) {
        Optional<Usuarios> usuario = usuarioService.buscarPorUsername(username);
        return usuario.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca um usuário pelo email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Usuarios> buscarPorEmail(@PathVariable String email) {
        Optional<Usuarios> usuario = usuarioService.buscarPorEmail(email);
        return usuario.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista usuários por status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Usuarios>> listarPorStatus(@PathVariable Pessoas.StatusPessoa status) {
        List<Usuarios> usuarios = usuarioService.listarPorStatus(status);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Lista usuários por perfil
     */
    @GetMapping("/perfil/{perfil}")
    public ResponseEntity<List<Usuarios>> listarPorPerfil(@PathVariable String perfil) {
        List<Usuarios> usuarios = usuarioService.listarPorPerfil(perfil);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Lista usuários por nível de acesso mínimo
     */
    @GetMapping("/nivel/{nivelMinimo}")
    public ResponseEntity<List<Usuarios>> listarPorNivelAcessoMinimo(@PathVariable Integer nivelMinimo) {
        List<Usuarios> usuarios = usuarioService.listarPorNivelAcessoMinimo(nivelMinimo);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Busca usuários por termo (username ou email)
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Usuarios>> buscarPorTermo(@RequestParam String termo) {
        List<Usuarios> usuarios = usuarioService.buscarPorTermo(termo);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Cria um novo usuário
     */
    @PostMapping
    public ResponseEntity<Usuarios> criar(@RequestBody Usuarios usuario) {
        try {
            Usuarios novoUsuario = usuarioService.salvar(usuario);
            return ResponseEntity.ok(novoUsuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Atualiza um usuário existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuarios> atualizar(@PathVariable Long id, @RequestBody Usuarios usuario) {
        try {
            usuario.setId(id);
            Usuarios usuarioAtualizado = usuarioService.salvar(usuario);
            return ResponseEntity.ok(usuarioAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Adiciona um perfil a um usuário
     */
    @PostMapping("/{id}/perfis")
    public ResponseEntity<Usuarios> adicionarPerfil(@PathVariable Long id, @RequestParam String perfil) {
        try {
            Usuarios usuario = usuarioService.adicionarPerfil(id, perfil);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove um perfil de um usuário
     */
    @DeleteMapping("/{id}/perfis")
    public ResponseEntity<Usuarios> removerPerfil(@PathVariable Long id, @RequestParam String perfil) {
        try {
            Usuarios usuario = usuarioService.removerPerfil(id, perfil);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verifica se um usuário tem um perfil específico
     */
    @GetMapping("/{id}/perfis/{perfil}")
    public ResponseEntity<Boolean> temPerfil(@PathVariable Long id, @PathVariable String perfil) {
        boolean temPerfil = usuarioService.temPerfil(id, perfil);
        return ResponseEntity.ok(temPerfil);
    }

    /**
     * Registra login de um usuário
     */
    @PostMapping("/{id}/login")
    public ResponseEntity<Void> registrarLogin(@PathVariable Long id) {
        try {
            usuarioService.registrarLogin(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * Bloqueia um usuário por excesso de tentativas
     */
    @PostMapping("/{id}/bloquear")
    public ResponseEntity<Usuarios> bloquear(@PathVariable Long id) {
        try {
            Usuarios usuario = usuarioService.bloquearPorTentativas(id);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Reseta tentativas de login de um usuário
     */
    @PostMapping("/{id}/resetar-tentativas")
    public ResponseEntity<Usuarios> resetarTentativas(@PathVariable Long id) {
        try {
            Usuarios usuario = usuarioService.resetarTentativasLogin(id);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Altera a senha de um usuário
     */
    @PutMapping("/{id}/senha")
    public ResponseEntity<Usuarios> alterarSenha(@PathVariable Long id, 
                                                @RequestParam String senhaAtual, 
                                                @RequestParam String novaSenha) {
        try {
            Usuarios usuario = usuarioService.alterarSenha(id, senhaAtual, novaSenha);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cria usuários padrão
     */
    @PostMapping("/padrao")
    public ResponseEntity<String> criarUsuariosPadrao() {
        usuarioService.criarUsuariosPadrao();
        return ResponseEntity.ok("Usuários padrão criados com sucesso");
    }
}