#!/bin/bash

# Mochi - Script de inicio completo (Backend + Frontend)
# Uso: ./start.sh

set -e

echo "🚀 Iniciando Mochi - Optimizador de cortes de aluminio"
echo "======================================================"

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Funciones
start_backend() {
    echo -e "${YELLOW}📦 Iniciando Backend (Spring Boot)...${NC}"
    cd /home/alex/dev/front_mochi
    
    # Compilar si no existe el JAR
    if [ ! -f "target/web-service-fabrica.jar" ]; then
        echo "Compilando proyecto..."
        mvn clean package -DskipTests
    fi
    
    # Arrancar backend en background
    java -jar target/web-service-fabrica.jar &
    BACKEND_PID=$!
    echo $BACKEND_PID > .backend.pid
    
    echo -e "${GREEN}✅ Backend iniciado (PID: $BACKEND_PID)${NC}"
    echo "   Backend disponible en: http://localhost:8080"
}

start_frontend() {
    echo -e "${YELLOW}🎨 Iniciando Frontend (React + Vite)...${NC}"
    cd /home/alex/dev/front_mochi/frontend
    
    # Instalar dependencias si no existen
    if [ ! -d "node_modules" ]; then
        echo "Instalando dependencias..."
        npm install
    fi
    
    # Arrancar frontend en background
    npm run dev &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > .frontend.pid
    
    echo -e "${GREEN}✅ Frontend iniciado (PID: $FRONTEND_PID)${NC}"
    echo "   Frontend disponible en: http://localhost:5173"
}

stop_services() {
    echo -e "${RED}🛑 Deteniendo servicios...${NC}"
    
    if [ -f ".backend.pid" ]; then
        kill $(cat .backend.pid) 2>/dev/null || true
        rm .backend.pid
        echo "Backend detenido"
    fi
    
    if [ -f ".frontend.pid" ]; then
        kill $(cat .frontend.pid) 2>/dev/null || true
        rm .frontend.pid
        echo "Frontend detenido"
    fi
}

# Verificar que MySQL esté corriendo
check_mysql() {
    echo -e "${YELLOW}🔍 Verificando conexión a MySQL...${NC}"
    if mysql -h localhost -u android -pFabricasion17/ -e "USE tff;" 2>/dev/null; then
        echo -e "${GREEN}✅ MySQL conectado${NC}"
    else
        echo -e "${RED}❌ Error: MySQL no está disponible o las credenciales son incorrectas${NC}"
        echo "   Verifica que MySQL esté corriendo y las credenciales sean correctas"
        exit 1
    fi
}

# Menú
case "${1:-start}" in
    start)
        check_mysql
        start_backend
        start_frontend
        echo ""
        echo "======================================================"
        echo -e "${GREEN}🎉 Mochi iniciado correctamente!${NC}"
        echo "   Frontend: http://localhost:5173"
        echo "   Backend:   http://localhost:8080"
        echo ""
        echo "Para detener: ./start.sh stop"
        ;;
    stop)
        stop_services
        echo -e "${GREEN}✅ Servicios detenidos${NC}"
        ;;
    restart)
        stop_services
        sleep 2
        check_mysql
        start_backend
        start_frontend
        echo -e "${GREEN}✅ Servicios reiniciados${NC}"
        ;;
    *)
        echo "Uso: $0 {start|stop|restart}"
        exit 1
        ;;
esac
