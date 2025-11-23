package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "cartoes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_cartao", discriminatorType = DiscriminatorType.STRING)
public abstract class Cartoes {
    
    // Enumerações (definidas uma única vez na classe base)
    public enum StatusCartao {
        ATIVO, BLOQUEADO, CANCELADO, PENDENTE
    }
    
    public enum TipoCartao {
        CREDITO, DEBITO, CREDITO_DEBITO, ALIMENTACAO, REFEICAO, TRANSPORTE, SAUDE
    }
    
    public enum BandeiraCartao {
        VISA("Visa"),
        MASTERCARD("MasterCard"),
        AMERICAN_EXPRESS("American Express"),
        ELO("Elo"),
        HIPERCARD("Hipercard"),
        OUTROS("Outras bandeiras");
        
        private final String descricao;
        
        BandeiraCartao(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    // Campos da classe base
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_cartao", length = 16, nullable = false, unique = true)
    @Pattern(regexp = "\\d{16}", message = "Número do cartão deve ter 16 dígitos")
    private String numeroCartao;
    
    @Column(name = "nome_portador", length = 100, nullable = false)
    @Size(max = 100, message = "Nome do portador não pode exceder 100 caracteres")
    private String nomePortador;
    
    @Column(name = "data_validade")
    @Future(message = "Data de validade deve ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataValidade;
    
    @Column(name = "cvv", length = 4, nullable = false)
    @Pattern(regexp = "\\d{3,4}", message = "CVV deve ter 3 ou 4 dígitos")
    private String cvv;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cartao", nullable = false, insertable = false, updatable = false)
    private TipoCartao tipoCartao;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusCartao status;
    
    @Column(name = "limite", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Limite deve ser maior ou igual a zero")
    @Digits(integer = 13, fraction = 2, message = "Limite deve ter no máximo 13 dígitos inteiros e 2 decimais")
    private BigDecimal limite;
    
    @Column(name = "limite_utilizado", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Limite utilizado deve ser maior ou igual a zero")
    @Digits(integer = 13, fraction = 2, message = "Limite utilizado deve ter no máximo 13 dígitos inteiros e 2 decimais")
    private BigDecimal limiteUtilizado = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "bandeira", nullable = false)
    private BandeiraCartao bandeira;
    
    @Column(name = "principal", nullable = false)
    private Boolean principal = false;
    
    @Column(name = "contactless", nullable = false)
    private Boolean contactless = false;
    
    @CreationTimestamp
    @Column(name = "data_emissao", updatable = false)
    private LocalDateTime dataEmissao;
    
    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @Column(name = "observacoes", length = 500)
    private String observacoes;
    
    @Column(name = "tentativas_erradas")
    private Integer tentativasErradas = 0;
    
    @Column(name = "data_bloqueio")
    private LocalDateTime dataBloqueio;
    
    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Clientes cliente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contratos contrato;

    // CONSTRUTORES
    public Cartoes() {
    }
    
    public Cartoes(Long id, Clientes cliente, Contratos contrato, String numeroCartao, 
                  String nomePortador, LocalDate dataValidade, String cvv, TipoCartao tipoCartao,
                  StatusCartao status, BigDecimal limite, BigDecimal limiteUtilizado, 
                  BandeiraCartao bandeira, Boolean principal, Boolean contactless,
                  LocalDateTime dataEmissao, LocalDateTime dataAtualizacao, String observacoes,
                  Integer tentativasErradas, LocalDateTime dataBloqueio) {
        this.id = id;
        this.cliente = cliente;
        this.contrato = contrato;
        this.numeroCartao = numeroCartao;
        this.nomePortador = nomePortador;
        this.dataValidade = dataValidade;
        this.cvv = cvv;
        this.tipoCartao = tipoCartao;
        this.status = status;
        this.limite = limite;
        this.limiteUtilizado = (limiteUtilizado != null) ? limiteUtilizado : BigDecimal.ZERO;
        this.bandeira = bandeira;
        this.principal = principal;
        this.contactless = contactless;
        this.dataEmissao = dataEmissao;
        this.dataAtualizacao = dataAtualizacao;
        this.observacoes = observacoes;
        this.tentativasErradas = tentativasErradas;
        this.dataBloqueio = dataBloqueio;
    }

    // GETTERS E SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Clientes getCliente() {
        return cliente;
    }

    public void setCliente(Clientes cliente) {
        this.cliente = cliente;
    }

    public Contratos getContrato() {
        return contrato;
    }

    public void setContrato(Contratos contrato) {
        this.contrato = contrato;
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public void setNumeroCartao(String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }

    public String getNomePortador() {
        return nomePortador;
    }

    public void setNomePortador(String nomePortador) {
        this.nomePortador = nomePortador;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public TipoCartao getTipoCartao() {
        return tipoCartao;
    }

    public void setTipoCartao(TipoCartao tipoCartao) {
        this.tipoCartao = tipoCartao;
    }

    public StatusCartao getStatus() {
        return status;
    }

    public void setStatus(StatusCartao status) {
        this.status = status;
    }

    public BigDecimal getLimite() {
        return limite;
    }

    public void setLimite(BigDecimal limite) {
        this.limite = limite;
    }

    public BigDecimal getLimiteUtilizado() {
        return (limiteUtilizado != null) ? limiteUtilizado : BigDecimal.ZERO;
    }

    public void setLimiteUtilizado(BigDecimal limiteUtilizado) {
        this.limiteUtilizado = (limiteUtilizado != null) ? limiteUtilizado : BigDecimal.ZERO;
    }

    public BandeiraCartao getBandeira() {
        return bandeira;
    }

    public void setBandeira(BandeiraCartao bandeira) {
        this.bandeira = bandeira;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public Boolean getContactless() {
        return contactless;
    }

    public void setContactless(Boolean contactless) {
        this.contactless = contactless;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Integer getTentativasErradas() {
        return tentativasErradas;
    }

    public void setTentativasErradas(Integer tentativasErradas) {
        this.tentativasErradas = tentativasErradas;
    }

    public LocalDateTime getDataBloqueio() {
        return dataBloqueio;
    }

    public void setDataBloqueio(LocalDateTime dataBloqueio) {
        this.dataBloqueio = dataBloqueio;
    }

    // MÉTODOS DA CLASSE BASE
    public String getNumeroCartaoMascarado() {
        if (numeroCartao == null || numeroCartao.length() != 16) {
            return "****";
        }
        return "**** **** **** " + numeroCartao.substring(12);
    }
    
    public BigDecimal getLimiteDisponivel() {
        if (limite == null) return BigDecimal.ZERO;
        return limite.subtract(getLimiteUtilizado());
    }
    
    public boolean podeUtilizarLimite(BigDecimal valor) {
        if (!isValidoParaUso()) return false;
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) return false;
        return getLimiteDisponivel().compareTo(valor) >= 0;
    }
    
    public void utilizarLimite(BigDecimal valor) {
        if (!podeUtilizarLimite(valor)) {
            throw new IllegalArgumentException("Limite insuficiente ou cartão inválido");
        }
        this.limiteUtilizado = getLimiteUtilizado().add(valor);
    }
    
    public void liberarLimite(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal novoValor = getLimiteUtilizado().subtract(valor);
        this.limiteUtilizado = (novoValor.compareTo(BigDecimal.ZERO) < 0) ? BigDecimal.ZERO : novoValor;
    }
    
    public boolean isValidoParaUso() {
        return StatusCartao.ATIVO.equals(status) && 
               dataValidade != null && 
               dataValidade.isAfter(LocalDate.now());
    }
    
    public boolean podeRealizarTransacao(BigDecimal valor) {
        return isValidoParaUso() && podeUtilizarLimite(valor);
    }
    
    public boolean podeRealizarTransacaoContactless(BigDecimal valor) {
        return Boolean.TRUE.equals(contactless) && podeRealizarTransacao(valor);
    }
    
    // MÉTODOS ABSTRATOS (para implementação pelas subclasses)
    public abstract String getTipoEspecifico();
    
    public abstract BigDecimal calcularTaxa(BigDecimal valor);
    
    public abstract String getRegrasNegocio();
    
    public abstract boolean podeUsarNoEstabelecimento(String cnaeDoEstabelecimento);
    
    // VALIDAÇÕES
    @PrePersist
    @PreUpdate
    protected void validarCartao() {
        if (numeroCartao == null || !numeroCartao.matches("\\d{16}")) {
            throw new IllegalArgumentException("Número do cartão deve ter 16 dígitos");
        }
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("CVV deve ter 3 ou 4 dígitos");
        }
        if (dataValidade == null || !dataValidade.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de validade deve ser futura");
        }
        if (limite != null && limite.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Limite não pode ser negativo");
        }
    }
    
    @Override
    public String toString() {
        return "Cartoes{" +
               "id=" + id +
               ", numeroCartao='" + getNumeroCartaoMascarado() + '\'' +
               ", status=" + status +
               '}';
    }
}