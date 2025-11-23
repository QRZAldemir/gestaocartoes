 package com.empresa.gestao_cartoes.service;

import com.empresa.gestao_cartoes.model.Cartoes;
import com.empresa.gestao_cartoes.repository.CartaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartaoService {

    private final CartaoRepository cartaoRepository;

    public CartaoService(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    /**
     * Lista todos os cartões
     */
    @Transactional(readOnly = true)
    public List<Cartoes> listarCartoes() {
        return cartaoRepository.findAll();
    }

    /**
     * Salva um novo cartão com validações
     */
    @Transactional
    public Cartoes salvarCartao(Cartoes cartao) {
        // Validações adicionais podem ser adicionadas aqui
        return cartaoRepository.save(cartao);
    }

    /**
     * Busca cartões por status
     */
    @Transactional(readOnly = true)
    public List<Cartoes> buscarPorStatus(Cartoes.StatusCartao status) {
        return cartaoRepository.findByStatus(status);
    }

    /**
     * Busca cartões por bandeira
     */
    @Transactional(readOnly = true)
    public List<Cartoes> buscarPorBandeira(Cartoes.BandeiraCartao bandeira) {
        return cartaoRepository.findByBandeira(bandeira);
    }
}