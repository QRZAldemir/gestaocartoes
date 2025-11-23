  package com.empresa.gestao_cartoes.controller;

import com.empresa.gestao_cartoes.model.Cartoes;
import com.empresa.gestao_cartoes.service.CartaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {

    private final CartaoService cartaoService;

    public CartaoController(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @GetMapping
    public ResponseEntity<List<Cartoes>> listarCartoes() {
        return ResponseEntity.ok(cartaoService.listarCartoes());
    }

    @PostMapping
    public ResponseEntity<Cartoes> adicionarCartao(@RequestBody Cartoes cartao) {
        return ResponseEntity.ok(cartaoService.salvarCartao(cartao));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Cartoes>> buscarPorStatus(
            @PathVariable Cartoes.StatusCartao status) {
        return ResponseEntity.ok(cartaoService.buscarPorStatus(status));
    }

    @GetMapping("/bandeira/{bandeira}")
    public ResponseEntity<List<Cartoes>> buscarPorBandeira(
            @PathVariable Cartoes.BandeiraCartao bandeira) {
        return ResponseEntity.ok(cartaoService.buscarPorBandeira(bandeira));
    }
}