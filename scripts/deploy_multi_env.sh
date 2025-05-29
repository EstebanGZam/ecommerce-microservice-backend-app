#!/bin/bash

# Script de despliegue multi-ambiente para AKS - EcommerceGZam
set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Función de ayuda
show_help() {
    echo "Uso: $0 [AMBIENTE] [OPCIONES]"
    echo ""
    echo "Ambientes disponibles:"
    echo "  dev     - Ambiente de desarrollo"
    echo "  stage   - Ambiente de staging"
    echo "  prod    - Ambiente de producción"
    echo ""
    echo "Opciones:"
    echo "  -h, --help     Mostrar esta ayuda"
    echo "  -f, --force    No pedir confirmación"
    echo "  -v, --verbose  Salida detallada"
    echo ""
    echo "Ejemplos:"
    echo "  $0 dev                    # Desplegar en desarrollo"
    echo "  $0 stage --force          # Desplegar en staging sin confirmación"
    echo "  $0 prod --verbose         # Desplegar en producción con salida detallada"
}

# Configuraciones por ambiente
get_env_config() {
    local env=$1
    case $env in
        "dev")
            NAMESPACE="dev"
            REPLICAS=1
            RESOURCES_REQUESTS_CPU="100m"
            RESOURCES_REQUESTS_MEMORY="128Mi"
            RESOURCES_LIMITS_CPU="500m"
            RESOURCES_LIMITS_MEMORY="512Mi"
            SERVICE_TYPE="ClusterIP"
            ;;
        "stage")
            NAMESPACE="stage"
            REPLICAS=2
            RESOURCES_REQUESTS_CPU="200m"
            RESOURCES_REQUESTS_MEMORY="256Mi"
            RESOURCES_LIMITS_CPU="1000m"
            RESOURCES_LIMITS_MEMORY="1Gi"
            SERVICE_TYPE="ClusterIP"
            ;;
        "prod")
            NAMESPACE="prod"
            REPLICAS=3
            RESOURCES_REQUESTS_CPU="500m"
            RESOURCES_REQUESTS_MEMORY="512Mi"
            RESOURCES_LIMITS_CPU="2000m"
            RESOURCES_LIMITS_MEMORY="2Gi"
            SERVICE_TYPE="LoadBalancer"
            ;;
        *)
            log_error "Ambiente no válido: $env"
            show_help
            exit 1
            ;;
    esac
}

# Parsear argumentos
ENVIRONMENT=""
FORCE=false
VERBOSE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        dev|stage|prod)
            ENVIRONMENT="$1"
            shift
            ;;
        -f|--force)
            FORCE=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            log_error "Opción desconocida: $1"
            show_help
            exit 1
            ;;
    esac
done

# Validar ambiente
if [ -z "$ENVIRONMENT" ]; then
    log_error "Debe especificar un ambiente"
    show_help
    exit 1
fi

# Obtener configuración del ambiente
get_env_config $ENVIRONMENT

log_info "=== CONFIGURACIÓN DE DESPLIEGUE ==="
log_info "Ambiente: $ENVIRONMENT"
log_info "Namespace: $NAMESPACE"
log_info "Réplicas: $REPLICAS"
log_info "Tipo de servicio: $SERVICE_TYPE"

# Verificar conexión con AKS
log_info "Verificando conexión con AKS..."
if ! kubectl cluster-info > /dev/null 2>&1; then
    log_error "No se puede conectar al cluster AKS"
    exit 1
fi

CLUSTER_INFO=$(kubectl config current-context)
log_info "Cluster: ${CLUSTER_INFO}"

# Confirmación
if [ "$FORCE" = false ]; then
    echo ""
    log_warning "¿Continuar con el despliegue en $ENVIRONMENT? (y/N): "
    read -r response
    if [[ ! $response =~ ^[Yy]$ ]]; then
        log_info "Despliegue cancelado"
        exit 0
    fi
fi

# Directorio de manifiestos
K8S_MANIFESTS_DIR="k8s-manifests"
if [ ! -d "$K8S_MANIFESTS_DIR" ]; then
    log_error "Directorio $K8S_MANIFESTS_DIR no encontrado"
    exit 1
fi

# Función para aplicar manifiestos con reemplazo de variables
apply_manifest() {
    local manifest_file=$1
    local temp_file="/tmp/$(basename $manifest_file)"
    
    # Reemplazar variables en el manifiesto
    sed -e "s/namespace: dev/namespace: $NAMESPACE/g" \
        -e "s/replicas: 1/replicas: $REPLICAS/g" \
        -e "s/type: ClusterIP/type: $SERVICE_TYPE/g" \
        "$manifest_file" > "$temp_file"
    
    if [ "$VERBOSE" = true ]; then
        log_info "Aplicando: $manifest_file"
        kubectl apply -f "$temp_file" --dry-run=client -o yaml
    fi
    
    kubectl apply -f "$temp_file"
    rm "$temp_file"
}

