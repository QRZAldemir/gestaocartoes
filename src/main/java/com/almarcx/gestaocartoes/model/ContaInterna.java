package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conta_interna")
public class ContaInterna {

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
    private TipoConta tipoConta;

    @Column(name = "saldo", precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "limite_credito", precision = 15, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "saldo_disponivel", precision = 15, scale = 2)
    private BigDecimal saldoDisponivel = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusConta status = StatusConta.ATIVA;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_encerramento")
    private LocalDateTime dataEncerramento;

    // Relacionamentos
    @NotNull(message = "Banco é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_id", nullable = false)
    private Banco banco;

    @NotNull(message = "Cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Clientes cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_externa_id")
    private ContaBancariaExterna contaBancariaExterna;

    @OneToMany(mappedBy = "contaInterna", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contratos> contratos = new ArrayList<>();

    // Enums
    public enum TipoConta {
        CORRENTE("Conta Corrente"),
        POUPANCA("Conta Poupança"),
        SALARIO("Conta Salário"),
        PAGAMENTOS("Conta de Pagamentos");

        private final String descricao;

        TipoConta(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public enum StatusConta {
        ATIVA("Ativa"),
        BLOQUEADA("Bloqueada"),
        ENCERRADA("Encerrada"),
        PENDENTE("Pendente");

        private final String descricao;

        StatusConta(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Construtores
    public ContaInterna() {
        this.dataAbertura = LocalDateTime.now();
    }

    public ContaInterna(Banco banco, Clientes cliente, String numeroConta, String agencia, TipoConta tipoConta) {
        this();
        this.banco = banco;
        this.cliente = cliente;
        this.numeroConta = numeroConta;
        this.agencia = agencia;
        this.tipoConta = tipoConta;
        calcularSaldoDisponivel();
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

    public TipoConta getTipoConta() {
        return tipoConta;
    }

    public void setTipoConta(TipoConta tipoConta) {
        this.tipoConta = tipoConta;
    }

    public BigDecimal getSaldo() {
        return saldo != null ? saldo : BigDecimal.ZERO;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
        calcularSaldoDisponivel();
    }

    public BigDecimal getLimiteCredito() {
        return limiteCredito != null ? limiteCredito : BigDecimal.ZERO;
    }

    public void setLimiteCredito(BigDecimal limiteCredito) {
        this.limiteCredito = limiteCredito;
        calcularSaldoDisponivel();
    }

    public BigDecimal getSaldoDisponivel() {
        return saldoDisponivel != null ? saldoDisponivel : BigDecimal.ZERO;
    }

    public void setSaldoDisponivel(BigDecimal saldoDisponivel) {
        this.saldoDisponivel = saldoDisponivel;
    }

    public StatusConta getStatus() {
        return status;
    }

    public void setStatus(StatusConta status) {
        this.status = status;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDateTime dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
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

    public ContaBancariaExterna getContaBancariaExterna() {
        return contaBancariaExterna;
    }

    public void setContaBancariaExterna(ContaBancariaExterna contaBancariaExterna) {
        this.contaBancariaExterna = contaBancariaExterna;
    }

    public List<Contratos> getContratos() {
        return contratos;
    }

    public void setContratos(List<Contratos> contratos) {
        this.contratos = contratos;
    }

    // Métodos de negócio
    private void calcularSaldoDisponivel() {
        this.saldoDisponivel = getSaldo().add(getLimiteCredito());
    }

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
        return StatusConta.ATIVA.equals(status);
    }

    public boolean isBloqueada() {
        return StatusConta.BLOQUEADA.equals(status);
    }

    public boolean isEncerrada() {
        return StatusConta.ENCERRADA.equals(status);
    }

    public boolean possuiSaldoSuficiente(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return getSaldoDisponivel().compareTo(valor) >= 0;
    }

    public void creditar(BigDecimal valor) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
            this.saldo = getSaldo().add(valor);
            calcularSaldoDisponivel();
        }
    }

    public boolean debitar(BigDecimal valor) {
        if (possuiSaldoSuficiente(valor)) {
            this.saldo = getSaldo().subtract(valor);
            calcularSaldoDisponivel();
            return true;
        }
        return false;
    }

    public boolean podeAbrirContrato() {
        return isAtiva() && cliente != null && cliente.isAtiva();
    }

    @PrePersist
    @PreUpdate
    protected void validarConta() {
        if (dataAbertura == null) {
            dataAbertura = LocalDateTime.now();
        }
        
        if (status == null) {
            status = StatusConta.ATIVA;
        }
        
        calcularSaldoDisponivel();
    }

    @Override
    public String toString() {
        return "Conta{" +
                "id=" + id +
                ", numeroConta='" + numeroConta + '\'' +
                ", agencia='" + agencia + '\'' +
                ", tipoConta=" + tipoConta +
                ", saldo=" + saldo +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContaInterna conta = (ContaInterna) o;
        return banco != null && cliente != null && numeroConta != null &&
               banco.equals(conta.banco) && cliente.equals(conta.cliente) && 
               numeroConta.equals(conta.numeroConta);
    }

    @Override
    public int hashCode() {
        int result = banco != null ? banco.hashCode() : 0;
        result = 31 * result + (cliente != null ? cliente.hashCode() : 0);
        result = 31 * result + (numeroConta != null ? numeroConta.hashCode() : 0);
        return result;
    }
}