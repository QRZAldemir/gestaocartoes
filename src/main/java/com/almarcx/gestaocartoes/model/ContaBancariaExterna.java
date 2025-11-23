package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conta_bancaria_externa")
public class ContaBancariaExterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Número da conta é obrigatório")
    @Pattern(regexp = "\\d{5,10}", message = "Número da conta deve ter entre 5 e 10 dígitos")
    @Column(name = "numero_conta", nullable = false, length = 10)
    private String numeroConta;

    @NotBlank(message = "Agência é obrigatória")
    @Pattern(regexp = "\\d{4,5}", message = "Agência deve ter 4 ou 5 dígitos")
    @Column(name = "agencia", nullable = false, length = 5)
    private String agencia;

    @Size(max = 1, message = "Dígito da agência deve ter 1 caractere")
    @Column(name = "digito_agencia", length = 1)
    private String digitoAgencia;

    @Size(max = 1, message = "Dígito da conta deve ter 1 caractere")
    @Column(name = "digito_conta", length = 1)
    private String digitoConta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false)
    private TipoContaBancaria tipoConta;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusContaBancaria status = StatusContaBancaria.ATIVA;

    @Column(name = "debito_automatico_habilitado")
    private Boolean debitoAutomaticoHabilitado = false;

    @Column(name = "data_vinculacao", nullable = false)
    private LocalDateTime dataVinculacao;

    @Column(name = "data_desvinculacao")
    private LocalDateTime dataDesvinculacao;

    // Relacionamentos
    @NotNull(message = "Banco é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_id", nullable = false)
    private Banco banco;

    @NotNull(message = "Cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Clientes cliente;

    @OneToMany(mappedBy = "contaBancariaExterna", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContaInterna> contasInternas = new ArrayList<>();

    @OneToMany(mappedBy = "contaOrigem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transferencia> transferencias = new ArrayList<>();

    @OneToMany(mappedBy = "contaBancariaExterna", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DebitoAutomatico> debitosAutomaticos = new ArrayList<>();

    // Enums
    public enum TipoContaBancaria {
        CORRENTE("Conta Corrente"),
        POUPANCA("Conta Poupança"),
        SALARIO("Conta Salário");

        private final String descricao;

        TipoContaBancaria(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum StatusContaBancaria {
        ATIVA("Ativa"),
        BLOQUEADA("Bloqueada"),
        DESVINCULADA("Desvinculada"),
        PENDENTE("Pendente");

        private final String descricao;

        StatusContaBancaria(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Construtores
    public ContaBancariaExterna() {
        this.dataVinculacao = LocalDateTime.now();
    }

    public ContaBancariaExterna(Banco banco, Clientes cliente, String numeroConta, String agencia, TipoContaBancaria tipoConta) {
        this();
        this.banco = banco;
        this.cliente = cliente;
        this.numeroConta = numeroConta;
        this.agencia = agencia;
        this.tipoConta = tipoConta;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    public void setNumeroConta(String numeroConta) {
        this.numeroConta = numeroConta;
    }

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public String getDigitoAgencia() {
        return digitoAgencia;
    }

    public void setDigitoAgencia(String digitoAgencia) {
        this.digitoAgencia = digitoAgencia;
    }

    public String getDigitoConta() {
        return digitoConta;
    }

    public void setDigitoConta(String digitoConta) {
        this.digitoConta = digitoConta;
    }

    public TipoContaBancaria getTipoConta() {
        return tipoConta;
    }

    public void setTipoConta(TipoContaBancaria tipoConta) {
        this.tipoConta = tipoConta;
    }

    public StatusContaBancaria getStatus() {
        return status;
    }

    public void setStatus(StatusContaBancaria status) {
        this.status = status;
    }

    public Boolean getDebitoAutomaticoHabilitado() {
        return debitoAutomaticoHabilitado;
    }

    public void setDebitoAutomaticoHabilitado(Boolean debitoAutomaticoHabilitado) {
        this.debitoAutomaticoHabilitado = debitoAutomaticoHabilitado;
    }

    public LocalDateTime getDataVinculacao() {
        return dataVinculacao;
    }

    public void setDataVinculacao(LocalDateTime dataVinculacao) {
        this.dataVinculacao = dataVinculacao;
    }

    public LocalDateTime getDataDesvinculacao() {
        return dataDesvinculacao;
    }

    public void setDataDesvinculacao(LocalDateTime dataDesvinculacao) {
        this.dataDesvinculacao = dataDesvinculacao;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public Clientes getCliente() {
        return cliente;
    }

    public void setCliente(Clientes cliente) {
        this.cliente = cliente;
    }

    public List<ContaInterna> getContasInternas() {
        return contasInternas;
    }

    public void setContasInternas(List<ContaInterna> contasInternas) {
        this.contasInternas = contasInternas;
    }

    public List<Transferencia> getTransferencias() {
        return transferencias;
    }

    public void setTransferencias(List<Transferencia> transferencias) {
        this.transferencias = transferencias;
    }

    public List<DebitoAutomatico> getDebitosAutomaticos() {
        return debitosAutomaticos;
    }

    public void setDebitosAutomaticos(List<DebitoAutomatico> debitosAutomaticos) {
        this.debitosAutomaticos = debitosAutomaticos;
    }

    // Métodos de negócio
    public String getAgenciaCompleta() {
        if (digitoAgencia != null && !digitoAgencia.isBlank()) {
            return agencia + "-" + digitoAgencia;
        }
        return agencia;
    }

    public String getContaCompleta() {
        if (digitoConta != null && !digitoConta.isBlank()) {
            return numeroConta + "-" + digitoConta;
        }
        return numeroConta;
    }

    public String getDescricaoCompleta() {
        return banco.getNomeComCodigo() + " - Ag: " + getAgenciaCompleta() + " - CC: " + getContaCompleta();
    }

    public boolean isAtiva() {
        return StatusContaBancaria.ATIVA.equals(status);
    }

    public boolean isDebitoAutomaticoHabilitado() {
        return Boolean.TRUE.equals(debitoAutomaticoHabilitado);
    }

    public void habilitarDebitoAutomatico() {
        this.debitoAutomaticoHabilitado = true;
    }

    public void desabilitarDebitoAutomatico() {
        this.debitoAutomaticoHabilitado = false;
    }

    public void desvincular() {
        this.status = StatusContaBancaria.DESVINCULADA;
        this.dataDesvinculacao = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void validarContaBancaria() {
        if (dataVinculacao == null) {
            dataVinculacao = LocalDateTime.now();
        }
        
        if (status == null) {
            status = StatusContaBancaria.ATIVA;
        }
    }

    @Override
    public String toString() {
        return "ContaBancariaExterna{" +
                "id=" + id +
                ", numeroConta='" + numeroConta + '\'' +
                ", agencia='" + agencia + '\'' +
                ", tipoConta=" + tipoConta +
                ", status=" + status +
                '}';
    }
}

