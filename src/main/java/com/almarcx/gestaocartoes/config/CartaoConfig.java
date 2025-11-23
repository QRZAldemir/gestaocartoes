package com.empresa.gestao_cartoes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Period;
import java.util.Map;

/**
 * Classe de configuração para definição de regras, limites e políticas
 * relacionadas ao gerenciamento de cartões no sistema.
 */
@Configuration
@ConfigurationProperties(prefix = "cartao") // Corrigido com importação
public class CartaoConfig {

    // Valores padrão otimizados
    private BigDecimal limitePadrao = BigDecimal.valueOf(1000.00);
    private BigDecimal limiteMaximo = BigDecimal.valueOf(50000.00);
    private BigDecimal valorMinimoTransacao = BigDecimal.valueOf(0.01);
    private Period validadePeriodo = Period.ofYears(5);
    private boolean contactlessHabilitado = true;
    
    private Map<String, BigDecimal> taxasPadrao = Map.of(
        "CREDITO", BigDecimal.valueOf(0.029),
        "DEBITO", BigDecimal.valueOf(0.019),
        "ALIMENTACAO", BigDecimal.ZERO,
        "REFEICAO", BigDecimal.ZERO,
        "COMBUSTIVEL", BigDecimal.valueOf(0.025),
        "CONVENIO", BigDecimal.ZERO
    );
    
    private BigDecimal taxaFixaCredito = BigDecimal.valueOf(0.30);
    private BigDecimal limiteDiarioRefeicao = BigDecimal.valueOf(80.00);
    private BigDecimal maxValorTransacaoRefeicao = BigDecimal.valueOf(150.00);
    private int maxCartoesPorCliente = 5;
    private boolean bloqueioAutomaticoSuspeita = true;
    private int maxTentativasPin = 3;

    // Getters e Setters (mantidos)
    public BigDecimal getLimitePadrao() {
        return limitePadrao;
    }

    public void setLimitePadrao(BigDecimal limitePadrao) {
        this.limitePadrao = limitePadrao;
    }

	public BigDecimal getLimiteMaximo() {
		return limiteMaximo;
	}

	public void setLimiteMaximo(BigDecimal limiteMaximo) {
		this.limiteMaximo = limiteMaximo;
	}

	public Period getValidadePeriodo() {
		return validadePeriodo;
	}

	public void setValidadePeriodo(Period validadePeriodo) {
		this.validadePeriodo = validadePeriodo;
	}

	public BigDecimal getValorMinimoTransacao() {
		return valorMinimoTransacao;
	}

	public void setValorMinimoTransacao(BigDecimal valorMinimoTransacao) {
		this.valorMinimoTransacao = valorMinimoTransacao;
	}

	public boolean isContactlessHabilitado() {
		return contactlessHabilitado;
	}

	public void setContactlessHabilitado(boolean contactlessHabilitado) {
		this.contactlessHabilitado = contactlessHabilitado;
	}

	public Map<String, BigDecimal> getTaxasPadrao() {
		return taxasPadrao;
	}

	public void setTaxasPadrao(Map<String, BigDecimal> taxasPadrao) {
		this.taxasPadrao = taxasPadrao;
	}

	public BigDecimal getTaxaFixaCredito() {
		return taxaFixaCredito;
	}

	public void setTaxaFixaCredito(BigDecimal taxaFixaCredito) {
		this.taxaFixaCredito = taxaFixaCredito;
	}

	public BigDecimal getLimiteDiarioRefeicao() {
		return limiteDiarioRefeicao;
	}

	public void setLimiteDiarioRefeicao(BigDecimal limiteDiarioRefeicao) {
		this.limiteDiarioRefeicao = limiteDiarioRefeicao;
	}

	public BigDecimal getMaxValorTransacaoRefeicao() {
		return maxValorTransacaoRefeicao;
	}

	public void setMaxValorTransacaoRefeicao(BigDecimal maxValorTransacaoRefeicao) {
		this.maxValorTransacaoRefeicao = maxValorTransacaoRefeicao;
	}

	public int getMaxCartoesPorCliente() {
		return maxCartoesPorCliente;
	}

	public void setMaxCartoesPorCliente(int maxCartoesPorCliente) {
		this.maxCartoesPorCliente = maxCartoesPorCliente;
	}

	public boolean isBloqueioAutomaticoSuspeita() {
		return bloqueioAutomaticoSuspeita;
	}

	public void setBloqueioAutomaticoSuspeita(boolean bloqueioAutomaticoSuspeita) {
		this.bloqueioAutomaticoSuspeita = bloqueioAutomaticoSuspeita;
	}

	public int getMaxTentativasPin() {
		return maxTentativasPin;
	}

	public void setMaxTentativasPin(int maxTentativasPin) {
		this.maxTentativasPin = maxTentativasPin;
	}

    // ... (outros getters e setters permanecem iguais)
}