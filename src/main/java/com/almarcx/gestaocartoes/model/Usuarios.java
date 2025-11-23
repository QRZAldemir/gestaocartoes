package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa um usuário do sistema
 */
@Entity
@Table(name = "usuarios")
public class Usuarios extends Pessoas {

    @Column(nullable = false)
    private String senha;

    @Column(name = "tentativas_login")
    private Integer tentativasLogin = 0;

    @Column(name = "bloqueado")
    private Boolean bloqueado = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuarios_perfis",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfis = new HashSet<>();

    // Construtores
    public Usuarios() {
        super(); // Chama o construtor da classe pai (Pessoas)
    }

    /**
     * Construtor para criar um usuário com os dados básicos
     *
     * @param username Nome de usuário
     * @param senha Senha do usuário
     * @param emailPrincipal Email principal
     * @param status Status do usuário
     */
    public Usuarios(String username, String senha, String emailPrincipal, Pessoas.StatusPessoa status) {
        super(); // Chama o construtor da classe pai (Pessoas)
        this.username = username;
        this.senha = senha;
        this.emailPrincipal = emailPrincipal;
        this.status = status;
    }

    // Getters e Setters
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
        setDataAtualizacao(LocalDateTime.now());
    }

    public Integer getTentativasLogin() {
        return tentativasLogin;
    }

    public void setTentativasLogin(Integer tentativasLogin) {
        this.tentativasLogin = tentativasLogin;
        setDataAtualizacao(LocalDateTime.now());
    }

    public Boolean getBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado;
        setDataAtualizacao(LocalDateTime.now());
    }

    public Set<Perfil> getPerfis() {
        return perfis;
    }

    public void setPerfis(Set<Perfil> perfis) {
        this.perfis = perfis != null ? perfis : new HashSet<>();
        setDataAtualizacao(LocalDateTime.now());
    }

    // Métodos para adicionar e remover perfis
    public void adicionarPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil não pode ser nulo");
        }

        this.perfis.add(perfil);
        setDataAtualizacao(LocalDateTime.now());
    }

    public void removerPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil não pode ser nulo");
        }

        this.perfis.remove(perfil);
        setDataAtualizacao(LocalDateTime.now());
    }

    public void removerPerfil(String nomePerfil) {
        if (nomePerfil == null || nomePerfil.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do perfil não pode ser nulo ou vazio");
        }

        this.perfis.removeIf(p -> nomePerfil.equals(p.getNome()));
        setDataAtualizacao(LocalDateTime.now());
    }

    // Métodos auxiliares
    public boolean temPerfil(String nomePerfil) {
        if (nomePerfil == null || nomePerfil.trim().isEmpty()) {
            return false;
        }
        return perfis.stream().anyMatch(p -> nomePerfil.equals(p.getNome()));
    }

    public boolean temPerfil(Perfil perfil) {
        if (perfil == null) {
            return false;
        }
        return perfis.contains(perfil);
    }

    public boolean possuiPerfil(String nomePerfil) {
        return temPerfil(nomePerfil);
    }

    public boolean podeRealizarOperacoes() {
        // Usar o método da classe pai
        return super.podeRealizarOperacoes() && !Boolean.TRUE.equals(this.bloqueado);
    }

    public void registrarLogin() {
        setDataAtualizacao(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Usuarios{" +
                "id=" + getId() +
                ", username='" + username + ''' +
                ", email='" + getEmailPrincipal() + ''' +
                ", status=" + getStatus() +
                ", bloqueado=" + bloqueado +
                ", perfis=" + perfis.size() +
                '}';
    }
}
