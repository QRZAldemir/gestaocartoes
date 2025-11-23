package com.empresa.gestao_cartoes.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("COMBUSTIVEL")
public class CartaoCombustivel extends Cartoes { // CORREÇÃO: Extende CartaoBase

    private static final BigDecimal TAXA_ADMINISTRACAO = new BigDecimal("0.01");
    private static final String[] TIPOS_COMBUSTIVEL = {
        "GASOLINA", "ETANOL", "DIESEL", "GNV", "ADITIVADA"
    };

    public CartaoCombustivel() {
        super(); // CORREÇÃO: Chamada ao construtor pai
    }

    @Override
    public String getTipoEspecifico() {
        return "Cartão Combustível";
    }

    @Override
    public BigDecimal calcularTaxa(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        return valor.multiply(TAXA_ADMINISTRACAO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean podeRealizarTransacao(BigDecimal valor) {
        return isValidoParaUso() && podeUtilizarLimite(valor);
    }
    
    @Override
    public String getRegrasNegocio() {
        return """
               Regras para Cartão Combustível:
               - Uso exclusivo em postos de combustível credenciados
               - Taxa de administração de 1% por transação
               - Limite específico para abastecimentos
               - Validação de CNAE do estabelecimento
               - Aceitação apenas em redes de postos credenciados
               - Válido apenas para combustíveis (gasolina, etanol, diesel, GNV)""";
    }
    
    @Override
    public boolean podeUsarNoEstabelecimento(String cnaeDoEstabelecimento) {
        if (cnaeDoEstabelecimento == null || cnaeDoEstabelecimento.trim().isEmpty()) {
            return false;
        }
        
        return cnaeDoEstabelecimento.startsWith("47.31-8") || 
               cnaeDoEstabelecimento.equals("4731-8/00") ||
               cnaeDoEstabelecimento.startsWith("47.31");
    }

    public boolean isTipoCombustivelPermitido(String tipoCombustivel) {
        if (tipoCombustivel == null || tipoCombustivel.trim().isEmpty()) {
            return false;
        }
        
        for (String tipo : TIPOS_COMBUSTIVEL) {
            if (tipo.equalsIgnoreCase(tipoCombustivel.trim())) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal getLimiteMaximoPorAbastecimento() {
        return getLimiteDisponivel();
    }
    
    public boolean isAbastecimentoPermitido(BigDecimal valor, String tipoCombustivel) {
        return isValidoParaUso() && 
               isTipoCombustivelPermitido(tipoCombustivel) && 
               valor.compareTo(getLimiteMaximoPorAbastecimento()) <= 0;
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
        return "CartaoCombustivel{" +
               "id=" + getId() + // CORREÇÃO: getId() agora disponível
               ", numeroCartao='" + getNumeroCartaoMascarado() + '\'' +
               ", tipoEspecifico='" + getTipoEspecifico() + '\'' +
               '}';
    }
}