package com.empresa.gestao_cartoes.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("CONVENIO")
public class CartaoConvenio extends Cartoes {

    private static final BigDecimal TAXA_ADMINISTRACAO = new BigDecimal("0.015"); // 1.5%
    private static final BigDecimal LIMITE_TRANSACAO_EDUCACAO = new BigDecimal("2000.00");
    private static final BigDecimal LIMITE_TRANSACAO_BEM_ESTAR = new BigDecimal("500.00");
    private static final BigDecimal LIMITE_TRANSACAO_AUTOMOTIVO = new BigDecimal("800.00");

    public enum TipoConvenio {
        EDUCACAO("Educação", "85"),
        BEM_ESTAR("Bem-estar", "93,96"),
        AUTOMOTIVO("Automotivo", "45"),
        CULTURA_LAZER("Cultura e Lazer", "79,90,91,92"),
        TECNOLOGIA("Tecnologia", "62,95"),
        COMERCIO("Comércio Especializado", "47.6,47.7"),
        OUTROS("Outros Serviços", "NA");

        private final String descricao;
        private final String cnaes;

        TipoConvenio(String descricao, String cnaes) {
            this.descricao = descricao;
            this.cnaes = cnaes;
        }

        public String getDescricao() { return descricao; }
        public String getCnaes() { return cnaes; }
    }

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @jakarta.persistence.Column(name = "tipo_convenio")
    private TipoConvenio tipoConvenio = TipoConvenio.OUTROS;

    public CartaoConvenio() {
        super();
    }

    public TipoConvenio getTipoConvenio() {
        return tipoConvenio;
    }

    public void setTipoConvenio(TipoConvenio tipoConvenio) {
        this.tipoConvenio = tipoConvenio;
    }

    @Override
    public String getTipoEspecifico() {
        return "Cartão Convênio - " + (tipoConvenio != null ? tipoConvenio.getDescricao() : "Benefícios Diversos");
    }

    @Override
    public BigDecimal calcularTaxa(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return valor.multiply(TAXA_ADMINISTRACAO);
    }

    @Override
    public boolean podeRealizarTransacao(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Verifica limite específico por tipo de convênio
        if (tipoConvenio != null) {
            BigDecimal limiteTransacao = getLimiteTransacaoPorTipo();
            if (valor.compareTo(limiteTransacao) > 0) {
                return false;
            }
        }
        
        return isValidoParaUso() && podeUtilizarLimite(valor);
    }
    
    @Override
    public String getRegrasNegocio() {
        return """
               Regras para Cartão Convênio:
               - Uso exclusivo em estabelecimentos conveniados
               - Taxa de administração de 1.5% por transação
               - Limites específicos por categoria de benefício
               - Validação de CNAE do estabelecimento
               - Aceitação apenas em redes credenciadas
               - Válido para diversos benefícios: educação, bem-estar, automotivo, cultura, etc.
               
               Limites por transação:
               - Educação: R$ """ + LIMITE_TRANSACAO_EDUCACAO + """
               - Bem-estar: R$ """ + LIMITE_TRANSACAO_BEM_ESTAR + """
               - Automotivo: R$ """ + LIMITE_TRANSACAO_AUTOMOTIVO + """
               - Outros: Limite disponível do cartão""";
    }
    
    @Override
    public boolean podeUsarNoEstabelecimento(String cnaeDoEstabelecimento) {
        if (cnaeDoEstabelecimento == null || cnaeDoEstabelecimento.trim().isEmpty()) {
            return false;
        }
        
        // Se não tem tipo específico, permite ampla gama de estabelecimentos
        if (tipoConvenio == null || TipoConvenio.OUTROS.equals(tipoConvenio)) {
            return isCnaePermitidoGenerico(cnaeDoEstabelecimento);
        }
        
        // Verifica CNAEs específicos do tipo de convênio
        return isCnaePermitidoPorTipo(cnaeDoEstabelecimento, tipoConvenio);
    }

    private boolean isCnaePermitidoGenerico(String cnae) {
        // Lista ampla de CNAEs permitidos para convênios genéricos
        List<String> cnaesPermitidos = Arrays.asList(
            "85.", // Educação
            "93.", // Atividades esportivas e recreativas
            "96.", // Outras atividades de serviços pessoais
            "45.", // Comércio e reparação de veículos
            "79.", // Agências de viagem e operadores turísticos
            "90.", // Atividades criativas, artísticas e espetáculos
            "91.", // Atividades ligadas ao patrimônio cultural
            "92.", // Atividades de exploração de jogos de azar
            "62.", // Atividades de tecnologia da informação
            "95.", // Reparação de equipamentos
            "47.6", // Comércio varejista de livros, jornais e papelaria
            "47.7"  // Comércio varejista de outros produtos novos
        );
        
        return cnaesPermitidos.stream().anyMatch(cnae::startsWith);
    }

