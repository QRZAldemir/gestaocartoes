package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.time.Period;

/**
 * Entidade que representa um Cliente do tipo Pessoa Física.
 * Herda todos os atributos de Clientes (e consequentemente de Pessoas).
 *
 * CORREÇÕES:
 * - Implementado método getTipoPessoa() corretamente
 * - Corrigida a anotação @DiscriminatorValue
 */
@Entity
@Table(name = "clientes_pf")
@DiscriminatorValue("PF") // CORREÇÃO: Valor discriminador simplificado
@PrimaryKeyJoinColumn(name = "cliente_id")
public class ClientesPF extends Clientes {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Sobrenome é obrigatório")
    @Size(min = 2, max = 100, message = "Sobrenome deve ter entre 2 e 100 caracteres")
    @Column(name = "sobrenome", nullable = false, length = 100)
    private String sobrenome;

    @CPF(message = "CPF inválido")
    @NotBlank(message = "CPF é obrigatório")
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Size(max = 50, message = "Nacionalidade deve ter no máximo 50 caracteres")
    @Column(name = "nacionalidade", length = 50)
    private String nacionalidade = "Brasileira";

    @Size(max = 50, message = "Estado civil deve ter no máximo 50 caracteres")
    @Column(name = "estado_civil", length = 50)
    private String estadoCivil;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo")
    private Sexo sexo;

    @Size(max = 100, message = "Profissão deve ter no máximo 100 caracteres")
    @Column(name = "profissao", length = 100)
    private String profissao;

    // --- Enums ---
    public enum Sexo {
        MASCULINO("M", "Masculino"),
        FEMININO("F", "Feminino"),
        NAO_INFORMADO("N", "Não Informado");

        private final String codigo;
        private final String descricao;

        Sexo(String codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }

        public String getCodigo() { return codigo; }
        public String getDescricao() { return descricao; }
    }

    // --- Construtores ---
    public ClientesPF() {
        super();
    }

    public ClientesPF(String nome, String sobrenome, String cpf, LocalDate dataNascimento) {
        super();
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
    }

    // --- Implementação dos Métodos Abstratos ---
    @Override
    public String getNomeCompletoOuRazaoSocial() {
        return this.nome + " " + this.sobrenome;
    }

    @Override
    public String getDocumentoPrincipal() {
        return this.cpf;
    }

    /**
     * CORREÇÃO: Implementação completa do método getTipoPessoa()
     */
    @Override
    public String getTipoPessoa() {
        return "Pessoa Física";
    }

    // --- Métodos de Negócio ---
    public int getIdade() {
        if (this.dataNascimento == null) return 0;
        return Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }

    public boolean isMaiorDeIdade() {
        return getIdade() >= 18;
    }

    public boolean isMenorDeIdade() {
        return getIdade() < 18;
    }

    public boolean isIdoso() {
        return getIdade() >= 65;
    }

    public String getCpfFormatado() {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + 
               cpf.substring(3, 6) + "." + 
               cpf.substring(6, 9) + "-" + 
               cpf.substring(9);
    }

    // --- Getters e Setters ---
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSobrenome() { return sobrenome; }
    public void setSobrenome(String sobrenome) { this.sobrenome = sobrenome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }

    public String getEstadoCivil() { return estadoCivil; }
    public void setEstadoCivil(String estadoCivil) { this.estadoCivil = estadoCivil; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public String getProfissao() { return profissao; }
    public void setProfissao(String profissao) { this.profissao = profissao; }

    // --- toString, equals e hashCode ---
    @Override
    public String toString() {
        return "ClientesPF{" +
                "nome='" + nome + '\'' +
                ", sobrenome='" + sobrenome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", codigoCliente='" + getCodigoCliente() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientesPF)) return false;
        ClientesPF that = (ClientesPF) o;
        return cpf != null && cpf.equals(that.cpf);
    }

    @Override
    public int hashCode() {
        return cpf != null ? cpf.hashCode() : 0;
    }
}