#!/bin/bash

# ==============================================
# Script para executar a aplicação Spring Boot
# Sistema de Gestão de Cartões
# ==============================================

echo "🚀 Sistema de Gestão de Cartões - Spring Boot"
echo "=============================================="

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Verificar se o Maven está instalado
if ! command -v mvn &> /dev/null; then
    error "Maven não está instalado. Instalando..."
    sudo apt update
    sudo apt install -y maven
fi

# Verificar se o Java 17 está instalado
if ! java -version 2>&1 | grep -q "17"; then
    warning "Java 17 não encontrado. Verificando versões disponíveis..."
    java -version
    echo ""
    echo "Para instalar Java 17:"
    echo "sudo apt install openjdk-17-jdk"
    echo ""
fi

# Função para mostrar menu
show_menu() {
    echo ""
    echo "📋 Selecione o ambiente de execução:"
    echo "   1) Desenvolvimento (localhost:5432)"
    echo "   2) Produção (Proxmox container)"
    echo "   3) Testar conexão com banco"
    echo "   4) Compilar projeto"
    echo "   5) Limpar e compilar"
    echo "   6) Sair"
    echo ""
    read -p "Digite sua opção (1-6): " choice
}

# Função para testar conexão
test_connection() {
    info "Testando conexão com o banco de dados..."
    if [ -f "test-db-connection.sh" ]; then
        chmod +x test-db-connection.sh
        ./test-db-connection.sh
    else
        error "Script de teste não encontrado"
    fi
}

# Função para compilar
compile_project() {
    info "Compilando projeto..."
    mvn clean compile
    if [ $? -eq 0 ]; then
        log "✅ Compilação bem-sucedida!"
    else
        error "❌ Erro na compilação"
        return 1
    fi
}

# Função para limpar e compilar
clean_compile() {
    info "Limpando e compilando projeto..."
    mvn clean compile
    if [ $? -eq 0 ]; then
        log "✅ Limpeza e compilação bem-sucedidas!"
    else
        error "❌ Erro na limpeza/compilação"
        return 1
    fi
}

# Função para executar aplicação
run_app() {
    local profile=$1
    local profile_name=$2
    
    info "Iniciando aplicação no perfil: $profile_name"
    echo ""
    
    # Verificar se o banco está acessível
    if [ "$profile" = "dev" ]; then
        info "Verificando conexão com PostgreSQL local..."
        if ! pg_isready -h localhost -p 5432 &> /dev/null; then
            warning "PostgreSQL local não está rodando. Iniciando com Docker..."
            if [ -f "setup-postgres.sh" ]; then
                chmod +x setup-postgres.sh
                ./setup-postgres.sh
            fi
        fi
    fi
    
    # Executar aplicação
    log "🚀 Iniciando Spring Boot com perfil: $profile"
    echo ""
    echo "📊 URLs importantes:"
    echo "   Aplicação: http://localhost:8080/gestao-cartoes"
    echo "   API Docs: http://localhost:8080/gestao-cartoes/swagger-ui.html"
    echo "   Health Check: http://localhost:8080/gestao-cartoes/actuator/health"
    echo ""
    echo "⏹️  Para parar a aplicação: Ctrl+C"
    echo ""
    
    mvn spring-boot:run -Dspring-boot.run.profiles=$profile
}

# Loop principal
while true; do
    show_menu
    
    case $choice in
        1)
            run_app "dev" "Desenvolvimento"
            ;;
        2)
            run_app "prod" "Produção (Proxmox)"
            ;;
        3)
            test_connection
            ;;
        4)
            compile_project
            ;;
        5)
            clean_compile
            ;;
        6)
            log "👋 Saindo..."
            exit 0
            ;;
        *)
            error "Opção inválida. Tente novamente."
            ;;
    esac
    
    echo ""
    read -p "Pressione Enter para continuar..."
done
