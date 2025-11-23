package com.empresa.gestao_cartoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Classe principal da aplicação Spring Boot
 * Sistema completo de gestão de cartões (crédito, débito, alimentação, refeição, combustível)
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableTransactionManagement
public class GestaoCartoesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestaoCartoesApplication.class, args);
        System.out.println("🚀 Sistema de Gestão de Cartões iniciado com sucesso!");
        System.out.println("💳 Tipos de cartões suportados:");
        System.out.println("   • Cartão de Crédito");
        System.out.println("   • Cartão de Débito");
        System.out.println("   • Cartão Alimentação");
        System.out.println("   • Cartão Refeição");
        System.out.println("   • Cartão Combustível");
        System.out.println("🌐 Aplicação: http://localhost:8080/api");
        System.out.println("💊 Health Check: http://localhost:8080/api/actuator/health");
        System.out.println("🗄️  Banco PostgreSQL: Proxmox Container");
    }
}