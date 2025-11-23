#!/bin/bash

# ==============================================
# Script para configurar banco PostgreSQL
# Sistema de Gestão de Cartões - Proxmox
# ==============================================

echo "🗄️  Configurando banco PostgreSQL - Sistema de Gestão de Cartões"
echo "=================================================================="

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
SCHEMA_FILE="database-schema.sql"

# Verificar se o psql está instalado
if ! command -v psql &> /dev/null; then
    warning "psql não está instalado. Instalando..."
    sudo apt update
    sudo apt install -y postgresql-client
fi

# Verificar se o arquivo de schema existe
if [ ! -f "$SCHEMA_FILE" ]; then
    error "Arquivo $SCHEMA_FILE não encontrado!"
    exit 1
fi

# Função para testar conexão
test_connection() {
    info "Testando conexão com PostgreSQL..."
    export PGPASSWORD="$DB_PASSWORD"
    
    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" &> /dev/null; then
        log "✅ Conexão estabelecida com sucesso!"
        return 0
    else
        error "❌ Falha na conexão com PostgreSQL"
        return 1
    fi
}

# Função para executar schema
execute_schema() {
    info "Executando script de criação do banco..."
    export PGPASSWORD="$DB_PASSWORD"
    
    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$SCHEMA_FILE"; then
        log "✅ Schema executado com sucesso!"
        return 0
    else
        error "❌ Erro ao executar schema"
        return 1
    fi
}

# Função para verificar tabelas criadas
verify_tables() {
    info "Verificando tabelas criadas..."
    export PGPASSWORD="$DB_PASSWORD"
    
    echo ""
    info "📋 Tabelas criadas:"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
        SELECT 
            table_name as \"Tabela\",
            table_type as \"Tipo\"
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
        ORDER BY table_name;
    "
    
    echo ""
    info "📊 Contagem de tabelas:"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
        SELECT COUNT(*) as \"Total de Tabelas\"
        FROM information_schema.tables 
        WHERE table_schema = 'public';
    "
}

# Função para verificar dados iniciais
verify_initial_data() {
    info "Verificando dados iniciais..."
    export PGPASSWORD="$DB_PASSWORD"
    
    echo ""
    info "👥 Perfis criados:"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
        SELECT nome, descricao, tipo_padrao 
        FROM perfil 
        ORDER BY nome;
    "
    
    echo ""
    info "🏦 Bancos cadastrados:"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
        SELECT codigo, nome, ativo 
        FROM banco 
        ORDER BY codigo;
    "
}

# Função para criar usuário da aplicação
create_app_user() {
    info "Criando usuário para a aplicação..."
    export PGPASSWORD="$DB_PASSWORD"
    
    # Verificar se o usuário já existe
    USER_EXISTS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "
        SELECT COUNT(*) FROM pg_user WHERE usename = 'gestao_cartoes_app';
    " | tr -d ' ')
    
    if [ "$USER_EXISTS" = "0" ]; then
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
            CREATE USER gestao_cartoes_app WITH PASSWORD 'app_password_123';
            GRANT ALL PRIVILEGES ON DATABASE postgre TO gestao_cartoes_app;
            GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO gestao_cartoes_app;
            GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO gestao_cartoes_app;
        "
        log "✅ Usuário da aplicação criado: gestao_cartoes_app"
    else
        warning "Usuário gestao_cartoes_app já existe"
    fi
}

# Função para mostrar resumo
show_summary() {
    echo ""
    echo "🎉 CONFIGURAÇÃO CONCLUÍDA COM SUCESSO!"
    echo "======================================"
    echo ""
    echo "📊 Informações de Conexão:"
    echo "   Host: $DB_HOST"
    echo "   Porta: $DB_PORT"
    echo "   Banco: $DB_NAME"
    echo "   Usuário Principal: $DB_USER"
    echo "   Usuário App: gestao_cartoes_app"
    echo ""
    echo "🔧 Configuração para application.properties:"
    echo "   spring.datasource.url=jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
    echo "   spring.datasource.username=$DB_USER"
    echo "   spring.datasource.password=$DB_PASSWORD"
    echo ""
    echo "🚀 Próximos passos:"
    echo "   1. Executar aplicação: mvn spring-boot:run"
    echo "   2. Acessar API: http://localhost:8080/gestao-cartoes"
    echo "   3. Documentação: http://localhost:8080/gestao-cartoes/swagger-ui.html"
    echo ""
    echo "📋 APIs disponíveis:"
    echo "   GET  /api/cartoes - Listar cartões"
    echo "   POST /api/cartoes - Criar cartão"
    echo "   GET  /api/pessoas - Listar pessoas"
    echo "   POST /api/pessoas - Criar pessoa"
    echo ""
}

# Execução principal
main() {
    echo ""
    info "Iniciando configuração do banco de dados..."
    echo ""
    
    # Testar conexão
    if ! test_connection; then
        error "Não foi possível conectar ao banco. Verifique as configurações."
        exit 1
    fi
    
    # Executar schema
    if ! execute_schema; then
        error "Falha ao executar schema do banco."
        exit 1
    fi
    
    # Verificar tabelas
    verify_tables
    
    # Verificar dados iniciais
    verify_initial_data
    
    # Criar usuário da aplicação
    create_app_user
    
    # Mostrar resumo
    show_summary
    
    # Limpar variável de ambiente
    unset PGPASSWORD
    
    log "✅ Configuração do banco concluída com sucesso!"
}

# Executar função principal
main "$@"
