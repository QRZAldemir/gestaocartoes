#!/bin/bash

# ==============================================
# Script para testar conexão com PostgreSQL
# no container Proxmox
# ==============================================

echo "🔍 Testando conexão com PostgreSQL no Proxmox"
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

# Configurações do PostgreSQL no Proxmox
DB_HOST="192.168.100.10"
DB_PORT="5432"
DB_NAME="postgres"
DB_USER="postgres"
DB_PASSWORD="Qrz#89dc"

# Verificar se o psql está instalado
if ! command -v psql &> /dev/null; then
    warning "psql não está instalado. Instalando..."
    sudo apt update
    sudo apt install -y postgresql-client
fi

# Função para testar conexão
test_connection() {
    local host=$1
    local port=$2
    local dbname=$3
    local user=$4
    local password=$5
    
    info "Testando conexão com: $user@$host:$port/$dbname"
    
    # Definir senha como variável de ambiente
    export PGPASSWORD="$password"
    
    # Testar conexão
    if psql -h "$host" -p "$port" -U "$user" -d "$dbname" -c "SELECT version();" &> /dev/null; then
        log "✅ Conexão bem-sucedida!"
        
        # Obter informações do banco
        echo ""
        info "📊 Informações do banco:"
        psql -h "$host" -p "$port" -U "$user" -d "$dbname" -c "
            SELECT 
                current_database() as database,
                current_user as user,
                version() as version,
                current_setting('timezone') as timezone;
        "
        
        # Verificar se as tabelas existem
        echo ""
        info "📋 Verificando tabelas existentes:"
        psql -h "$host" -p "$port" -U "$user" -d "$dbname" -c "
            SELECT table_name, table_type 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            ORDER BY table_name;
        "
        
        return 0
    else
        error "Falha na conexão"
        return 1
    fi
}

# Testar diferentes configurações possíveis
echo "Testando configurações comuns do Proxmox..."
echo ""

# Configuração 1: localhost padrão
info "🔧 Teste 1: localhost:5432"
if test_connection "$DB_HOST" "$DB_PORT" "$DB_NAME" "$DB_USER" "$DB_PASSWORD"; then
    echo ""
    log "Conexão estabelecida com sucesso!"
    echo ""
    echo "Configuração recomendada para application.properties:"
    echo "   spring.datasource.url=jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
    echo "   spring.datasource.username=$DB_USER"
    echo "   spring.datasource.password=$DB_PASSWORD"
    exit 0
fi

# Configuração 2: IP do Proxmox (comum)
info "Teste 2: IP do Proxmox (192.168.1.100:5432)"
if test_connection "192.168.1.100" "$DB_PORT" "$DB_NAME" "$DB_USER" "$DB_PASSWORD"; then
    echo ""
    log "Conexão estabelecida com sucesso!"
    echo ""
    echo "Configuração recomendada para application.properties:"
    echo "spring.datasource.url=jdbc:postgresql://192.168.1.100:$DB_PORT/$DB_NAME"
    echo "spring.datasource.username=$DB_USER"
    echo "spring.datasource.password=$DB_PASSWORD"
    exit 0
fi

# Configuração 3: Outras portas comuns
for port in 5433 5434 15432; do
    info "Teste 3: localhost:$port"
    if test_connection "$DB_HOST" "$port" "$DB_NAME" "$DB_USER" "$DB_PASSWORD"; then
        echo ""
        log "Conexão estabelecida com sucesso!"
        echo ""
        echo "Configuração recomendada para application.properties:"
        echo "spring.datasource.url=jdbc:postgresql://$DB_HOST:$port/$DB_NAME"
        echo "spring.datasource.username=$DB_USER"
        echo "spring.datasource.password=$DB_PASSWORD"
        exit 0
    fi
done

# Se chegou até aqui, não conseguiu conectar
echo ""
error "Não foi possível conectar com nenhuma configuração testada"
echo ""
echo "Para resolver, verifique:"
echo "1. Se o container PostgreSQL está rodando no Proxmox"
echo "2. Se a porta está correta (comando: lxc list)"
echo "3. Se o firewall permite conexões na porta"
echo "4. Se as credenciais estão corretas"
echo ""
echo "Comandos úteis no Proxmox:"
echo "lxc list                    # Listar containers"
echo "lxc exec <container> -- psql -U postgres -l  # Listar bancos"
echo "lxc exec <container> -- netstat -tlnp | grep 5432  # Verificar porta"
echo ""

# Limpar variável de ambiente
unset PGPASSWORD

