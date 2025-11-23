package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bancos")
public class Banco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Código do banco é obrigatório")
    @Pattern(regexp = "\\d{3}", message = "Código do banco deve ter 3 dígitos")
    @Column(name = "codigo_banco", nullable = false, unique = true, length = 3)
    private String codigoBanco;

    @NotBlank(message = "Nome do banco é obrigatório")
    @Size(max = 100, message = "Nome do banco não pode exceder 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Size(max = 20, message = "ISPB deve ter no máximo 20 caracteres")
    @Column(name = "ispb", length = 20)
    private String ispb;

    @Size(max = 200, message = "Site deve ter no máximo 200 caracteres")
    @Column(name = "site", length = 200)
    private String site;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    // Relacionamentos
    @OneToMany(mappedBy = "banco", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContaInterna> contas = new ArrayList<>();

    // Construtores
    public Banco() {
    }

    public Banco(String codigoBanco, String nome, String ispb) {
        this.codigoBanco = codigoBanco;
        this.nome = nome;
        this.ispb = ispb;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoBanco() {
        return codigoBanco;
    }

    public void setCodigoBanco(String codigoBanco) {
        this.codigoBanco = codigoBanco;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIspb() {
        return ispb;
    }

    public void setIspb(String ispb) {
        this.ispb = ispb;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public List<ContaInterna> getContas() {
        return contas;
    }

    public void setContas(List<ContaInterna> contas) {
        this.contas = contas;
    }

    // Métodos de negócio
    public String getNomeComCodigo() {
        return codigoBanco + " - " + nome;
    }

    public boolean isAtivo() {
        return Boolean.TRUE.equals(ativo);
    }

    public void ativar() {
        this.ativo = true;
    }

    public void inativar() {
        this.ativo = false;
    }

    @Override
    public String toString() {
        return "Banco{" +
                "id=" + id +
                ", codigoBanco='" + codigoBanco + '\'' +
                ", nome='" + nome + '\'' +
                ", ativo=" + ativo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Banco banco = (Banco) o;
        return codigoBanco != null && codigoBanco.equals(banco.codigoBanco);
    }

    @Override
    public int hashCode() {
        return codigoBanco != null ? codigoBanco.hashCode() : 0;
    }
}