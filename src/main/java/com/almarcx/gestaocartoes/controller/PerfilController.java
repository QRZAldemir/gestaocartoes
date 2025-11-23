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
