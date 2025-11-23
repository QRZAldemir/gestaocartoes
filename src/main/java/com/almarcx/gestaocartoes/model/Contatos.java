package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Entity
@Table(name = "contatos", indexes = {
    @Index(name = "idx_pessoa_tipo", columnList = "pessoa_id, tipo"),
    @Index(name = "idx_contato_principal", columnList = "principal"),
    @Index(name = "idx_contato_ativo", columnList = "ativo")
})
public class Contatos {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String PHONE_CLEAN_REGEX = "\\D";
    private static final String LINKEDIN_REGEX = "^https://www\\.linkedin\\.com/in/[a-zA-Z0-9-]+/?$";
    private static final String WEBSITE_REGEX = "^(https?:\\/\\/)?([\\w\\-]+\\.)+[a-zA-Z]{2,}([\\/\\?\\#].*)?$";
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern LINKEDIN_PATTERN = Pattern.compile(LINKEDIN_REGEX);
    private static final Pattern WEBSITE_PATTERN = Pattern.compile(WEBSITE_REGEX);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoas pessoa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @NotNull(message = "Tipo de contato é obrigatório")
    private TipoContato tipo;

    @Column(name = "valor", nullable = false, length = 100)
    @NotBlank(message = "Valor do contato é obrigatório")
    @Size(max = 100, message = "Valor não pode exceder 100 caracteres")
    private String valor;

    @Column(name = "principal")
    private Boolean principal = false;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500, message = "Observações não podem exceder 500 caracteres")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    public enum TipoContato {
        EMAIL("E-mail"),
        TELEFONE_FIXO("Telefone Fixo"),
        TELEFONE_CELULAR("Telefone Celular"),
        WHATSAPP("WhatsApp"),
        TELEGRAM("Telegram"),
        LINKEDIN("LinkedIn"),
        WEBSITE("Website"),
        OUTRO("Outro");

        private final String descricao;
        
        TipoContato(String descricao) { 
            this.descricao = descricao; 
        }
        
        public String getDescricao() { 
            return descricao; 
        }
    }

    // Construtores
    public Contatos() {
        // Construtor padrão necessário para JPA
    }

    public Contatos(Pessoas pessoa, TipoContato tipo, String valor, Boolean principal, Boolean ativo, String observacoes) {
        this.pessoa = pessoa;
        this.tipo = tipo;
        this.valor = valor;
        this.principal = principal != null ? principal : false;
        this.ativo = ativo != null ? ativo : true;
        this.observacoes = observacoes;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pessoas getPessoa() { return pessoa; }
    public void setPessoa(Pessoas pessoa) { this.pessoa = pessoa; }

    public TipoContato getTipo() { return tipo; }
    public void setTipo(TipoContato tipo) { this.tipo = tipo; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public Boolean getPrincipal() { return principal != null ? principal : false; }
    public void setPrincipal(Boolean principal) { this.principal = principal; }
    
    public boolean isPrincipal() { return Boolean.TRUE.equals(principal); }
    
    public Boolean getAtivo() { return ativo != null ? ativo : true; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    
    public boolean isAtivo() { return Boolean.TRUE.equals(ativo); }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    // Métodos auxiliares
    public String getTipoNome() {
        return tipo != null ? tipo.name() : null;
    }

    public String getTipoDescricao() {
        return tipo != null ? tipo.getDescricao() : null;
    }

    public String getValorFormatado() {
        if (valor == null) return null;
        
        return switch (tipo) {
            case TELEFONE_FIXO, TELEFONE_CELULAR, WHATSAPP -> formatPhoneNumber(valor);
            case EMAIL -> valor.toLowerCase();
            default -> valor;
        };
    }

    public String getValorLimpo() {
        if (valor == null) return null;
        
        if (isTelefone()) {
            return valor.replaceAll(PHONE_CLEAN_REGEX, "");
        }
        return valor;
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null) return null;
        
        String digits = phone.replaceAll(PHONE_CLEAN_REGEX, "");
        
        return switch (digits.length()) {
            case 11 -> digits.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
            case 10 -> digits.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
            default -> phone;
        };
    }

    public boolean isEmail() {
        return TipoContato.EMAIL.equals(tipo);
    }

    public boolean isTelefone() {
        return tipo == TipoContato.TELEFONE_CELULAR || 
               tipo == TipoContato.TELEFONE_FIXO || 
               tipo == TipoContato.WHATSAPP;
    }
    
    public boolean isRedeSocial() {
        return tipo == TipoContato.LINKEDIN || 
               tipo == TipoContato.TELEGRAM;
    }

    // Validações
    @PrePersist
    @PreUpdate
    private void validate() {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidationException("Valor do contato não pode estar vazio");
        }
        
        this.valor = valor.trim();
        
        validateByType();
    }

    private void validateByType() {
        switch (tipo) {
            case EMAIL -> validateEmail();
            case TELEFONE_CELULAR, TELEFONE_FIXO, WHATSAPP -> validatePhone();
            case LINKEDIN -> validateLinkedIn();
            case WEBSITE -> validateWebsite();
            case TELEGRAM, OUTRO -> {
                // Não precisa de validação específica para Telegram e Outro
            }
        }
    }

    private void validateEmail() {
        if (!EMAIL_PATTERN.matcher(valor).matches()) {
            throw new ValidationException("E-mail inválido: " + valor);
        }
    }

    private void validatePhone() {
        String digits = valor.replaceAll(PHONE_CLEAN_REGEX, "");
        if (digits.length() != 10 && digits.length() != 11) {
            throw new ValidationException("Telefone deve ter 10 ou 11 dígitos: " + valor);
        }
    }

    private void validateLinkedIn() {
        if (!LINKEDIN_PATTERN.matcher(valor).matches()) {
            throw new ValidationException("URL do LinkedIn inválida. Formato esperado: https://www.linkedin.com/in/usuario");
        }
    }

    private void validateWebsite() {
        if (!WEBSITE_PATTERN.matcher(valor).matches()) {
            throw new ValidationException("URL do website inválida: " + valor);
        }
    }

    // Classe de exceção para validação
    public static class ValidationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ValidationException(String message) {
            super(message);
        }
    }

    // Métodos utilitários
    @Override
    public String toString() {
        return "Contatos{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", valor='" + valor + '\'' +
                ", principal=" + principal +
                ", ativo=" + ativo +
                '}';
    }
}