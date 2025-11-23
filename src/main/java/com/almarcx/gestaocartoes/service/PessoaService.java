package com.empresa.gestao_cartoes.service;

import com.empresa.gestao_cartoes.model.Pessoas;
import com.empresa.gestao_cartoes.repository.PessoaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;

    public PessoaService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    public Pessoas salvar(Pessoas pessoa) {
        return pessoaRepository.save(pessoa);
    }

    public Optional<Pessoas> buscarPorId(Long id) {
        return pessoaRepository.findById(id);
    }

    public List<Pessoas> listarTodos() {
        return pessoaRepository.findAll();
    }

    public List<Pessoas> buscarPorStatus(Pessoas.StatusPessoa status) {
        return pessoaRepository.findByStatus(status);
    }

    public void deletar(Long id) {
        pessoaRepository.deleteById(id);
    }

    public boolean existePorEmail(String email) {
        return pessoaRepository.existsByEmailPrincipal(email);
    }
}