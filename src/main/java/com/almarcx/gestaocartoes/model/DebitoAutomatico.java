package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "debito_automatico")
public class DebitoAutomatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Conta bancária externa é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_externa_id", nullable = false)
    private ContaBancariaExterna contaBancariaExterna;

    @NotNull(message = "Conta interna é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_interna_id", nullable = false)
    private ContaInterna contaInterna;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "Valor deve ter no máximo 13 dígitos inteiros e 2 decimais")
    @Column(name = "valor", precision = 15, scale = 2, nullable = false)
    private BigDecimal valor;

    @Min(value = 1, message = "Dia do débito deve ser entre 1 e 31")
    @Max(value = 31, message = "Dia do débito deve ser entre 1 e 31")
    @Column(name = "dia_debito", nullable = false)
    private Integer diaDebito;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_debito", nullable = false)
    private TipoDebito tipoDebito;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusDebito status = StatusDebito.ATIVO;

    @Column(name = "data_inicio", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFim;

    @Column(name = "data_ultimo_debito")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataUltimoDebito;

    @Column(name = "data_proximo_debito")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataProximoDebito;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    // Enums
    public enum TipoDebito {
        RECARGA_AUTOMATICA("Recarga Automática"),
        PAGAMENTO_FATURA("Pagamento de Fatura"),
        MANUTENCAO_CONTA("Manutenção de Conta"),
        ANUIDADE_CARTAO("Anuidade de Cartão");

        private final String descricao;

        TipoDebito(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum StatusDebito {
        ATIVO("Ativo"),
        INATIVO("Inativo"),
        SUSPENSO("Suspenso"),
        CANCELADO("Cancelado");

        private final String descricao;

        StatusDebito(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Construtores
    public DebitoAutomatico() {}

    public DebitoAutomatico(ContaBancariaExterna contaBancariaExterna, ContaInterna contaInterna,
                           BigDecimal valor, Integer diaDebito, TipoDebito tipoDebito) {
        this.contaBancariaExterna = contaBancariaExterna;
        this.contaInterna = contaInterna;
        this.valor = valor;
        this.diaDebito = diaDebito;
        this.tipoDebito = tipoDebito;
        this.status = StatusDebito.ATIVO;
        this.dataInicio = LocalDate.now();
        calcularProximoDebito();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ContaBancariaExterna getContaBancariaExterna() {
        return contaBancariaExterna;
    }

    public void setContaBancariaExterna(ContaBancariaExterna contaBancariaExterna) {
        this.contaBancariaExterna = contaBancariaExterna;
    }

    public ContaInterna getContaInterna() {
        return contaInterna;
    }

    public void setContaInterna(ContaInterna contaInterna) {
        this.contaInterna = contaInterna;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getDiaDebito() {
        return diaDebito;
    }

    public void setDiaDebito(Integer diaDebito) {
        this.diaDebito = diaDebito;
        calcularProximoDebito();
    }

    public TipoDebito getTipoDebito() {
        return tipoDebito;
    }

    public void setTipoDebito(TipoDebito tipoDebito) {
        this.tipoDebito = tipoDebito;
    }

    public StatusDebito getStatus() {
        return status;
    }

    public void setStatus(StatusDebito status) {
        this.status = status;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
        calcularProximoDebito();
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public LocalDate getDataUltimoDebito() {
        return dataUltimoDebito;
    }

    public void setDataUltimoDebito(LocalDate dataUltimoDebito) {
        this.dataUltimoDebito = dataUltimoDebito;
    }

    public LocalDate getDataProximoDebito() {
        return dataProximoDebito;
    }

    public void setDataProximoDebito(LocalDate dataProximoDebito) {
        this.dataProximoDebito = dataProximoDebito;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
    public boolean isAtivo() {
        return StatusDebito.ATIVO.equals(status);
    }

    public boolean isVigente() {
        LocalDate hoje = LocalDate.now();
        boolean inicioOk = dataInicio == null || !hoje.isBefore(dataInicio);
        boolean fimOk = dataFim == null || !hoje.isAfter(dataFim);
        return isAtivo() && inicioOk && fimOk;
    }

    public boolean isDiaDebito() {
        return isVigente() && dataProximoDebito != null && 
               LocalDate.now().equals(dataProximoDebito);
    }

    public void calcularProximoDebito() {
        if (diaDebito != null) {
            LocalDate hoje = LocalDate.now();
            LocalDate proximoMes = hoje.plusMonths(1);
            
            // Ajustar para o dia correto do mês
            int diaMes = Math.min(diaDebito, proximoMes.lengthOfMonth());
            this.dataProximoDebito = proximoMes.withDayOfMonth(diaMes);
        }
    }

    public void executarDebito() {
        if (isDiaDebito()) {
            this.dataUltimoDebito = LocalDate.now();
            calcularProximoDebito();
        }
    }

    public void ativar() {
        this.status = StatusDebito.ATIVO;
        calcularProximoDebito();
    }

    public void desativar() {
        this.status = StatusDebito.INATIVO;
    }

    public void suspender(String motivo) {
        this.status = StatusDebito.SUSPENSO;
        if (motivo != null && !motivo.trim().isEmpty()) {
            this.descricao = (this.descricao != null ? this.descricao + " | " : "") + "Suspenso: " + motivo;
        }
    }

    public void cancelar(String motivo) {
        this.status = StatusDebito.CANCELADO;
        this.dataFim = LocalDate.now();
        if (motivo != null && !motivo.trim().isEmpty()) {
            this.descricao = (this.descricao != null ? this.descricao + " | " : "") + "Cancelado: " + motivo;
        }
    }

    @PrePersist
    @PreUpdate
    private void validarDebitoAutomatico() {
        if (dataInicio == null) {
            dataInicio = LocalDate.now();
        }
        
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        
        if (diaDebito != null && (diaDebito < 1 || diaDebito > 31)) {
            throw new IllegalArgumentException("Dia do débito deve estar entre 1 e 31");
        }
        
        calcularProximoDebito();
    }

    @Override
    public String toString() {
        return "DebitoAutomatico{" +
                "id=" + id +
                ", valor=" + valor +
                ", diaDebito=" + diaDebito +
                ", tipoDebito=" + tipoDebito +
                ", status=" + status +
                '}';
    }
}

