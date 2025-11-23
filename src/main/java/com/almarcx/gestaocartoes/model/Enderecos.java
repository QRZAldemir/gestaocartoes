package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;

import jakarta.validation.constraints.Size;

@Entity
@Table(name = "enderecos")
public class Enderecos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoas pessoa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoEndereco tipo = TipoEndereco.RESIDENCIAL;

    @Size(max = 8, message = "CEP deve ter no máximo 8 caracteres")
    @Column(name = "cep", length = 8)
    private String cep;

    @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres")
    @Column(name = "logradouro", length = 150)
    private String logradouro;

    @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
    @Column(name = "numero", length = 10)
    private String numero;

    @Size(max = 50, message = "Complemento deve ter no máximo 50 caracteres")
    @Column(name = "complemento", length = 50)
    private String complemento;

    @Size(max = 50, message = "Bairro deve ter no máximo 50 caracteres")
    @Column(name = "bairro", length = 50)
    private String bairro;

    @Size(max = 50, message = "Cidade deve ter no máximo 50 caracteres")
    @Column(name = "cidade", length = 50)
    private String cidade;

    @Size(max = 2, message = "UF deve ter no máximo 2 caracteres")
    @Column(name = "uf", length = 2)
    private String uf;

    @Size(max = 50, message = "País deve ter no máximo 50 caracteres")
    @Column(name = "pais", length = 50)
    private String pais = "Brasil";

    @Column(name = "principal")
    private Boolean principal = false;

    public enum TipoEndereco {
        RESIDENCIAL("Residencial"),
        COMERCIAL("Comercial"),
        COBRANCA("Cobrança"),
        ENTREGA("Entrega"),
        OUTRO("Outro");

        private final String descricao;

        TipoEndereco(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Construtores
    public Enderecos() {
    }

    public Enderecos(Pessoas pessoa, TipoEndereco tipo, String cep, String logradouro, 
                   String numero, String complemento, String bairro, String cidade, 
                   String uf, Boolean principal) {
        this.pessoa = pessoa;
        this.tipo = tipo;
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.principal = principal;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pessoas getPessoa() { return pessoa; }
    public void setPessoa(Pessoas pessoa) { this.pessoa = pessoa; }

    public TipoEndereco getTipo() { return tipo; }
    public void setTipo(TipoEndereco tipo) { this.tipo = tipo; }

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

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public Boolean getPrincipal() { return principal; }
    public void setPrincipal(Boolean principal) { this.principal = principal; }
    public boolean isPrincipal() { return Boolean.TRUE.equals(principal); }

    // Métodos auxiliares
    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        if (logradouro != null) sb.append(logradouro);
        if (numero != null) sb.append(", ").append(numero);
        if (complemento != null) sb.append(" - ").append(complemento);
        if (bairro != null) sb.append(" - ").append(bairro);
        if (cidade != null) sb.append(" - ").append(cidade);
        if (uf != null) sb.append("/").append(uf);
        if (cep != null) sb.append(" - CEP: ").append(cep);
        return sb.toString();
    }

    public String getCepFormatado() {
        if (cep == null || cep.length() != 8) return cep;
        return cep.substring(0, 5) + "-" + cep.substring(5);
    }

    @Override
    public String toString() {
        return "Endereco{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", cep='" + cep + '\'' +
                ", logradouro='" + logradouro + '\'' +
                ", numero='" + numero + '\'' +
                ", cidade='" + cidade + '\'' +
                ", uf='" + uf + '\'' +
                ", principal=" + principal +
                '}';
    }
}