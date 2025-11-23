package com.empresa.gestao_cartoes.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("DEBITO")
public class CartaoDebito extends Cartoes { // CORREÇÃO: Extende CartaoBase

    private static final BigDecimal TAXA_TRANSACAO = new BigDecimal("0.019");

    public CartaoDebito() {
        super(); // CORREÇÃO: Chamada ao construtor pai
    }

    @Override
    public String getTipoEspecifico() {
        return "Cartão de Débito";
    }

    @Override
    public BigDecimal calcularTaxa(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        return valor.multiply(TAXA_TRANSACAO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean podeRealizarTransacao(BigDecimal valor) {
        return isValidoParaUso() && podeUtilizarLimite(valor);
    }
    
    @Override
    public String getRegrasNegocio() {
        return """
               Regras para Cartão de Débito:
               - Utiliza o saldo disponível na conta vinculada
               - Taxa de 1.9% por transação
               - Não permite transações que ultrapassem o saldo
               - Não possui limite adicional além do saldo da conta
               - Aceito na maioria dos estabelecimentos""";
    }
    
    @Override
    public boolean podeUsarNoEstabelecimento(String cnaeDoEstabelecimento) {
        return true;
    }

    public boolean possuiSaldoSuficiente(BigDecimal valor) {
        return podeUtilizarLimite(valor);
    }

    public boolean simularTransacaoDebito(BigDecimal valor) {
        return possuiSaldoSuficiente(valor) && isValidoParaUso();
    }
    
    public boolean permiteSaque() {
        return true;
    }
    
    public BigDecimal getLimiteSaqueDiario() {
        return new BigDecimal("1000.00");
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
        return "CartaoDebito{" +
               "id=" + getId() + // CORREÇÃO: getId() agora disponível
               ", numeroCartao='" + getNumeroCartaoMascarado() + '\'' +
               ", tipoEspecifico='" + getTipoEspecifico() + '\'' +
               '}';
    }
}