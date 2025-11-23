#!/bin/bash

# ==============================================
# Script simples para testar conexão PostgreSQL
# Sistema de Gestão de Cartões - Proxmox
# ==============================================

echo "🔍 Testando conexão com PostgreSQL no Proxmox"
echo "=============================================="

# Configurações do PostgreSQL no Proxmox
DB_HOST="192.168.100.10"
DB_PORT="5432"
DB_NAME="postgres"
DB_USER="postgres"
DB_PASSWORD="Qrz#89dc"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

# Testar conectividade de rede
info "Testando conectividade de rede com Proxmox..."
if ping -c 1 $DB_HOST &> /dev/null; then
    log "✅ Proxmox acessível via rede"
else
    error "❌ Não foi possível acessar o Proxmox ($DB_HOST)"
    exit 1
fi

# Testar porta PostgreSQL
info "Testando porta PostgreSQL..."
if timeout 5 bash -c "</dev/tcp/$DB_HOST/$DB_PORT" 2>/dev/null; then
    log "✅ Porta PostgreSQL ($DB_PORT) está aberta"
else
    error "❌ Porta PostgreSQL ($DB_PORT) não está acessível"
    exit 1
fi

# Verificar se o Java está instalado
info "Verificando Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    log "✅ Java encontrado: $JAVA_VERSION"
else
    error "❌ Java não encontrado. Instale Java 17 ou superior"
    exit 1
fi

# Verificar se o Maven está instalado
info "Verificando Maven..."
if command -v mvn &> /dev/null; then
    MAVEN_VERSION=$(mvn -version 2>&1 | head -n 1)
    log "✅ Maven encontrado: $MAVEN_VERSION"
else
    error "❌ Maven não encontrado. Instale Maven"
    exit 1
fi

# Testar compilação do projeto
info "Testando compilação do projeto..."
if mvn clean compile -q; then
    log "✅ Projeto compila com sucesso"
else
    error "❌ Erro na compilação do projeto"
    exit 1
fi

echo ""
log "🎉 Testes de conectividade concluídos com sucesso!"
echo ""
echo "📊 Configuração para conexão:"
echo "   Host: $DB_HOST"
echo "   Porta: $DB_PORT"
echo "   Banco: $DB_NAME"
echo "   Usuário: $DB_USER"
echo ""
echo "🚀 Próximos passos:"
echo "   1. Executar aplicação: mvn spring-boot:run"
echo "   2. A aplicação tentará conectar ao PostgreSQL automaticamente"
echo "   3. Se houver erro de conexão, verifique as credenciais no Proxmox"
echo ""
echo "💡 Para verificar o container PostgreSQL no Proxmox:"
echo "   - Acesse: https://192.168.100.2:8006"
echo "   - Container ID: 100"
echo "   - Nome: DbPostGre"
echo ""







