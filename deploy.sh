#!/bin/bash

# ==============================================
# Script para configurar Nginx e implantar aplicação Spring Boot
# Sistema de Gestão de Cartões
# ==============================================

echo "🚀 Configurando Nginx para Gestão de Cartões - Spring Boot"
echo "==========================================================="

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

# Instalar Nginx se não estiver instalado
if ! command -v nginx &> /dev/null; then
    log "Instalando Nginx..."
    apt update
    apt install -y nginx
    systemctl enable nginx
    systemctl start nginx
    log "Nginx instalado com sucesso!"
else
    log "Nginx já está instalado."
fi

# Criar diretório para logs do Nginx
mkdir -p /var/log/nginx/gestao-cartoes
chown -R www-data:www-data /var/log/nginx/gestao-cartoes

# Criar diretório para a aplicação
APP_DIR="/opt/gestao-cartoes"
if [ ! -d "$APP_DIR" ]; then
    log "Criando diretório da aplicação: $APP_DIR"
    mkdir -p $APP_DIR
    chown -R www-data:www-data $APP_DIR
fi

# Criar serviço systemd para a aplicação
log "Criando serviço systemd para a aplicação..."
cat > /etc/systemd/system/gestao-cartoes.service << EOF
[Unit]
Description=Gestao Cartoes Spring Boot Application
After=network.target postgresql.service

[Service]
User=www-data
Group=www-data
WorkingDirectory=$APP_DIR
ExecStart=/usr/bin/java -jar $APP_DIR/gestao-cartoes.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 \$MAINPID
SuccessExitStatus=143
Restart=always
RestartSec=10

# Configurações de ambiente
Environment="JAVA_OPTS=-Xms512m -Xmx1024m"
Environment="SPRING_PROFILES_ACTIVE=prod"

# Limites de recursos
LimitNOFILE=65536
LimitMEMLOCK=infinity

[Install]
WantedBy=multi-user.target
EOF

