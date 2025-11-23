package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CNPJ;

import java.time.LocalDate;
import java.time.Period;

/**
 * Entidade que representa um Cliente do tipo Pessoa Jurídica.
 * Herda todos os atributos de Clientes (e consequentemente de Pessoas).
 *
 * CORREÇÕES:
 * - Implementado método getTipoPessoa() corretamente
 * - Corrigida a anotação @DiscriminatorValue
 */
@Entity
@Table(name = "clientes_pj")
@DiscriminatorValue("PJ") // CORREÇÃO: Valor discriminador simplificado
@PrimaryKeyJoinColumn(name = "cliente_id")
public class ClientesPJ extends Clientes {

    @CNPJ(message = "CNPJ inválido")
    @NotBlank(message = "CNPJ é obrigatório")
    @Column(name = "cnpj", nullable = false, unique = true, length = 14)
    private String cnpj;

    @NotBlank(message = "Razão Social é obrigatória")
    @Size(min = 2, max = 150, message = "Razão Social deve ter entre 2 e 150 caracteres")
    @Column(name = "razao_social", nullable = false, length = 150)
    private String razaoSocial;

    @Size(max = 150, message = "Nome fantasia deve ter no máximo 150 caracteres")
    @Column(name = "nome_fantasia", length = 150)
    private String nomeFantasia;

    @NotNull(message = "Data de constituição é obrigatória")
    @Past(message = "Data de constituição deve ser no passado")
    @Column(name = "data_constituicao", nullable = false)
    private LocalDate dataConstituicao;

    @Size(max = 20, message = "Inscrição Estadual deve ter no máximo 20 caracteres")
    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    @Size(max = 20, message = "Inscrição Municipal deve ter no máximo 20 caracteres")
    @Column(name = "inscricao_municipal", length = 20)
    private String inscricaoMunicipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "porte_empresa")
    private PorteEmpresa porteEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "regime_tributario")
    private RegimeTributario regimeTributario;

    @Size(max = 100, message = "Ramo de atividade deve ter no máximo 100 caracteres")
    @Column(name = "ramo_atividade", length = 100)
    private String ramoAtividade;

    // --- Enums ---
    public enum PorteEmpresa {
        MEI("MEI", "Microempreendedor Individual"),
        MICRO("MICRO", "Microempresa"),
        PEQUENA("PEQUENA", "Empresa de Pequeno Porte"),
        MEDIA("MEDIA", "Empresa de Médio Porte"),
        GRANDE("GRANDE", "Empresa de Grande Porte");

        private final String codigo;
        private final String descricao;

        PorteEmpresa(String codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }

        public String getCodigo() { return codigo; }
        public String getDescricao() { return descricao; }
    }

    public enum RegimeTributario {
        SIMPLES_NACIONAL("SIMPLES", "Simples Nacional"),
        LUCRO_PRESUMIDO("PRESUMIDO", "Lucro Presumido"),
        LUCRO_REAL("REAL", "Lucro Real"),
        LUCRO_ARBITRADO("ARBITRADO", "Lucro Arbitrado");

        private final String codigo;
        private final String descricao;

        RegimeTributario(String codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }

        public String getCodigo() { return codigo; }
        public String getDescricao() { return descricao; }
    }

    // --- Construtores ---
    public ClientesPJ() {
        super();
    }

    public ClientesPJ(String cnpj, String razaoSocial, LocalDate dataConstituicao) {
        super();
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.dataConstituicao = dataConstituicao;
    }

    // --- Implementação dos Métodos Abstratos ---
    @Override
    public String getNomeCompletoOuRazaoSocial() {
        return this.razaoSocial;
    }

    @Override
    public String getDocumentoPrincipal() {
        return this.cnpj;
    }

    /**
     * CORREÇÃO: Implementação completa do método getTipoPessoa()
     */
    @Override
    public String getTipoPessoa() {
        return "Pessoa Jurídica";
    }

    // --- Métodos de Negócio ---
    public int getIdadeEmpresa() {
        if (this.dataConstituicao == null) return 0;
        return Period.between(this.dataConstituicao, LocalDate.now()).getYears();
    }

    public boolean isEmpresaRecente() {
        return getIdadeEmpresa() < 2;
    }

    public boolean isEmpresaConsolidada() {
        return getIdadeEmpresa() >= 5;
    }

    public String getNomeParaExibicao() {
        return (this.nomeFantasia != null && !this.nomeFantasia.isBlank()) 
            ? this.nomeFantasia 
            : this.razaoSocial;
    }

    public String getCnpjFormatado() {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.substring(0, 2) + "." + 
               cnpj.substring(2, 5) + "." + 
               cnpj.substring(5, 8) + "/" + 
               cnpj.substring(8, 12) + "-" + 
               cnpj.substring(12);
    }

    public boolean isMEI() {
        return PorteEmpresa.MEI.equals(this.porteEmpresa);
    }

    public boolean isMicroEmpresa() {
        return PorteEmpresa.MICRO.equals(this.porteEmpresa);
    }

    public boolean isPequenaEmpresa() {
        return PorteEmpresa.PEQUENA.equals(this.porteEmpresa);
    }

    // --- Getters e Setters ---
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public LocalDate getDataConstituicao() { return dataConstituicao; }
    public void setDataConstituicao(LocalDate dataConstituicao) { this.dataConstituicao = dataConstituicao; }

    public String getInscricaoEstadual() { return inscricaoEstadual; }
    public void setInscricaoEstadual(String inscricaoEstadual) { this.inscricaoEstadual = inscricaoEstadual; }

    public String getInscricaoMunicipal() { return inscricaoMunicipal; }
    public void setInscricaoMunicipal(String inscricaoMunicipal) { this.inscricaoMunicipal = inscricaoMunicipal; }

    public PorteEmpresa getPorteEmpresa() { return porteEmpresa; }
    public void setPorteEmpresa(PorteEmpresa porteEmpresa) { this.porteEmpresa = porteEmpresa; }

    public RegimeTributario getRegimeTributario() { return regimeTributario; }
    public void setRegimeTributario(RegimeTributario regimeTributario) { this.regimeTributario = regimeTributario; }

    public String getRamoAtividade() { return ramoAtividade; }
    public void setRamoAtividade(String ramoAtividade) { this.ramoAtividade = ramoAtividade; }

    // --- toString, equals e hashCode ---
    @Override
    public String toString() {
        return "ClientesPJ{" +
                "razaoSocial='" + razaoSocial + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", nomeFantasia='" + nomeFantasia + '\'' +
                ", codigoCliente='" + getCodigoCliente() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientesPJ)) return false;
        ClientesPJ that = (ClientesPJ) o;
        return cnpj != null && cnpj.equals(that.cnpj);
    }

    @Override
    public int hashCode() {
        return cnpj != null ? cnpj.hashCode() : 0;
    }
}