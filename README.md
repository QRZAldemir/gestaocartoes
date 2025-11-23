# 💳 Sistema de Gestão de Cartões

Sistema completo de gestão de cartões desenvolvido em Spring Boot com Java 17, conectando a um banco PostgreSQL no container Proxmox.

## 🚀 Características

- **Backend**: Spring Boot 3.5.6 com Java 17
- **Banco de Dados**: PostgreSQL 15
- **Arquitetura**: REST API com JPA/Hibernate
- **Segurança**: Spring Security
- **Documentação**: Swagger/OpenAPI
- **Tipos de Cartão**: Crédito, Débito, Alimentação, Refeição, Combustível, Saúde

## 📋 Tipos de Cartões Suportados

- 💳 **Cartão de Crédito**: Gestão de limite, faturas, parcelamentos
- 💳 **Cartão de Débito**: Controle de saldo, transações
- 🍽️ **Cartão Alimentação**: Benefícios alimentares
- 🍽️ **Cartão Refeição**: Benefícios de refeição
- ⛽ **Cartão Combustível**: Controle de abastecimento
- 🏥 **Cartão Saúde**: Benefícios de saúde

## 🛠️ Configuração do Ambiente

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- PostgreSQL (no container Proxmox)
- Docker (opcional, para desenvolvimento local)

### 1. Configuração do Banco PostgreSQL no Proxmox

O banco PostgreSQL já está configurado no container Proxmox. Para conectar:

```bash
# Testar conexão
./test-db-connection.sh

# Configurações padrão:
# Host: localhost (ou IP do Proxmox)
# Porta: 5432
# Banco: gestao_cartoes
# Usuário: postgres
# Senha: postgres123
```

### 2. Configuração da Aplicação

#### Perfis Disponíveis

- **Desenvolvimento** (`dev`): PostgreSQL local
- **Produção** (`prod`): PostgreSQL no Proxmox

#### Executar Aplicação

```bash
# Tornar scripts executáveis
chmod +x *.sh

# Executar com menu interativo
./run-app.sh

# Ou executar diretamente
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📊 Estrutura do Projeto

```
gestao-cartoes/
├── src/main/java/com/empresa/gestao_cartoes/
│   ├── config/          # Configurações (Security, Database, Swagger)
│   ├── controller/      # Controllers REST
│   ├── model/          # Entidades JPA
│   ├── repository/     # Repositórios JPA
│   └── service/        # Lógica de negócio
├── src/main/resources/
│   ├── application.properties      # Configuração padrão
│   ├── application-dev.properties  # Desenvolvimento
│   └── application-prod.properties # Produção
├── setup-postgres.sh   # Script para configurar PostgreSQL local
├── test-db-connection.sh # Script para testar conexão
└── run-app.sh         # Script para executar aplicação
```

## 🗄️ Modelo de Dados

### Entidades Principais

- **Cartoes**: Classe base abstrata para todos os tipos de cartão
- **Clientes**: Clientes (PF e PJ)
- **Contratos**: Contratos de cartão
- **Pessoas**: Dados pessoais
- **Enderecos**: Endereços dos clientes
- **Contatos**: Informações de contato

### Relacionamentos

- Cliente → Cartões (1:N)
- Cliente → Contratos (1:N)
- Contrato → Cartões (1:N)
- Pessoa → Endereços (1:N)
- Pessoa → Contatos (1:N)

## 🔧 APIs Disponíveis

### Cartões

- `GET /api/cartoes` - Listar cartões
- `GET /api/cartoes/{id}` - Buscar cartão por ID
- `POST /api/cartoes` - Criar novo cartão
- `PUT /api/cartoes/{id}` - Atualizar cartão
- `DELETE /api/cartoes/{id}` - Excluir cartão

### Clientes

- `GET /api/clientes` - Listar clientes
- `GET /api/clientes/{id}` - Buscar cliente por ID
- `POST /api/clientes` - Criar novo cliente
- `PUT /api/clientes/{id}` - Atualizar cliente

## 📚 Documentação da API

Após iniciar a aplicação, acesse:

- **Swagger UI**: http://localhost:8080/gestao-cartoes/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/gestao-cartoes/v3/api-docs

## 🔍 Monitoramento

- **Health Check**: http://localhost:8080/gestao-cartoes/actuator/health
- **Métricas**: http://localhost:8080/gestao-cartoes/actuator/metrics
- **Info**: http://localhost:8080/gestao-cartoes/actuator/info

## 🚨 Solução de Problemas

### Erro de Conexão com Banco

1. Verificar se o container PostgreSQL está rodando no Proxmox
2. Testar conexão: `./test-db-connection.sh`
3. Verificar configurações no `application.properties`

### Erro de Compilação

1. Verificar Java 17: `java -version`
2. Limpar projeto: `mvn clean`
3. Recompilar: `mvn compile`

### Erro de Porta em Uso

1. Verificar processos na porta 8080: `lsof -i :8080`
2. Parar processo ou alterar porta no `application.properties`

## 📝 Logs

Os logs são salvos em:
- Desenvolvimento: `logs/gestao-cartoes-dev.log`
- Produção: `logs/gestao-cartoes-prod.log`

## 🔐 Segurança

- Autenticação básica configurada
- Usuário padrão: `admin` / `admin123` (produção)
- Usuário desenvolvimento: `dev` / `dev123`

## 🎯 Próximos Passos

1. ✅ Configurar conexão com PostgreSQL no Proxmox
2. ✅ Implementar APIs REST
3. ✅ Configurar documentação Swagger
4. 🔄 Implementar frontend (Thymeleaf/React)
5. 🔄 Adicionar testes automatizados
6. 🔄 Implementar relatórios
7. 🔄 Configurar CI/CD

## 📞 Suporte

Para dúvidas ou problemas:
1. Verificar logs da aplicação
2. Testar conexão com banco
3. Verificar configurações de ambiente
