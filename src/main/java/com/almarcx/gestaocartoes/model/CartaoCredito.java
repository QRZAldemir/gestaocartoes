package com.empresa.gestao_cartoes.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("CREDITO")
public class CartaoCredito extends Cartoes {

    private static final BigDecimal TAXA_PERCENTUAL = new BigDecimal("0.029");
    private static final BigDecimal TAXA_FIXA = new BigDecimal("0.30");
    private static final BigDecimal VALOR_MINIMO_FATURA = new BigDecimal("50.00");
    private static final BigDecimal PERCENTUAL_MINIMO_FATURA = new BigDecimal("0.15");

    public CartaoCredito() {
        super();
    }

    @Override
    public String getTipoEspecifico() {
        return "Cartão de Crédito";
    }

    @Override
    public BigDecimal calcularTaxa(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal taxaPercentual = valor.multiply(TAXA_PERCENTUAL);
        return taxaPercentual.add(TAXA_FIXA).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getRegrasNegocio() {
        return """
               Regras para Cartão de Crédito:
               - Limite de crédito pré-aprovado
               - Pagamento mínimo de 15% da fatura ou R$ 50,00, o que for maior
               - Juros de 2.9% + R$ 0,30 per transação
               - Possibilidade de parcelamento em até 12x
               - Anuidade isenta para gastos acima de R$ 500,00/mês
               - Aceito na maioria dos estabelecimentos""";
    }
    
    @Override
    public boolean podeUsarNoEstabelecimento(String cnaeDoEstabelecimento) {
        // Cartão de crédito é aceito em praticamente todos os estabelecimentos
        return true;
    }

    public BigDecimal calcularValorMinimoFatura() {
        BigDecimal limiteUtilizado = getLimiteUtilizado();
        if (limiteUtilizado == null || limiteUtilizado.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal minimoPercentual = limiteUtilizado.multiply(PERCENTUAL_MINIMO_FATURA);
        return minimoPercentual.max(VALOR_MINIMO_FATURA).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularValorTotalFatura(BigDecimal valorFatura, int diasAtraso) {
        if (valorFatura == null || valorFatura.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (diasAtraso <= 0) {
            return valorFatura;
        }
        
        BigDecimal jurosMensal = new BigDecimal("0.01");
        BigDecimal multaAtraso = new BigDecimal("0.02");
        
        BigDecimal jurosProporcional = jurosMensal
            .divide(new BigDecimal("30"), 10, RoundingMode.HALF_UP)
            .multiply(new BigDecimal(diasAtraso));
        
        BigDecimal valorComMulta = valorFatura.multiply(BigDecimal.ONE.add(multaAtraso));
        return valorComMulta.multiply(BigDecimal.ONE.add(jurosProporcional))
                           .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal simularParcelamento(BigDecimal valor, int parcelas) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0 || parcelas <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (parcelas == 1) {
            return valor.setScale(2, RoundingMode.HALF_UP);
        }
        
        double juros = 0.02;
        BigDecimal valorComJuros = valor.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(juros * parcelas)));
        
        return valorComJuros.divide(new BigDecimal(parcelas), 2, RoundingMode.HALF_UP);
    }

    public boolean temIsencaoAnuidade() {
        BigDecimal limiteUtilizado = getLimiteUtilizado();
        return limiteUtilizado != null && 
               limiteUtilizado.compareTo(new BigDecimal("500.00")) >= 0;
    }
    
    public boolean permiteTransacaoInternacional() {
        BandeiraCartao bandeira = getBandeira();
        return BandeiraCartao.VISA.equals(bandeira) ||
               BandeiraCartao.MASTERCARD.equals(bandeira) ||
               BandeiraCartao.AMERICAN_EXPRESS.equals(bandeira);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public String toString() {
        return "CartaoCredito{" +
               "id=" + getId() +
               ", numeroCartao='" + getNumeroCartaoMascarado() + '\'' +
               ", tipoEspecifico='" + getTipoEspecifico() + '\'' +
               '}';
    }
    
    
}