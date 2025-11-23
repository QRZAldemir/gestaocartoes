package com.empresa.gestao_cartoes.controller;

import com.empresa.gestao_cartoes.model.Permissao;
import com.empresa.gestao_cartoes.repository.PermissaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciar permissões do sistema
 */
@RestController
@RequestMapping("/api/permissoes")
public class PermissaoController {

    private final PermissaoRepository permissaoRepository;

    public PermissaoController(PermissaoRepository permissaoRepository) {
        this.permissaoRepository = permissaoRepository;
    }

    /**
     * Lista todas as permissões
     */
    @GetMapping
    public ResponseEntity<List<Permissao>> listarTodos() {
        List<Permissao> permissoes = permissaoRepository.findAll();
        return ResponseEntity.ok(permissoes);
    }

    /**
     * Lista permissões ativas
     */
    @GetMapping("/ativas")
    public ResponseEntity<List<Permissao>> listarAtivas() {
        List<Permissao> permissoes = permissaoRepository.findByAtivoTrue();
        return ResponseEntity.ok(permissoes);
    }

    /**
     * Busca uma permissão pelo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Permissao> buscarPorId(@PathVariable Long id) {
        Optional<Permissao> permissao = permissaoRepository.findById(id);
        return permissao.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca uma permissão pelo nome
     */
    @GetMapping("/nome/{nome}")
    public ResponseEntity<Permissao> buscarPorNome(@PathVariable String nome) {
        Optional<Permissao> permissao = permissaoRepository.findByNome(nome);
        return permissao.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista permissões por módulo
     */
    @GetMapping("/modulo/{modulo}")
    public ResponseEntity<List<Permissao>> listarPorModulo(@PathVariable String modulo) {
        List<Permissao> permissoes = permissaoRepository.findByModulo(modulo);
        return ResponseEntity.ok(permissoes);
    }

    /**
     * Busca permissões por termo (nome ou descrição)
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Permissao>> buscarPorTermo(@RequestParam String termo) {
        List<Permissao> permissoes = permissaoRepository.findByTermo(termo);
        return ResponseEntity.ok(permissoes);
    }

    /**
     * Lista módulos disponíveis
     */
    @GetMapping("/modulos")
    public ResponseEntity<List<String>> listarModulos() {
        List<String> modulos = permissaoRepository.findModulosDistinct();
        return ResponseEntity.ok(modulos);
    }

    /**
     * Cria uma nova permissão
     */
    @PostMapping
    public ResponseEntity<Permissao> criar(@RequestBody Permissao permissao) {
        if (permissaoRepository.existsByNome(permissao.getNome())) {
            return ResponseEntity.badRequest().build();
        }

        Permissao novaPermissao = permissaoRepository.save(permissao);
        return ResponseEntity.ok(novaPermissao);
    }

    /**
     * Atualiza uma permissão existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Permissao> atualizar(@PathVariable Long id, @RequestBody Permissao permissao) {
        if (!permissaoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        permissao.setId(id);
        Permissao permissaoAtualizada = permissaoRepository.save(permissao);
        return ResponseEntity.ok(permissaoAtualizada);
    }

    /**
     * Exclui uma permissão
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!permissaoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        permissaoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cria permissões padrão
     */
    @PostMapping("/padrao")
    public ResponseEntity<String> criarPermissoesPadrao() {
        // Esta implementação será chamada pelo DatabaseUpdater
        return ResponseEntity.ok("Permissões padrão serão criadas durante a inicialização do sistema");
    }
}