    private boolean isCnaePermitidoPorTipo(String cnae, TipoConvenio tipo) {
        String[] cnaesPermitidos = tipo.getCnaes().split(",");
        return Arrays.stream(cnaesPermitidos)
                .anyMatch(permitido -> cnae.startsWith(permitido.trim()));
    }

    private BigDecimal getLimiteTransacaoPorTipo() {
        if (tipoConvenio == null) {
            return getLimiteDisponivel();
        }
        
        return switch (tipoConvenio) {
            case EDUCACAO -> LIMITE_TRANSACAO_EDUCACAO;
            case BEM_ESTAR -> LIMITE_TRANSACAO_BEM_ESTAR;
            case AUTOMOTIVO -> LIMITE_TRANSACAO_AUTOMOTIVO;
            default -> getLimiteDisponivel();
        };
    }

    public boolean isServicoPermitido(String tipoServico, String cnaeEstabelecimento) {
        if (tipoServico == null || cnaeEstabelecimento == null) {
            return false;
        }
        
        if (!podeUsarNoEstabelecimento(cnaeEstabelecimento)) {
            return false;
        }
        
        // Verifica se o tipo de serviço é compatível com o CNAE
        return isServicoCompativelComCnae(tipoServico, cnaeEstabelecimento);
    }

    private boolean isServicoCompativelComCnae(String tipoServico, String cnae) {
        // Mapeamento de serviços compatíveis por CNAE
        if (cnae.startsWith("85")) {
            return Arrays.asList("CURSO", "PÓS-GRADUAÇÃO", "WORKSHOP", "TREINAMENTO", "SEMINÁRIO")
                    .contains(tipoServico.toUpperCase());
        } else if (cnae.startsWith("93")) {
            return Arrays.asList("ACADEMIA", "NATAÇÃO", "MUSCULAÇÃO", "PILATES", "YOGA")
                    .contains(tipoServico.toUpperCase());
        } else if (cnae.startsWith("45")) {
            return Arrays.asList("REVISÃO", "TROCA_OLEO", "ALINHAMENTO", "LAVAGEM", "POLIMENTO")
                    .contains(tipoServico.toUpperCase());
        }
        
        return true; // Para outros CNAEs, assume compatibilidade
    }

    public boolean permitePagamentoRecorrente() {
        return tipoConvenio != null && 
               (TipoConvenio.EDUCACAO.equals(tipoConvenio) || 
                TipoConvenio.BEM_ESTAR.equals(tipoConvenio));
    }

    public BigDecimal calcularValorParcelaRecorrente(BigDecimal valorTotal, int parcelas) {
        if (!permitePagamentoRecorrente() || parcelas <= 0) {
            return valorTotal;
        }
        
        if (parcelas == 1) {
            return valorTotal;
        }
        
        // Para educação e bem-estar, permite parcelamento sem juros em até 6x
        int maxParcelasSemJuros = 6;
        if (parcelas <= maxParcelasSemJuros) {
            return valorTotal.divide(BigDecimal.valueOf(parcelas), 2, java.math.RoundingMode.HALF_UP);
        }
        
        // Acima de 6 parcelas, aplica juros de 1% ao mês
        double taxaJuros = 0.01;
        BigDecimal valorComJuros = valorTotal.multiply(
            BigDecimal.ONE.add(BigDecimal.valueOf(taxaJuros * (parcelas - maxParcelasSemJuros)))
        );
        
        return valorComJuros.divide(BigDecimal.valueOf(parcelas), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        
        CartaoConvenio that = (CartaoConvenio) o;
        return tipoConvenio == that.tipoConvenio;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tipoConvenio != null ? tipoConvenio.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "CartaoConvenio{" +
               "id=" + getId() +
               ", numeroCartao='" + getNumeroCartaoMascarado() + '\'' +
               ", tipoConvenio=" + tipoConvenio +
               ", tipoEspecifico='" + getTipoEspecifico() + '\'' +
               '}';
    }
}