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

    // Construtor para injeção de dependência
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
     * 
     * Explicação: Este método foi adicionado para substituir o método inexistente listarTodosAtivos()
     * que era chamado pelo PerfilController. Ele usa o método findByAtivoTrue() do repositório.
     */
    @Transactional(readOnly = true)
    public List<Perfil> listarTodosAtivos() {
        return perfilRepository.findByAtivoTrue();
    }

    /**
     * Lista perfis por status (ativo/inativo)
     * 
     * Explicação: Este método substitui o método original que tentava usar findByAtivo(Boolean)
     * que não existe no repositório. Agora ele usa findByAtivoTrue() para perfis ativos.
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
     * 
     * ANTES: Usava findByNivelAcessoMinimo(nivelMinimo) - MÉTODO INEXISTENTE
     * DEPOIS: Usa findByNivelAcessoGreaterThanEqual(nivelMinimo) - MÉTODO CORRETO
     * 
     * Explicação: O método findByNivelAcessoMinimo() não existe no PerfilRepository.
     * Em vez disso, usamos findByNivelAcessoGreaterThanEqual() que busca perfis com nível
     * de acesso maior ou igual ao valor fornecido.
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
     * 
     * Explicação: Este método foi adicionado para substituir o método inexistente buscarPorTermo()
     * que era chamado pelo PerfilController. Ele usa o método findByTermo() do repositório.
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
