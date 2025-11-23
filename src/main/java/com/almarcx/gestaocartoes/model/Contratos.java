package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Entidade que representa contratos de cartões
 */
@Entity
@Table(name = "contratos")
public class Contratos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Clientes cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_interna_id", nullable = false)
    private ContaInterna contaInterna;

    @Column(name = "numero_contrato", nullable = false, unique = true, length = 30)
    @NotBlank(message = "Número do contrato é obrigatório")
    @Size(max = 30, message = "Número do contrato não pode exceder 30 caracteres")
    private String numeroContrato;

    @Column(name = "tipo_contrato", nullable = false, length = 30)
    @NotNull(message = "Tipo de contrato é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoContrato tipoContrato;

    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    private StatusContrato status = StatusContrato.ATIVO;

    @Column(name = "data_inicio", nullable = false)
    @NotNull(message = "Data de início é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFim;

    @Column(name = "data_vencimento")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataVencimento;

    @Column(name = "valor_contrato", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor do contrato deve ser maior ou igual a zero")
    @Digits(integer = 13, fraction = 2, message = "Valor deve ter no máximo 13 dígitos inteiros e 2 decimais")
    private BigDecimal valorContrato;

    @Column(name = "taxa_juros", precision = 5, scale = 4)
    @DecimalMin(value = "0.0", inclusive = true, message = "Taxa de juros deve ser maior ou igual a zero")
    @DecimalMax(value = "1.0", inclusive = true, message = "Taxa de juros deve ser menor ou igual a 1 (100%)")
    private BigDecimal taxaJuros;

    @Column(name = "dia_vencimento")
    @Min(value = 1)
    @Max(value = 31)
    private Integer diaVencimento;

    @Column(name = "renovacao_automatica")
    private Boolean renovacaoAutomatica = false;

    @Column(name = "observacoes", length = 500)
    @Size(max = 500, message = "Observações não podem exceder 500 caracteres")
    private String observacoes;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Cartoes> cartoes = new ArrayList<>();

    // Enums
    public enum TipoContrato {
        CARTAO_CREDITO("Cartão de Crédito"),
        CARTAO_DEBITO("Cartão de Débito"),
        CARTAO_ALIMENTACAO("Cartão Alimentação"),
        CARTAO_REFEICAO("Cartão Refeição"),
        CARTAO_COMBUSTIVEL("Cartão Combustível"),
        MULTIPLO("Múltiplos Cartões");

        private final String descricao;
        TipoContrato(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    public enum StatusContrato {
        ATIVO("Ativo"),
        INATIVO("Inativo"),
        SUSPENSO("Suspenso"),
        CANCELADO("Cancelado"),
        VENCIDO("Vencido"),
        PENDENTE_APROVACAO("Pendente Aprovação"),
        EM_RENOVACAO("Em Renovação");

        private final String descricao;
        StatusContrato(String descricao) { this.descricao = descricao; }
        public String getDescricao() { return descricao; }
    }

    // Construtores
    public Contratos() {}

    public Contratos(Clientes cliente, ContaInterna contaInterna, String numeroContrato, TipoContrato tipoContrato,
                     LocalDate dataInicio, BigDecimal valorContrato) {
        this.cliente = cliente;
        this.contaInterna = contaInterna;
        this.numeroContrato = numeroContrato;
        this.tipoContrato = tipoContrato;
        this.dataInicio = dataInicio;
        this.valorContrato = valorContrato;
        this.status = StatusContrato.ATIVO;
        this.renovacaoAutomatica = false;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Clientes getCliente() { return cliente; } // ✅ CORRIGIDO: era "clientes"
    public void setCliente(Clientes cliente) { this.cliente = cliente; }

    public ContaInterna getContaInterna() { return contaInterna; }
    public void setContaInterna(ContaInterna contaInterna) { this.contaInterna = contaInterna; }

    public String getNumeroContrato() { return numeroContrato; }
    public void setNumeroContrato(String numeroContrato) { this.numeroContrato = numeroContrato; }

    public TipoContrato getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(TipoContrato tipoContrato) { this.tipoContrato = tipoContrato; }

    public StatusContrato getStatus() { return status; }
    public void setStatus(StatusContrato status) { this.status = status; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public BigDecimal getValorContrato() { return valorContrato; }
    public void setValorContrato(BigDecimal valorContrato) { this.valorContrato = valorContrato; }

    public BigDecimal getTaxaJuros() { return taxaJuros; }
    public void setTaxaJuros(BigDecimal taxaJuros) { this.taxaJuros = taxaJuros; }

    public Integer getDiaVencimento() { return diaVencimento; }
    public void setDiaVencimento(Integer diaVencimento) { this.diaVencimento = diaVencimento; }

    public Boolean getRenovacaoAutomatica() { return renovacaoAutomatica; }
    public void setRenovacaoAutomatica(Boolean renovacaoAutomatica) { this.renovacaoAutomatica = renovacaoAutomatica; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public List<Cartoes> getCartoes() { return cartoes; }
    public void setCartoes(List<Cartoes> cartoes) { this.cartoes = cartoes; }

    // Métodos utilitários
    public boolean isAtivo() { return StatusContrato.ATIVO.equals(status); }

    public boolean isVencido() {
        return dataVencimento != null && LocalDate.now().isAfter(dataVencimento);
    }

    public boolean isVigente() {
        LocalDate hoje = LocalDate.now();
        boolean inicioOk = dataInicio == null || !hoje.isBefore(dataInicio);
        boolean fimOk = dataFim == null || !hoje.isAfter(dataFim);
        return isAtivo() && !isVencido() && inicioOk && fimOk;
    }

    public long getDiasParaVencimento() {
        return dataVencimento != null
                ? java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dataVencimento)
                : Long.MAX_VALUE;
    }

    public boolean isProximoVencimento() { return getDiasParaVencimento() <= 30; }

    public BigDecimal getValorJurosMensal() {
        return valorContrato != null && taxaJuros != null
                ? valorContrato.multiply(taxaJuros)
                : BigDecimal.ZERO;
    }

    public void renovarContrato(LocalDate novaDataFim) {
        if (!Boolean.TRUE.equals(renovacaoAutomatica)) {
            throw new IllegalStateException("Contrato não permite renovação automática");
        }
        this.dataFim = novaDataFim;
        this.status = StatusContrato.EM_RENOVACAO;
    }

    public void cancelarContrato(String motivo) {
        this.status = StatusContrato.CANCELADO;
        if (motivo != null && !motivo.trim().isEmpty()) {
            this.observacoes = (this.observacoes != null ? this.observacoes + " | " : "") + "Cancelado: " + motivo;
        }
    }

    public void suspenderContrato(String motivo) {
        this.status = StatusContrato.SUSPENSO;
        if (motivo != null && !motivo.trim().isEmpty()) {
            this.observacoes = (this.observacoes != null ? this.observacoes + " | " : "") + "Suspenso: " + motivo;
        }
    }

    public void reativarContrato() {
        if (StatusContrato.SUSPENSO.equals(status) || StatusContrato.INATIVO.equals(status)) {
            this.status = StatusContrato.ATIVO;
        } else {
            throw new IllegalStateException("Contrato não pode ser reativado no status atual: " + status);
        }
    }

    @PrePersist
    @PreUpdate
    private void validarContrato() {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        if (diaVencimento != null && (diaVencimento < 1 || diaVencimento > 31)) {
            throw new IllegalArgumentException("Dia de vencimento deve estar entre 1 e 31");
        }
        if (isVencido() && !StatusContrato.VENCIDO.equals(status)) {
            this.status = StatusContrato.VENCIDO;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contratos contratos = (Contratos) o;
        return Objects.equals(id, contratos.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}