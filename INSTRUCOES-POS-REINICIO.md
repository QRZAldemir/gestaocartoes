# 📋 Instruções Pós-Reinício

## ✅ O que foi configurado:

1. **Projeto Spring Boot** configurado para PostgreSQL
2. **Dependências** atualizadas no `pom.xml` (PostgreSQL driver)
3. **Configurações** de banco criadas para diferentes ambientes
4. **Scripts** de teste e execução criados
5. **Compilação** testada e funcionando

## 🚀 Próximos passos após reiniciar:

### 1. Verificar Proxmox
```bash
# Verificar se o container PostgreSQL está rodando
lxc list
```

### 2. Testar conexão com banco
```bash
cd /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes
chmod +x *.sh
./test-db-connection.sh
```

### 3. Executar aplicação
```bash
# Opção 1: Menu interativo
./run-app.sh

# Opção 2: Direto com Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🔧 Configurações criadas:

### application.properties (padrão)
- PostgreSQL: localhost:5432
- Usuário: postgres
- Senha: postgres123

### application-prod.properties
- PostgreSQL: 192.168.1.100:5432 (Proxmox)
- Configurações otimizadas para produção

### application-dev.properties
- PostgreSQL: localhost:5432
- Configurações para desenvolvimento

## 📊 URLs importantes:

- **Aplicação**: http://localhost:8080/gestao-cartoes
- **API Docs**: http://localhost:8080/gestao-cartoes/swagger-ui.html
- **Health Check**: http://localhost:8080/gestao-cartoes/actuator/health

## 🗄️ Banco de dados:

- **Host**: localhost (ou IP do Proxmox)
- **Porta**: 5432
- **Banco**: gestao_cartoes
- **Usuário**: postgres
- **Senha**: postgres123

## ⚠️ Se houver problemas:

1. Verificar se PostgreSQL está rodando no Proxmox
2. Testar conexão: `./test-db-connection.sh`
3. Verificar logs: `tail -f logs/gestao-cartoes-*.log`
4. Verificar porta 8080: `lsof -i :8080`

## 🎯 Sistema pronto para:

- ✅ Gestão de cartões de crédito
- ✅ Gestão de cartões de débito
- ✅ Cartões de alimentação/refeição
- ✅ Cartões de combustível
- ✅ Cartões de saúde
- ✅ API REST completa
- ✅ Documentação Swagger
- ✅ Monitoramento com Actuator

**Boa sorte com o reinício! 🚀**
