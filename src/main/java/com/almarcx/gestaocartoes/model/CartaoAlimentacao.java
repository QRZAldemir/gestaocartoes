package com.empresa.gestao_cartoes.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("ALIMENTACAO")
public class CartaoAlimentacao extends Cartoes { // CORREÇÃO: Extende CartaoBase

    private static final BigDecimal LIMITE_DIARIO = new BigDecimal("100.00");
    private static final BigDecimal LIMITE_POR_TRANSACAO = new BigDecimal("50.00");
    private static final BigDecimal TAXA_ADMINISTRACAO = new BigDecimal("0.02");

    public CartaoAlimentacao() {
        super(); // CORREÇÃO: Chamada ao construtor pai
    }

    @Override
    public String getTipoEspecifico() {
        return "Cartão Alimentação";
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
        return "Regras para Cartão Alimentação:\n" +
               "- Uso exclusivo para compra de gêneros alimentícios\n" +
               "- Válido em supermercados, mercados, mercearias e hortifrutis\n" +
               "- Limite diário de R$ " + LIMITE_DIARIO + "\n" +
               "- Limite por transação de R$ " + LIMITE_POR_TRANSACAO + "\n" +
               "- Taxa de 2% para estabelecimentos\n" +
               "- Não permite compra de produtos não alimentícios";
    }
    
    @Override
    public boolean podeUsarNoEstabelecimento(String cnaeDoEstabelecimento) {
        if (cnaeDoEstabelecimento == null || cnaeDoEstabelecimento.trim().isEmpty()) {
            return false;
        }
        
        return cnaeDoEstabelecimento.startsWith("47.11") ||
               cnaeDoEstabelecimento.startsWith("47.29") ||
               cnaeDoEstabelecimento.startsWith("47.21") ||
               cnaeDoEstabelecimento.startsWith("46.31");
    }

    public boolean isProdutoPermitido(String categoriaProduto) {
        if (categoriaProduto == null) return false;
        
        String[] categoriasPermitidas = {
            "ALIMENTO", "BEBIDA", "HORTIFRUTI", "PADARIA", "LATICINIO", 
            "AÇOUGUE", "FRIOS", "CONGELADO"
        };
        
        for (String categoria : categoriasPermitidas) {
            if (categoria.equalsIgnoreCase(categoriaProduto.trim())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isDentroLimiteDiario(BigDecimal gastoDiario) {
        return gastoDiario == null || gastoDiario.compareTo(LIMITE_DIARIO) <= 0;
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
        return "CartaoAlimentacao{" +
               "id=" + getId() + // CORREÇÃO: getId() agora disponível
               ", numeroCartao='" + getNumeroCartaoMascarado() + '\'' +
               ", tipoEspecifico='" + getTipoEspecifico() + '\'' +
               '}';
    }
}