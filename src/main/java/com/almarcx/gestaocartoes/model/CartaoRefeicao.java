package com.empresa.gestao_cartoes.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("REFEICAO")
public class CartaoRefeicao extends Cartoes { // CORREÇÃO: Extende CartaoBase

    private static final BigDecimal LIMITE_POR_TRANSACAO = new BigDecimal("150.00");
    private static final BigDecimal LIMITE_DIARIO_SUGERIDO = new BigDecimal("80.00");
    private static final BigDecimal TAXA_ADMINISTRACAO = new BigDecimal("0.018");

    public CartaoRefeicao() {
        super(); // CORREÇÃO: Chamada ao construtor pai
    }

    @Override
    public String getTipoEspecifico() {
        return "Cartão Refeição";
    }

    @Override
    public BigDecimal calcularTaxa(BigDecimal valor) {
        return BigDecimal.ZERO;
    }
    
    public BigDecimal calcularTaxaEstabelecimento(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return valor.multiply(TAXA_ADMINISTRACAO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean podeRealizarTransacao(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        return isValidoParaUso() && 
               podeUtilizarLimite(valor) && 
               valor.compareTo(LIMITE_POR_TRANSACAO) <= 0;
    }
    
    @Override
    public String getRegrasNegocio() {
        return "Regras para Cartão Refeição:\n" +
               "- Uso exclusivo para refeições em restaurantes, lanchonetes e similares\n" +
               "- Limite por transação de R$ " + LIMITE_POR_TRANSACAO + "\n" +
               "- Sugestão de limite diário de R$ " + LIMITE_DIARIO_SUGERIDO + "\n" +
               "- Taxa de 1.8% para estabelecimentos\n" +
               "- Não permite compra de produtos não alimentícios\n" +
               "- Válido apenas em estabelecimentos de alimentação";
    }
    
    @Override
    public boolean podeUsarNoEstabelecimento(String cnaeDoEstabelecimento) {
        if (cnaeDoEstabelecimento == null || cnaeDoEstabelecimento.trim().isEmpty()) {
            return false;
        }
        
        return cnaeDoEstabelecimento.startsWith("56.11-1") ||
               cnaeDoEstabelecimento.startsWith("56.12-0") ||
               cnaeDoEstabelecimento.startsWith("56.20-1") ||
               cnaeDoEstabelecimento.startsWith("56.30-8") ||
               cnaeDoEstabelecimento.startsWith("47.29-1");
    }

    public boolean isEstabelecimentoPermitido(String tipoEstabelecimento) {
        if (tipoEstabelecimento == null) return false;
        
        String[] tiposPermitidos = {
            "RESTAURANTE", "LANCHONETE", "PIZZARIA", "HAMBURGUERIA",
            "SELF_SERVICE", "BUFFET", "ROTISSERIA", "PASTELARIA"
        };
        
        for (String tipo : tiposPermitidos) {
            if (tipo.equalsIgnoreCase(tipoEstabelecimento.trim())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isDentroLimiteSugeridoDiario(BigDecimal gastoDiario) {
        return gastoDiario == null || gastoDiario.compareTo(LIMITE_DIARIO_SUGERIDO) <= 0;
    }

    public boolean permiteCobrancaServico(BigDecimal valorServico) {
        return valorServico != null && 
               valorServico.compareTo(new BigDecimal("10.00")) <= 0;
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
        return "CartaoRefeicao{" +
               "id=" + getId() + // CORREÇÃO: getId() agora disponível
               ", numeroCartao='" + getNumeroCartaoMascarado() + '\'' +
               ", tipoEspecifico='" + getTipoEspecifico() + '\'' +
               '}';
    }
}