# Crear namespace si no existe
log_info "Configurando namespace $NAMESPACE..."
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Aplicar ConfigMap (con variables de ambiente específicas)
log_info "Aplicando ConfigMap para $ENVIRONMENT..."
TEMP_CONFIGMAP="/tmp/configmap-$ENVIRONMENT.yaml"
sed "s/namespace: dev/namespace: $NAMESPACE/g" "${K8S_MANIFESTS_DIR}/common-env-configmap.yaml" > "$TEMP_CONFIGMAP"
kubectl apply -f "$TEMP_CONFIGMAP"
rm "$TEMP_CONFIGMAP"

# Servicios de infraestructura
log_info "=== DESPLEGANDO SERVICIOS DE INFRAESTRUCTURA ==="

INFRA_SERVICES=("zipkin" "service-discovery" "cloud-config")

for service in "${INFRA_SERVICES[@]}"; do
    log_info "Desplegando $service en $ENVIRONMENT..."
    apply_manifest "${K8S_MANIFESTS_DIR}/$service/$service-deployment.yaml"
    apply_manifest "${K8S_MANIFESTS_DIR}/$service/$service-service.yaml"
    
    log_info "Esperando a que $service esté listo..."
    kubectl rollout status deployment/$service -n $NAMESPACE --timeout=300s
    log_success "$service desplegado correctamente en $ENVIRONMENT"
done

# Pausa para estabilización
log_info "Pausa para estabilización de servicios de infraestructura..."
sleep 15

# Servicios de aplicación
log_info "=== DESPLEGANDO SERVICIOS DE APLICACIÓN ==="

APP_SERVICES=(
    "order-service"
    "payment-service"
    "product-service"
    "shipping-service"
    "user-service"
    "favourite-service"
)

# Desplegar servicios de aplicación
for service in "${APP_SERVICES[@]}"; do
    log_info "Desplegando $service en $ENVIRONMENT..."
    apply_manifest "${K8S_MANIFESTS_DIR}/$service/$service-deployment.yaml"
    apply_manifest "${K8S_MANIFESTS_DIR}/$service/$service-service.yaml"
done

# API Gateway y Proxy Client
log_info "Desplegando API Gateway en $ENVIRONMENT..."
apply_manifest "${K8S_MANIFESTS_DIR}/api-gateway/api-gateway-deployment.yaml"
apply_manifest "${K8S_MANIFESTS_DIR}/api-gateway/api-gateway-service.yaml"

log_info "Desplegando Proxy Client en $ENVIRONMENT..."
apply_manifest "${K8S_MANIFESTS_DIR}/proxy-client/proxy-client-deployment.yaml"
apply_manifest "${K8S_MANIFESTS_DIR}/proxy-client/proxy-client-service.yaml"

# Verificar despliegues críticos
log_info "=== VERIFICANDO DESPLIEGUES CRÍTICOS ==="
CRITICAL_SERVICES=("service-discovery" "cloud-config" "api-gateway")
for service in "${CRITICAL_SERVICES[@]}"; do
    log_info "Verificando $service..."
    kubectl rollout status deployment/$service -n $NAMESPACE --timeout=120s
done

# Resumen final
log_info "=== RESUMEN DEL DESPLIEGUE ==="
echo ""
log_success "¡Despliegue en $ENVIRONMENT completado!"
echo ""

log_info "Estado de los pods en $NAMESPACE:"
kubectl get pods -n $NAMESPACE

echo ""
log_info "Estado de los services en $NAMESPACE:"
kubectl get services -n $NAMESPACE

echo ""
log_info "=== COMANDOS ÚTILES ==="
echo "• Ver pods: kubectl get pods -n $NAMESPACE"
echo "• Ver logs: kubectl logs -f deployment/<service> -n $NAMESPACE"
echo "• Port-forward: kubectl port-forward service/<service> <port>:<port> -n $NAMESPACE"

if [ "$SERVICE_TYPE" = "LoadBalancer" ]; then
    echo ""
    log_info "Verificando LoadBalancers (puede tardar unos minutos)..."
    kubectl get services -n $NAMESPACE --field-selector spec.type=LoadBalancer
fi

log_success "Script completado exitosamente para $ENVIRONMENT"