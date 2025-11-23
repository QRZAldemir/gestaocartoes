#!/bin/bash

# ==============================================
# Script para buildar e implantar a aplicação Spring Boot
# Sistema de Gestão de Cartões
# ==============================================

echo "🚀 Build e Deploy - Gestão de Cartões - Spring Boot"
echo "===================================================="

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

# Verificar se o script está sendo executado como root
if [[ $EUID -ne 0 ]]; then
   error "Este script deve ser executado como root (use sudo)"
   exit 1
fi

# Verificar se o diretório do projeto existe
PROJECT_DIR="/home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes"
if [ ! -d "$PROJECT_DIR" ]; then
    error "Diretório do projeto não encontrado: $PROJECT_DIR"
    exit 1
fi

# Navegar para o diretório do projeto
cd $PROJECT_DIR
log "Navegando para o diretório do projeto: $PROJECT_DIR"

# Verificar se o Maven está instalado
if ! command -v mvn &> /dev/null; then
    error "Maven não está instalado. Instalando..."
    apt update
    apt install -y maven
fi

# Verificar se o Java 17 está instalado
if ! java -version 2>&1 | grep -q "17"; then
    warning "Java 17 não encontrado. Instalando..."
    apt update
    apt install -y openjdk-17-jdk
fi

# Criar diretório para o JAR se não existir
JAR_DIR="/opt/gestao-cartoes"
mkdir -p $JAR_DIR

# Compilar a aplicação
log "Compilando a aplicação..."
mvn clean package -DskipTests

# Verificar se o build foi bem-sucedido
if [ $? -eq 0 ]; then
    log "✅ Build bem-sucedido!"

    # Verificar se o JAR foi gerado
    JAR_FILE="$PROJECT_DIR/target/gestao-cartoes.jar"
    if [ -f "$JAR_FILE" ]; then
        log "Arquivo JAR encontrado: $JAR_FILE"

        # Copiar o JAR para o diretório de implantação
        log "Copiando JAR para o diretório de implantação..."
        cp $JAR_FILE $JAR_DIR/

        # Configurar permissões
        chown www-data:www-data $JAR_DIR/gestao-cartoes.jar
        chmod 755 $JAR_DIR/gestao-cartoes.jar

        # Verificar se o serviço systemd existe
        if [ -f "/etc/systemd/system/gestao-cartoes.service" ]; then
            # Recarregar o systemd
            systemctl daemon-reload

            # Iniciar ou reiniciar o serviço
            if systemctl is-active --quiet gestao-cartoes; then
                log "Reiniciando o serviço gestao-cartoes..."
                systemctl restart gestao-cartoes
            else
                log "Iniciando o serviço gestao-cartoes..."
                systemctl start gestao-cartoes
            fi

            # Habilitar o serviço para iniciar com o sistema
            systemctl enable gestao-cartoes

            # Verificar status do serviço
            sleep 3
            if systemctl is-active --quiet gestao-cartoes; then
                log "✅ Serviço gestao-cartoes está ativo!"

                # Verificar se a aplicação está respondendo
                log "Verificando se a aplicação está respondendo..."
                if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/gestao-cartoes/actuator/health | grep -q "200"; then
                    log "✅ Aplicação está saudável!"
                    info "A aplicação está disponível em: http://$(hostname)/gestao-cartoes"
                    info "Swagger UI: http://$(hostname)/gestao-cartoes/swagger-ui.html"
                else
                    warning "A aplicação pode não estar respondendo corretamente. Verificando logs..."
                    journalctl -u gestao-cartoes --no-pager -n 20
                fi
            else
                error "❌ Falha ao iniciar o serviço gestao-cartoes. Verificando logs..."
                journalctl -u gestao-cartoes --no-pager -n 20
            fi
        else
            warning "Serviço systemd não encontrado. Execute o script deploy.sh primeiro."
        fi
    else
        error "❌ Arquivo JAR não encontrado após o build."
        exit 1
    fi
else
    error "❌ Falha no build da aplicação."
    exit 1
fi
