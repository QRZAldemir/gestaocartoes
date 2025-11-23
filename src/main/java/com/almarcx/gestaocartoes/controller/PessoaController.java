package com.empresa.gestao_cartoes.controller;

import com.empresa.gestao_cartoes.model.Pessoas;
import com.empresa.gestao_cartoes.service.PessoaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pessoas")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @PostMapping
    public ResponseEntity<Pessoas> criar(@RequestBody Pessoas pessoa) {
        return ResponseEntity.ok(pessoaService.salvar(pessoa));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pessoas> buscarPorId(@PathVariable Long id) {
        Optional<Pessoas> pessoa = pessoaService.buscarPorId(id);
        return pessoa.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Pessoas> listarTodos() {
        return pessoaService.listarTodos();
    }

    @GetMapping("/status/{status}")
    public List<Pessoas> buscarPorStatus(@PathVariable Pessoas.StatusPessoa status) {
        return pessoaService.buscarPorStatus(status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pessoaService.deletar(id);
        return ResponseEntity.ok().build();
    }
}