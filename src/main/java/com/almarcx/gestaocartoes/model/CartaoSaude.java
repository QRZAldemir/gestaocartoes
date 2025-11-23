package com.empresa.gestao_cartoes.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
@Entity
@DiscriminatorValue("SAUDE")
public class CartaoSaude extends Cartoes { // CORREÇÃO: Extende CartaoBase

    public CartaoSaude() {
        super(); // CORREÇÃO: Chamada ao construtor pai
    }

    @Override
    public String getTipoEspecifico() {
        return "Cartão Saúde";
    }

    @Override
    public BigDecimal calcularTaxa(BigDecimal valor) {
        return BigDecimal.ZERO;
    }

    @Override
    public boolean podeRealizarTransacao(BigDecimal valor) {
        return isValidoParaUso() && podeUtilizarLimite(valor);
    }
    
    @Override
    public String getRegrasNegocio() {
        return """
               Regras para Cartão Saúde:
               - Uso exclusivo para despesas médicas e de saúde
               - Sem taxas para o portador
               - Limite específico para cada tipo de procedimento
               - Validação de CNAE do estabelecimento
               - Aceitação apenas em estabelecimentos de saúde credenciados
               - Válido para consultas, exames, medicamentos e procedimentos""";
    }
    
    @Override
    public boolean podeUsarNoEstabelecimento(String cnaeDoEstabelecimento) {
        if (cnaeDoEstabelecimento == null || cnaeDoEstabelecimento.trim().isEmpty()) {
            return false;
        }
        
        return cnaeDoEstabelecimento.startsWith("86.10-1") ||
               cnaeDoEstabelecimento.startsWith("47.71-7") ||
               cnaeDoEstabelecimento.startsWith("86.30-1") ||
               cnaeDoEstabelecimento.startsWith("86.90-3") ||
               cnaeDoEstabelecimento.startsWith("86.20-0") ||
               cnaeDoEstabelecimento.startsWith("86.40-8") ||
               cnaeDoEstabelecimento.startsWith("86.50-5");
    }

    public boolean isProcedimentoCoberto(String codigoProcedimento) {
        if (codigoProcedimento == null || codigoProcedimento.trim().isEmpty()) {
            return false;
        }
        
        String[] procedimentosCobertos = {
            "CONSULTA", "EXAME", "CIRURGIA", "FISIOTERAPIA", 
            "PSICOLOGIA", "ODONTOLOGIA", "PRONTO SOCORRO",
            "ULTRASSON", "TOMOGRAFIA", "RESSONANCIA", "ANALISE_LABORATORIAL"
        };
        
        for (String procedimento : procedimentosCobertos) {
            if (procedimento.equalsIgnoreCase(codigoProcedimento.trim())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isMedicamentoCoberto(String codigoMedicamento) {
        return true;
    }

    public boolean cobreExameLaboratorial() {
        return true;
    }

    public boolean cobreConsultaEspecialista() {
        return true;
    }

    public boolean cobreProcedimentoCirurgico() {
        return true;
    }
    
    public boolean cobreEspecialidade(String especialidade) {
        if (especialidade == null) return false;
        
        String[] especialidadesCobertas = {
            "CLINICA_GERAL", "PEDIATRIA", "CARDIOLOGIA", "DERMATOLOGIA",
            "ORTOPEDIA", "GINECOLOGIA", "UROLOGIA", "OFTALMOLOGIA",
            "NEUROLOGIA", "ENDOCRINOLOGIA", "GASTROENTEROLOGIA"
        };
        
        for (String esp : especialidadesCobertas) {
            if (esp.equalsIgnoreCase(especialidade.trim())) {
                return true;
            }
        }
        return false;
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
        return "CartaoSaude{" +
               "id=" + getId() + // CORREÇÃO: getId() agora disponível
               ", numeroCartao='" + getNumeroCartaoMascarado() + '\'' +
               ", tipoEspecifico='" + getTipoEspecifico() + '\'' +
               '}';
    }
}