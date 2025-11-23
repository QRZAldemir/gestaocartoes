package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pessoas")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_pessoa", discriminatorType = DiscriminatorType.STRING)
public abstract class Pessoas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatusPessoa status = StatusPessoa.ATIVO;

    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    // Campos de Endereço
    @Column(name = "cep", length = 8)
    private String cep;

    @Column(name = "logradouro", length = 150)
    private String logradouro;

    @Column(name = "numero", length = 10)
    private String numero;

    @Column(name = "complemento", length = 50)
    private String complemento;

    @Column(name = "bairro", length = 50)
    private String bairro;

    @Column(name = "cidade", length = 50)
    private String cidade;

    @Column(name = "uf", length = 2)
    private String uf;

    // Campos de Contato
    @Column(name = "email_principal", length = 100)
    private String emailPrincipal;

    @Column(name = "telefone_principal", length = 20)
    private String telefonePrincipal;

    @Column(name = "celular_principal", length = 20)
    private String celularPrincipal;

    @ElementCollection
    @CollectionTable(name = "pessoa_contatos_adicionais", joinColumns = @JoinColumn(name = "pessoa_id"))
    @Column(name = "contato")
    private List<String> contatosAdicionais = new ArrayList<>();

    public enum StatusPessoa {
        ATIVO("Ativo", true),
        INATIVO("Inativo", false),
        SUSPENSO("Suspenso", false),
        CANCELADO("Cancelado", false);

        private final String descricao;
        private final boolean operacoesPermitidas;

        StatusPessoa(String descricao, boolean operacoesPermitidas) {
            this.descricao = descricao;
            this.operacoesPermitidas = operacoesPermitidas;
        }

        public String getDescricao() { return descricao; }
        public boolean isOperacoesPermitidas() { return operacoesPermitidas; }
    }

    public Pessoas() {
        this.dataCadastro = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatusPessoa getStatus() { return status; }
    public void setStatus(StatusPessoa status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getEmailPrincipal() { return emailPrincipal; }
    public void setEmailPrincipal(String emailPrincipal) { this.emailPrincipal = emailPrincipal; }

    public String getTelefonePrincipal() { return telefonePrincipal; }
    public void setTelefonePrincipal(String telefonePrincipal) { this.telefonePrincipal = telefonePrincipal; }

    public String getCelularPrincipal() { return celularPrincipal; }
    public void setCelularPrincipal(String celularPrincipal) { this.celularPrincipal = celularPrincipal; }

    public List<String> getContatosAdicionais() { return contatosAdicionais; }
    public void setContatosAdicionais(List<String> contatosAdicionais) { this.contatosAdicionais = contatosAdicionais; }

    // Métodos Abstratos
    public abstract String getNomeCompletoOuRazaoSocial();
    public abstract String getDocumentoPrincipal();
    public abstract String getTipoPessoa();

    // Métodos de Negócio
    public boolean podeRealizarOperacoes() {
        return this.status != null && this.status.isOperacoesPermitidas();
    }

    public boolean isAtiva() {
        return StatusPessoa.ATIVO.equals(this.status);
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}