# Criar configuração do Nginx
log "Configurando Nginx..."
cat > /etc/nginx/sites-available/gestao-cartoes << 'EOF'
server {
    listen 80;
    server_name _;  # Aceitar qualquer nome de host

    # Diretório para logs
    access_log /var/log/nginx/gestao-cartoes/access.log;
    error_log /var/log/nginx/gestao-cartoes/error.log;

    # Diretório para arquivos estáticos
    root /opt/gestao-cartoes/static;
    index index.html;

    # Proxy para a aplicação Spring Boot
    location /gestao-cartoes {
        proxy_pass http://localhost:8080/gestao-cartoes;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Configurações de timeout
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        # Buffering
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;

        # Configurações de cabeçalho para CORS
        add_header 'Access-Control-Allow-Origin' '*' always;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
        add_header 'Access-Control-Allow-Headers' 'DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization' always;
    }

    # Configuração para a documentação da API (Swagger)
    location /gestao-cartoes/swagger-ui {
        proxy_pass http://localhost:8080/gestao-cartoes/swagger-ui;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Configuração para o health check
    location /gestao-cartoes/actuator {
        proxy_pass http://localhost:8080/gestao-cartoes/actuator;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Configuração para arquivos estáticos
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        root /opt/gestao-cartoes/static;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Bloco para redirecionar para HTTPS em produção (opcional)
    # Descomente as linhas abaixo se você tiver um certificado SSL
    # 
    # listen 443 ssl http2;
    # ssl_certificate /etc/letsencrypt/live/seudominio.com/fullchain.pem;
    # ssl_certificate_key /etc/letsencrypt/live/seudominio.com/privkey.pem;
    # ssl_protocols TLSv1.2 TLSv1.3;
    # ssl_ciphers 'ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256';
    # ssl_prefer_server_ciphers off;
    # ssl_session_cache shared:SSL:10m;
    # ssl_session_timeout 1d;
    # add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
}
EOF

# Habilitar o site do Nginx
ln -sf /etc/nginx/sites-available/gestao-cartoes /etc/nginx/sites-enabled/

# Remover o site padrão do Nginx
if [ -f /etc/nginx/sites-enabled/default ]; then
    rm /etc/nginx/sites-enabled/default
    log "Site padrão do Nginx removido."
fi

# Testar configuração do Nginx
log "Testando configuração do Nginx..."
nginx -t
if [ $? -eq 0 ]; then
    log "Configuração do Nginx válida. Reiniciando serviço..."
    systemctl reload nginx
else
    error "Erro na configuração do Nginx. Verifique os arquivos de configuração."
    exit 1
fi

# Criar script de implantação
cat > /opt/gestao-cartoes/deploy-app.sh << 'EOF'
#!/bin/bash

# ==============================================
# Script para implantar nova versão da aplicação
# ==============================================

APP_DIR="/opt/gestao-cartoes"
BACKUP_DIR="$APP_DIR/backups"
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")

# Criar diretório de backup se não existir
mkdir -p $BACKUP_DIR

# Parar a aplicação
echo "Parando a aplicação..."
systemctl stop gestao-cartoes

# Fazer backup da versão atual
if [ -f "$APP_DIR/gestao-cartoes.jar" ]; then
    echo "Fazendo backup da versão atual..."
    cp $APP_DIR/gestao-cartoes.jar $BACKUP_DIR/gestao-cartoes-$TIMESTAMP.jar
fi

# Copiar novo JAR para o diretório de implantação
echo "Copiando nova versão da aplicação..."
cp /home/querioz/eclipse-workspace/Projeto/SpringBoot/gestao-cartoes/target/gestao-cartoes.jar $APP_DIR/

# Configurar permissões
chown www-data:www-data $APP_DIR/gestao-cartoes.jar
chmod 755 $APP_DIR/gestao-cartoes.jar

# Iniciar a aplicação
echo "Iniciando a aplicação..."
systemctl start gestao-cartoes

# Verificar status
sleep 5
if systemctl is-active --quiet gestao-cartoes; then
    echo "✅ Aplicação iniciada com sucesso!"
else
    echo "❌ Erro ao iniciar a aplicação. Verificando logs..."
    journalctl -u gestao-cartoes --no-pager -n 20
fi
EOF

# Dar permissão de execução ao script de implantação
chmod +x /opt/gestao-cartoes/deploy-app.sh

# Criar script de monitoramento
cat > /opt/gestao-cartoes/monitor.sh << 'EOF'
#!/bin/bash

# ==============================================
# Script para monitorar a aplicação
# ==============================================

APP_NAME="gestao-cartoes"
HEALTH_URL="http://localhost:8080/gestao-cartoes/actuator/health"

# Verificar status do serviço
systemctl is-active --quiet $APP_NAME
if [ $? -eq 0 ]; then
    echo "✅ Serviço $APP_NAME está ativo"
else
    echo "❌ Serviço $APP_NAME não está ativo"
    # Tentar reiniciar o serviço
    systemctl start $APP_NAME
    sleep 5
    systemctl is-active --quiet $APP_NAME
    if [ $? -eq 0 ]; then
        echo "✅ Serviço $APP_NAME foi reiniciado com sucesso"
    else
        echo "❌ Falha ao reiniciar o serviço $APP_NAME"
        exit 1
    fi
fi

# Verificar saúde da aplicação
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)
if [ "$HTTP_CODE" -eq 200 ]; then
    echo "✅ Aplicação $APP_NAME está saudável"
else
    echo "❌ Aplicação $APP_NAME não está saudável (HTTP Code: $HTTP_CODE)"
fi

# Verificar uso de memória
JAVA_PID=$(pgrep -f "gestao-cartoes.jar")
if [ ! -z "$JAVA_PID" ]; then
    MEM_USAGE=$(ps -p $JAVA_PID -o %mem --no-headers)
    echo "Uso de memória: ${MEM_USAGE}%"

    # Verificar se o uso de memória está acima de 90%
    if (( $(echo "$MEM_USAGE > 90" | bc -l) )); then
        echo "⚠️  Uso de memória acima de 90%"
    fi
fi
EOF

# Dar permissão de execução ao script de monitoramento
chmod +x /opt/gestao-cartoes/monitor.sh

# Criar script para agendar monitoramento
cat > /etc/cron.d/gestao-cartoes-monitor << EOF
# Verificar a cada 5 minutos se a aplicação está rodando
*/5 * * * * www-data /opt/gestao-cartoes/monitor.sh >> /var/log/nginx/gestao-cartoes/monitor.log 2>&1
EOF

log "✅ Configuração concluída!"
echo ""
echo "Próximos passos:"
echo "1. Compile sua aplicação com: mvn clean package"
echo "2. Copie o JAR gerado para /opt/gestao-cartoes/"
echo "3. Implante a aplicação com: /opt/gestao-cartoes/deploy-app.sh"
echo "4. Monitore a aplicação com: /opt/gestao-cartoes/monitor.sh"
echo ""
echo "A aplicação estará disponível em: http://seu-servidor/gestao-cartoes"
echo "Swagger UI: http://seu-servidor/gestao-cartoes/swagger-ui.html"
