package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "perfis")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Nome do perfil é obrigatório")
    private String nome;

    @Column(length = 255)
    private String descricao;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "perfil_permissoes",
        joinColumns = @JoinColumn(name = "perfil_id"),
        inverseJoinColumns = @JoinColumn(name = "permissao_id")
    )
    private Set<Permissao> permissoes = new HashSet<>();

    @Column(name = "nivel_acesso", nullable = false)
    private Integer nivelAcesso = 1;
    
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
    
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @ManyToMany(mappedBy = "perfis")
    private Set<Usuarios> usuarios = new HashSet<>();

    public enum TipoPadrao {
        ADMIN("Administrador"),
        ANALISTA_RISCO("Analista de Risco"),
        ATENDENTE("Atendente"),
        SUPERVISOR("Supervisor"),
        CLIENTE("Cliente"),
        AUDITOR("Auditor");

        private final String descricao;

        TipoPadrao(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public Perfil() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public Perfil(TipoPadrao tipoPadrao) {
        this();
        this.nome = tipoPadrao.name();
        this.descricao = tipoPadrao.getDescricao();
        this.permissoes = obterPermissoesPadrao(tipoPadrao);
        this.nivelAcesso = definirNivelAcesso(tipoPadrao);
    }
    
    public Perfil(String nome, String descricao, Integer nivelAcesso) {
        this();
        this.nome = nome;
        this.descricao = descricao;
        this.nivelAcesso = nivelAcesso;
    }

    private Set<Permissao> obterPermissoesPadrao(TipoPadrao tipo) {
        Set<Permissao> permissoes = new HashSet<>();
        
        switch (tipo) {
            case ADMIN:
                permissoes.add(new Permissao("GERENCIAR_USUARIOS", "Gerenciar usuários do sistema", "SISTEMA"));
                permissoes.add(new Permissao("ACESSO_TOTAL", "Acesso total ao sistema", "SISTEMA"));
                permissoes.add(new Permissao("APROVAR_LIMITES", "Aprovar limites de cartão", "CARTOES"));
                break;
            case ANALISTA_RISCO:
                permissoes.add(new Permissao("ANALISAR_RISCOS", "Analisar riscos de crédito", "CLIENTES"));
                permissoes.add(new Permissao("VISUALIZAR_DADOS_SENSIVEIS", "Visualizar dados sensíveis", "CLIENTES"));
                break;
            case ATENDENTE:
                permissoes.add(new Permissao("GERENCIAR_CLIENTES", "Gerenciar clientes", "CLIENTES"));
                permissoes.add(new Permissao("BLOQUEAR_CARTOES", "Bloquear cartões", "CARTOES"));
                break;
            case SUPERVISOR:
                permissoes.add(new Permissao("APROVAR_SOLICITACOES", "Aprovar solicitações", "OPERACOES"));
                permissoes.add(new Permissao("GERENCIAR_EQUIPE", "Gerenciar equipe", "OPERACOES"));
                break;
            case AUDITOR:
                permissoes.add(new Permissao("AUDITAR_OPERACOES", "Auditar operações", "AUDITORIA"));
                permissoes.add(new Permissao("GERAR_RELATORIOS", "Gerar relatórios", "RELATORIOS"));
                break;
            case CLIENTE:
                permissoes.add(new Permissao("ACESSO_CONTA", "Acesso à conta", "CLIENTE"));
                permissoes.add(new Permissao("SOLICITAR_CARTAO", "Solicitar novo cartão", "CLIENTE"));
                break;
        }
        
        return permissoes;
    }

    private int definirNivelAcesso(TipoPadrao tipo) {
        return switch (tipo) {
            case ADMIN -> 10;
            case AUDITOR, ANALISTA_RISCO -> 8;
            case SUPERVISOR -> 7;
            case ATENDENTE -> 5;
            case CLIENTE -> 1;
        };
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Set<Permissao> getPermissoes() { return permissoes; }
    public void setPermissoes(Set<Permissao> permissoes) { this.permissoes = permissoes; }

    public Integer getNivelAcesso() { return nivelAcesso; }
    public void setNivelAcesso(Integer nivelAcesso) { this.nivelAcesso = nivelAcesso; }
    
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    
    public Set<Usuarios> getUsuarios() { return usuarios; }
    public void setUsuarios(Set<Usuarios> usuarios) { this.usuarios = usuarios; }

    // Métodos de negócio
    public boolean temPermissao(String permissao) {
        return permissoes.stream()
            .anyMatch(p -> p.getNome().equals(permissao));
    }
    
    public boolean temPermissao(Permissao permissao) {
        return permissoes.contains(permissao);
    }

    public void adicionarPermissao(Permissao permissao) {
        if (permissao != null) {
            this.permissoes.add(permissao);
        }
    }
    
    public void adicionarPermissao(String nomePermissao) {
        // Este método mantém compatibilidade com código antigo
        // mas na prática, a permissão deve ser buscada no repositório primeiro
        Permissao permissao = new Permissao();
        permissao.setNome(nomePermissao);
        this.permissoes.add(permissao);
    }

    public void removerPermissao(Permissao permissao) {
        if (permissao != null) {
            this.permissoes.remove(permissao);
        }
    }
    
    public void removerPermissao(String nomePermissao) {
        this.permissoes.removeIf(p -> p.getNome().equals(nomePermissao));
    }
    
    public void ativar() {
        this.ativo = true;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public void desativar() {
        this.ativo = false;
        this.dataAtualizacao = LocalDateTime.now();
    }
}