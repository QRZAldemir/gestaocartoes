package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transferencia")
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Conta de origem é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_origem_id", nullable = false)
    private ContaBancariaExterna contaOrigem;

    @NotNull(message = "Conta de destino é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_destino_id", nullable = false)
    private ContaInterna contaDestino;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "Valor deve ter no máximo 13 dígitos inteiros e 2 decimais")
    @Column(name = "valor", precision = 15, scale = 2, nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transferencia", nullable = false)
    private TipoTransferencia tipoTransferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusTransferencia status = StatusTransferencia.PENDENTE;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "codigo_autorizacao", length = 50)
    private String codigoAutorizacao;

    @Column(name = "data_processamento")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataProcessamento;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    // Enums
    public enum TipoTransferencia {
        MANUAL("Transferência Manual"),
        DEBITO_AUTOMATICO("Débito Automático"),
        RECARGA_CARTAO("Recarga de Cartão"),
        PAGAMENTO_FATURA("Pagamento de Fatura");

        private final String descricao;

        TipoTransferencia(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum StatusTransferencia {
        PENDENTE("Pendente"),
        PROCESSANDO("Processando"),
        CONCLUIDA("Concluída"),
        FALHOU("Falhou"),
        CANCELADA("Cancelada");

        private final String descricao;

        StatusTransferencia(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Construtores
    public Transferencia() {}

    public Transferencia(ContaBancariaExterna contaOrigem, ContaInterna contaDestino, 
                        BigDecimal valor, TipoTransferencia tipoTransferencia) {
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
        this.tipoTransferencia = tipoTransferencia;
        this.status = StatusTransferencia.PENDENTE;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContaBancariaExterna getContaOrigem() {
        return contaOrigem;
    }

    public void setContaOrigem(ContaBancariaExterna contaOrigem) {
        this.contaOrigem = contaOrigem;
    }

    public ContaInterna getContaDestino() {
        return contaDestino;
    }

    public void setContaDestino(ContaInterna contaDestino) {
        this.contaDestino = contaDestino;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public TipoTransferencia getTipoTransferencia() {
        return tipoTransferencia;
    }

    public void setTipoTransferencia(TipoTransferencia tipoTransferencia) {
        this.tipoTransferencia = tipoTransferencia;
    }

    public StatusTransferencia getStatus() {
        return status;
    }

    public void setStatus(StatusTransferencia status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCodigoAutorizacao() {
        return codigoAutorizacao;
    }

    public void setCodigoAutorizacao(String codigoAutorizacao) {
        this.codigoAutorizacao = codigoAutorizacao;
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }

    public void setDataProcessamento(LocalDateTime dataProcessamento) {
        this.dataProcessamento = dataProcessamento;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    // Métodos de negócio
    public boolean isPendente() {
        return StatusTransferencia.PENDENTE.equals(status);
    }

    public boolean isConcluida() {
        return StatusTransferencia.CONCLUIDA.equals(status);
    }

    public boolean isFalhou() {
        return StatusTransferencia.FALHOU.equals(status);
    }

    public void processar() {
        this.status = StatusTransferencia.PROCESSANDO;
        this.dataProcessamento = LocalDateTime.now();
    }

    public void concluir(String codigoAutorizacao) {
        this.status = StatusTransferencia.CONCLUIDA;
        this.codigoAutorizacao = codigoAutorizacao;
        this.dataProcessamento = LocalDateTime.now();
    }

    public void falhar(String motivo) {
        this.status = StatusTransferencia.FALHOU;
        this.descricao = (this.descricao != null ? this.descricao + " | " : "") + "Falha: " + motivo;
        this.dataProcessamento = LocalDateTime.now();
    }

    public void cancelar(String motivo) {
        this.status = StatusTransferencia.CANCELADA;
        this.descricao = (this.descricao != null ? this.descricao + " | " : "") + "Cancelada: " + motivo;
    }

    @PrePersist
    @PreUpdate
    private void validarTransferencia() {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transferência deve ser maior que zero");
        }
        
        if (contaOrigem != null && contaDestino != null && contaOrigem.equals(contaDestino)) {
            throw new IllegalArgumentException("Conta de origem e destino não podem ser iguais");
        }
    }

    @Override
    public String toString() {
        return "Transferencia{" +
                "id=" + id +
                ", valor=" + valor +
                ", tipoTransferencia=" + tipoTransferencia +
                ", status=" + status +
                '}';
    }
}

