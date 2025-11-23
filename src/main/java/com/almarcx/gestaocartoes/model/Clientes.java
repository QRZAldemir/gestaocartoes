package com.empresa.gestao_cartoes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_cliente", discriminatorType = DiscriminatorType.STRING)
public abstract class Clientes extends Pessoas {

    @Column(name = "codigo_cliente", length = 20, unique = true)
    private String codigoCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 20)
    private CategoriaCliente categoria = CategoriaCliente.STANDARD;

    @Column(name = "limite_credito", precision = 15, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "limite_credito_utilizado", precision = 15, scale = 2)
    private BigDecimal limiteCreditoUtilizado = BigDecimal.ZERO;

    @Column(name = "renda_mensal", precision = 15, scale = 2)
    private BigDecimal rendaMensal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_credito", length = 30)
    private StatusCredito statusCredito = StatusCredito.PENDENTE_ANALISE;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "data_ultima_analise_credito")
    private LocalDateTime dataUltimaAnaliseCredito;

    @Column(name = "data_inicio_relacionamento", nullable = false)
    private LocalDateTime dataInicioRelacionamento = LocalDateTime.now();

    @Column(name = "tempo_relacionamento_meses")
    private Integer tempoRelacionamentoMeses = 0;

    @Column(name = "score_credito")
    private Integer scoreCredito;

    @Enumerated(EnumType.STRING)
    @Column(name = "classificacao_risco", length = 20)
    private ClassificacaoRisco classificacaoRisco = ClassificacaoRisco.NAO_CLASSIFICADO;

    // --- Campos para controle de cartões adicionais ---
    @Column(name = "solicitou_cartao_adicional")
    private Boolean solicitouCartaoAdicional = false;

    @Column(name = "quantidade_cartoes_adicionais")
    private Integer quantidadeCartoesAdicionais = 0;

    @Column(name = "data_ultima_solicitacao_adicional")
    private LocalDateTime dataUltimaSolicitacaoAdicional;

    @Column(name = "tipo_beneficiario_adicional", length = 50)
    private String tipoBeneficiarioAdicional; // Ex: "Esposa", "Filho", "Marido", etc.

    @Column(name = "nome_beneficiario_adicional", length = 100)
    private String nomeBeneficiarioAdicional;

    @Column(name = "limite_solicitado_adicional", precision = 15, scale = 2)
    private BigDecimal limiteSolicitadoAdicional = BigDecimal.ZERO;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Contratos> contratos = new ArrayList<>();

    // --- Enums ---
    public enum CategoriaCliente {
        STANDARD("Standard", BigDecimal.valueOf(1000)),
        GOLD("Gold", BigDecimal.valueOf(5000)),
        PLATINUM("Platinum", BigDecimal.valueOf(15000)),
        BLACK("Black", BigDecimal.valueOf(50000)),
        CORPORATE("Corporate", BigDecimal.valueOf(100000));

        private final String descricao;
        private final BigDecimal limitePadrao;

        CategoriaCliente(String descricao, BigDecimal limitePadrao) {
            this.descricao = descricao;
            this.limitePadrao = limitePadrao;
        }

        public String getDescricao() { return descricao; }
        public BigDecimal getLimitePadrao() { return limitePadrao; }
    }

    public enum StatusCredito {
        PENDENTE_ANALISE("Pendente de Análise", false),
        APROVADO("Aprovado", true),
        REPROVADO("Reprovado", false),
        SUSPENSO("Suspenso", false),
        CANCELADO("Cancelado", false),
        EM_REVISAO("Em Revisão", false);

        private final String descricao;
        private final boolean podeUsarCredito;

        StatusCredito(String descricao, boolean podeUsarCredito) {
            this.descricao = descricao;
            this.podeUsarCredito = podeUsarCredito;
        }

        public String getDescricao() { return descricao; }
        public boolean isPodeUsarCredito() { return podeUsarCredito; }
    }

    public enum ClassificacaoRisco {
        NAO_CLASSIFICADO("Não Classificado", 0),
        BAIXO("Baixo Risco", 1),
        MEDIO("Médio Risco", 2),
        ALTO("Alto Risco", 3),
        MUITO_ALTO("Muito Alto Risco", 4);

        private final String descricao;
        private final int nivel;

        ClassificacaoRisco(String descricao, int nivel) {
            this.descricao = descricao;
            this.nivel = nivel;
        }

        public String getDescricao() { return descricao; }
        public int getNivel() { return nivel; }
    }

    // --- Construtores ---
    public Clientes() {
        super();
    }

    public Clientes(String codigoCliente) {
        this();
        this.codigoCliente = codigoCliente;
    }

    // --- Implementação do método abstrato ---
    @Override
    public String getTipoPessoa() {
        return "Cliente";
    }

    // --- Métodos de Negócio ---
    public BigDecimal getLimiteDisponivel() {
        return limiteCredito.subtract(limiteCreditoUtilizado);
    }

    public BigDecimal getPercentualUtilizacaoLimite() {
        if (limiteCredito.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return limiteCreditoUtilizado
                .divide(limiteCredito, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public boolean podeUsarCredito() {
        return isAtiva() && 
               statusCredito != null &&
               statusCredito.isPodeUsarCredito() &&
               getLimiteDisponivel().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean temLimiteSuficiente(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) return false;
        return getLimiteDisponivel().compareTo(valor) >= 0;
    }

    public boolean utilizarLimite(BigDecimal valor) {
        if (!podeUsarCredito() || !temLimiteSuficiente(valor)) return false;
        this.limiteCreditoUtilizado = this.limiteCreditoUtilizado.add(valor);
        return true;
    }

    public void liberarLimite(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) return;
        this.limiteCreditoUtilizado = this.limiteCreditoUtilizado.subtract(valor);
        if (this.limiteCreditoUtilizado.compareTo(BigDecimal.ZERO) < 0) {
            this.limiteCreditoUtilizado = BigDecimal.ZERO;
        }
    }

    private void atualizarClassificacaoRisco() {
        if (scoreCredito == null) {
            this.classificacaoRisco = ClassificacaoRisco.NAO_CLASSIFICADO;
        } else if (scoreCredito >= 800) {
            this.classificacaoRisco = ClassificacaoRisco.BAIXO;
        } else if (scoreCredito >= 600) {
            this.classificacaoRisco = ClassificacaoRisco.MEDIO;
        } else if (scoreCredito >= 400) {
            this.classificacaoRisco = ClassificacaoRisco.ALTO;
        } else {
            this.classificacaoRisco = ClassificacaoRisco.MUITO_ALTO;
        }
    }

    public void realizarAnaliseCredito(Integer novoScore, BigDecimal novoLimite) {
        this.scoreCredito = novoScore;
        this.atualizarClassificacaoRisco();
        this.dataUltimaAnaliseCredito = LocalDateTime.now();

        if (novoScore != null && novoScore >= 400) {
            this.statusCredito = StatusCredito.APROVADO;
            this.limiteCredito = novoLimite != null ? novoLimite : this.categoria.getLimitePadrao();
        } else {
            this.statusCredito = StatusCredito.REPROVADO;
            this.limiteCredito = BigDecimal.ZERO;
        }
    }

    public void atualizarCategoria(CategoriaCliente novaCategoria) {
        if (novaCategoria == null) throw new IllegalArgumentException("Categoria não pode ser nula");
        this.categoria = novaCategoria;
        if (this.limiteCredito.compareTo(novaCategoria.getLimitePadrao()) < 0) {
            this.limiteCredito = novaCategoria.getLimitePadrao();
        }
    }

    public void adicionarContrato(Contratos contrato) {
        this.contratos.add(contrato);
        contrato.setCliente(this);
    }

    public void removerContrato(Contratos contrato) {
        this.contratos.remove(contrato);
        contrato.setCliente(null);
    }

    public void calcularTempoRelacionamento() {
        if (dataInicioRelacionamento != null) {
            LocalDateTime agora = LocalDateTime.now();
            this.tempoRelacionamentoMeses = (int) java.time.temporal.ChronoUnit.MONTHS
                    .between(dataInicioRelacionamento, agora);
        }
    }

    public boolean isClienteAntigo() {
        calcularTempoRelacionamento();
        return tempoRelacionamentoMeses >= 12;
    }

    // --- Métodos para gerenciar solicitação de cartões adicionais ---
    public void solicitarCartaoAdicional(String tipoBeneficiario, String nomeBeneficiario, BigDecimal limiteSolicitado) {
        if (tipoBeneficiario == null || tipoBeneficiario.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de beneficiário é obrigatório");
        }
        if (nomeBeneficiario == null || nomeBeneficiario.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do beneficiário é obrigatório");
        }
        if (limiteSolicitado == null || limiteSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Limite solicitado deve ser maior que zero");
        }
        
        this.solicitouCartaoAdicional = true;
        this.tipoBeneficiarioAdicional = tipoBeneficiario;
        this.nomeBeneficiarioAdicional = nomeBeneficiario;
        this.limiteSolicitadoAdicional = limiteSolicitado;
        this.dataUltimaSolicitacaoAdicional = LocalDateTime.now();
    }
    
    public void cancelarSolicitacaoCartaoAdicional() {
        this.solicitouCartaoAdicional = false;
        this.tipoBeneficiarioAdicional = null;
        this.nomeBeneficiarioAdicional = null;
        this.limiteSolicitadoAdicional = BigDecimal.ZERO;
        this.dataUltimaSolicitacaoAdicional = null;
    }
    
    public void aprovarSolicitacaoCartaoAdicional() {
        // A criação real do cartão será feita na classe Cartoes
        this.quantidadeCartoesAdicionais++;
        this.solicitouCartaoAdicional = false;
        // Mantém as informações para referência
    }
    
    public void rejeitarSolicitacaoCartaoAdicional(String motivo) {
        this.solicitouCartaoAdicional = false;
        if (motivo != null && !motivo.trim().isEmpty()) {
            this.observacoes = (this.observacoes != null ? this.observacoes + " | " : "") + 
                    "Solicitação cartão adicional rejeitada: " + motivo + " - Beneficiário: " + 
                    nomeBeneficiarioAdicional;
        }
    }
    
    public boolean podeSolicitarCartaoAdicional() {
        return isAtiva() && 
               statusCredito != null &&
               statusCredito.isPodeUsarCredito() &&
               !solicitouCartaoAdicional &&
               isClienteAntigo() &&
               quantidadeCartoesAdicionais < 3; // Máximo de 3 cartões adicionais
    }
    
    public boolean atingiuLimiteCartoesAdicionais() {
        return quantidadeCartoesAdicionais >= 3;
    }
    
    public String gerarRelatorioResumido() {
        return """
            === RELATÓRIO DO CLIENTE ===
            Código: %s
            Nome: %s
            Documento: %s
            Categoria: %s
            Status: %s
            Status Crédito: %s
            Limite: R$ %.2f
            Utilizado: R$ %.2f
            Disponível: R$ %.2f
            Score: %d
            Risco: %s
            Meses: %d
            Cartões Adicionais: %d
            Solicitação Adicional: %s
            Beneficiário: %s
            Limite Solicitado: R$ %.2f
            Contratos: %d
            """.formatted(
                codigoCliente,
                getNomeCompletoOuRazaoSocial(),
                getDocumentoPrincipal(),
                categoria.getDescricao(),
                getStatus().getDescricao(),
                statusCredito.getDescricao(),
                limiteCredito,
                limiteCreditoUtilizado,
                getLimiteDisponivel(),
                scoreCredito != null ? scoreCredito : 0,
                classificacaoRisco.getDescricao(),
                tempoRelacionamentoMeses,
                quantidadeCartoesAdicionais,
                solicitouCartaoAdicional ? "Pendente - " + tipoBeneficiarioAdicional + ": " + nomeBeneficiarioAdicional : "Nenhuma",
                solicitouCartaoAdicional ? nomeBeneficiarioAdicional : "N/A",
                solicitouCartaoAdicional ? limiteSolicitadoAdicional : BigDecimal.ZERO,
                contratos.size()
        );
    }

    // --- Getters e Setters ---
    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }
    public CategoriaCliente getCategoria() { return categoria; }
    public void setCategoria(CategoriaCliente categoria) { this.categoria = categoria; }
    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal limiteCredito) { this.limiteCredito = limiteCredito; }
    public BigDecimal getLimiteCreditoUtilizado() { return limiteCreditoUtilizado; }
    public void setLimiteCreditoUtilizado(BigDecimal limiteCreditoUtilizado) { this.limiteCreditoUtilizado = limiteCreditoUtilizado; }
    public BigDecimal getRendaMensal() { return rendaMensal; }
    public void setRendaMensal(BigDecimal rendaMensal) { this.rendaMensal = rendaMensal; }
    public StatusCredito getStatusCredito() { return statusCredito; }
    public void setStatusCredito(StatusCredito statusCredito) { this.statusCredito = statusCredito; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public LocalDateTime getDataUltimaAnaliseCredito() { return dataUltimaAnaliseCredito; }
    public void setDataUltimaAnaliseCredito(LocalDateTime dataUltimaAnaliseCredito) { this.dataUltimaAnaliseCredito = dataUltimaAnaliseCredito; }
    public LocalDateTime getDataInicioRelacionamento() { return dataInicioRelacionamento; }
    public void setDataInicioRelacionamento(LocalDateTime dataInicioRelacionamento) { this.dataInicioRelacionamento = dataInicioRelacionamento; }
    public Integer getTempoRelacionamentoMeses() { return tempoRelacionamentoMeses; }
    public void setTempoRelacionamentoMeses(Integer tempoRelacionamentoMeses) { this.tempoRelacionamentoMeses = tempoRelacionamentoMeses; }
    public Integer getScoreCredito() { return scoreCredito; }
    public void setScoreCredito(Integer scoreCredito) { this.scoreCredito = scoreCredito; }
    public ClassificacaoRisco getClassificacaoRisco() { return classificacaoRisco; }
    public void setClassificacaoRisco(ClassificacaoRisco classificacaoRisco) { this.classificacaoRisco = classificacaoRisco; }
    public Boolean getSolicitouCartaoAdicional() { return solicitouCartaoAdicional; }
    public void setSolicitouCartaoAdicional(Boolean solicitouCartaoAdicional) { this.solicitouCartaoAdicional = solicitouCartaoAdicional; }
    public Integer getQuantidadeCartoesAdicionais() { return quantidadeCartoesAdicionais; }
    public void setQuantidadeCartoesAdicionais(Integer quantidadeCartoesAdicionais) { this.quantidadeCartoesAdicionais = quantidadeCartoesAdicionais; }
    public LocalDateTime getDataUltimaSolicitacaoAdicional() { return dataUltimaSolicitacaoAdicional; }
    public void setDataUltimaSolicitacaoAdicional(LocalDateTime dataUltimaSolicitacaoAdicional) { this.dataUltimaSolicitacaoAdicional = dataUltimaSolicitacaoAdicional; }
    public String getTipoBeneficiarioAdicional() { return tipoBeneficiarioAdicional; }
    public void setTipoBeneficiarioAdicional(String tipoBeneficiarioAdicional) { this.tipoBeneficiarioAdicional = tipoBeneficiarioAdicional; }
    public String getNomeBeneficiarioAdicional() { return nomeBeneficiarioAdicional; }
    public void setNomeBeneficiarioAdicional(String nomeBeneficiarioAdicional) { this.nomeBeneficiarioAdicional = nomeBeneficiarioAdicional; }
    public BigDecimal getLimiteSolicitadoAdicional() { return limiteSolicitadoAdicional; }
    public void setLimiteSolicitadoAdicional(BigDecimal limiteSolicitadoAdicional) { this.limiteSolicitadoAdicional = limiteSolicitadoAdicional; }
    public List<Contratos> getContratos() { return contratos; }
    public void setContratos(List<Contratos> contratos) { this.contratos = contratos; }
}