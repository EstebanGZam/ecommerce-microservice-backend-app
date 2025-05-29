#!/bin/bash

# Script de limpieza multi-ambiente para AKS - EcommerceGZam
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
    echo "  -h, --help          Mostrar esta ayuda"
    echo "  -f, --force         No pedir confirmación"
    echo "  -v, --verbose       Salida detallada"
    echo "  --keep-namespace    No eliminar el namespace"
    echo "  --dry-run          Solo mostrar lo que se eliminaría"
    echo ""
    echo "Ejemplos:"
    echo "  $0 dev                      # Limpiar desarrollo"
    echo "  $0 stage --force            # Limpiar staging sin confirmación"
    echo "  $0 prod --verbose           # Limpiar producción con salida detallada"
    echo "  $0 dev --keep-namespace     # Limpiar pero mantener namespace"
    echo "  $0 prod --dry-run           # Ver qué se eliminaría en producción"
}

# Configuraciones por ambiente
get_env_config() {
    local env=$1
    case $env in
        "dev")
            NAMESPACE="dev"
            ;;
        "stage")
            NAMESPACE="stage"
            ;;
        "prod")
            NAMESPACE="prod"
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
KEEP_NAMESPACE=false
DRY_RUN=false

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
        --keep-namespace)
            KEEP_NAMESPACE=true
            shift
            ;;
        --dry-run)
            DRY_RUN=true
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

log_info "=== CONFIGURACIÓN DE LIMPIEZA ==="
log_info "Ambiente: $ENVIRONMENT"
log_info "Namespace: $NAMESPACE"
log_info "Mantener namespace: $KEEP_NAMESPACE"
log_info "Dry run: $DRY_RUN"

# Verificar conexión con AKS
log_info "Verificando conexión con AKS..."
if ! kubectl cluster-info > /dev/null 2>&1; then
    log_error "No se puede conectar al cluster AKS"
    exit 1
fi

CLUSTER_INFO=$(kubectl config current-context)
log_info "Cluster: ${CLUSTER_INFO}"

# Verificar si el namespace existe
if ! kubectl get namespace $NAMESPACE > /dev/null 2>&1; then
    log_warning "El namespace $NAMESPACE no existe"
    exit 0
fi

# Mostrar recursos existentes
log_info "Recursos existentes en $NAMESPACE:"
echo ""
kubectl get all -n $NAMESPACE 2>/dev/null || log_warning "No hay recursos en el namespace $NAMESPACE"
echo ""

# Confirmación
if [ "$FORCE" = false ] && [ "$DRY_RUN" = false ]; then
    echo ""
    log_warning "⚠️  ATENCIÓN: Esta operación eliminará TODOS los recursos del ambiente $ENVIRONMENT"
    log_warning "¿Está seguro de continuar? (escriba 'YES' para confirmar): "
    read -r response
    if [[ ! $response = "YES" ]]; then
        log_info "Limpieza cancelada"
        exit 0
    fi
fi

# Función para eliminar recursos con opción dry-run
delete_resource() {
    local resource_type=$1
    local resource_name=$2
    
    if [ "$DRY_RUN" = true ]; then
        log_info "[DRY-RUN] Eliminaría: $resource_type/$resource_name"
        return
    fi
    
    if kubectl get $resource_type $resource_name -n $NAMESPACE > /dev/null 2>&1; then
        log_info "Eliminando $resource_type/$resource_name..."
        if [ "$VERBOSE" = true ]; then
            kubectl delete $resource_type $resource_name -n $NAMESPACE
        else
            kubectl delete $resource_type $resource_name -n $NAMESPACE > /dev/null 2>&1
        fi
        log_success "$resource_type/$resource_name eliminado"
    else
        log_warning "$resource_type/$resource_name no encontrado"
    fi
}

# Función para eliminar usando manifiestos
delete_manifest() {
    local manifest_file=$1
    local temp_file="/tmp/$(basename $manifest_file)"
    
    if [ ! -f "$manifest_file" ]; then
        log_warning "Manifiesto no encontrado: $manifest_file"
        return
    fi
    
    # Reemplazar variables en el manifiesto
    sed -e "s/namespace: dev/namespace: $NAMESPACE/g" \
        "$manifest_file" > "$temp_file"
    
    if [ "$DRY_RUN" = true ]; then
        log_info "[DRY-RUN] Eliminaría recursos de: $manifest_file"
        rm "$temp_file"
        return
    fi
    
    if [ "$VERBOSE" = true ]; then
        log_info "Eliminando desde: $manifest_file"
        kubectl delete -f "$temp_file" --ignore-not-found=true
    else
        kubectl delete -f "$temp_file" --ignore-not-found=true > /dev/null 2>&1
    fi
    
    rm "$temp_file"
}

if [ "$DRY_RUN" = true ]; then
    log_info "=== MODO DRY-RUN ACTIVADO ==="
    log_info "Mostrando lo que se eliminaría sin realizar cambios reales"
    echo ""
fi

# Directorio de manifiestos
K8S_MANIFESTS_DIR="k8s-manifests"

log_info "=== ELIMINANDO SERVICIOS DE APLICACIÓN ==="

# Servicios de aplicación (en orden inverso para dependencias)
APP_SERVICES=(
    "proxy-client"
    "api-gateway"
    "favourite-service"
    "user-service"
    "shipping-service"
    "product-service"
    "payment-service"
    "order-service"
)

# Eliminar servicios de aplicación
for service in "${APP_SERVICES[@]}"; do
    log_info "Eliminando $service..."
    
    if [ -d "${K8S_MANIFESTS_DIR}/$service" ]; then
        # Usar manifiestos si están disponibles
        delete_manifest "${K8S_MANIFESTS_DIR}/$service/$service-service.yaml"
        delete_manifest "${K8S_MANIFESTS_DIR}/$service/$service-deployment.yaml"
    else
        # Eliminar directamente por nombre
        delete_resource "service" "$service"
        delete_resource "deployment" "$service"
    fi
done

log_info "=== ELIMINANDO SERVICIOS DE INFRAESTRUCTURA ==="

# Servicios de infraestructura (en orden inverso)
INFRA_SERVICES=("cloud-config" "service-discovery" "zipkin")

for service in "${INFRA_SERVICES[@]}"; do
    log_info "Eliminando $service..."
    
    if [ -d "${K8S_MANIFESTS_DIR}/$service" ]; then
        # Usar manifiestos si están disponibles
        delete_manifest "${K8S_MANIFESTS_DIR}/$service/$service-service.yaml"
        delete_manifest "${K8S_MANIFESTS_DIR}/$service/$service-deployment.yaml"
    else
        # Eliminar directamente por nombre
        delete_resource "service" "$service"
        delete_resource "deployment" "$service"
    fi
done

# Eliminar ConfigMap
log_info "=== ELIMINANDO CONFIGMAPS ==="
if [ "$DRY_RUN" = true ]; then
    log_info "[DRY-RUN] Eliminaría ConfigMaps en namespace $NAMESPACE"
else
    log_info "Eliminando ConfigMaps..."
    kubectl delete configmap --all -n $NAMESPACE > /dev/null 2>&1 || log_warning "No hay ConfigMaps para eliminar"
fi

# Eliminar recursos adicionales que puedan existir
log_info "=== LIMPIEZA ADICIONAL ==="

ADDITIONAL_RESOURCES=("secrets" "ingress" "pvc" "hpa")

for resource in "${ADDITIONAL_RESOURCES[@]}"; do
    if [ "$DRY_RUN" = true ]; then
        log_info "[DRY-RUN] Eliminaría todos los $resource en namespace $NAMESPACE"
    else
        log_info "Eliminando $resource..."
        kubectl delete $resource --all -n $NAMESPACE > /dev/null 2>&1 || true
    fi
done

# Esperar a que los pods terminen
if [ "$DRY_RUN" = false ]; then
    log_info "Esperando a que todos los pods terminen..."
    kubectl wait --for=delete pods --all -n $NAMESPACE --timeout=120s > /dev/null 2>&1 || log_warning "Timeout esperando eliminación de pods"
fi

# Eliminar namespace si se especifica
if [ "$KEEP_NAMESPACE" = false ]; then
    log_info "=== ELIMINANDO NAMESPACE ==="
    if [ "$DRY_RUN" = true ]; then
        log_info "[DRY-RUN] Eliminaría namespace: $NAMESPACE"
    else
        log_info "Eliminando namespace $NAMESPACE..."
        kubectl delete namespace $NAMESPACE
        log_success "Namespace $NAMESPACE eliminado"
    fi
else
    log_info "=== MANTENIENDO NAMESPACE ==="
    log_info "Namespace $NAMESPACE mantenido según configuración"
fi

# Resumen final
echo ""
log_info "=== RESUMEN DE LIMPIEZA ==="

if [ "$DRY_RUN" = true ]; then
    log_info "Modo DRY-RUN completado para $ENVIRONMENT"
    log_info "No se realizaron cambios reales"
else
    log_success "¡Limpieza del ambiente $ENVIRONMENT completada!"
    
    if [ "$KEEP_NAMESPACE" = true ]; then
        echo ""
        log_info "Estado actual del namespace $NAMESPACE:"
        kubectl get all -n $NAMESPACE 2>/dev/null || log_info "Namespace vacío"
    fi
fi

echo ""
log_info "=== COMANDOS ÚTILES ==="
echo "• Ver namespaces: kubectl get namespaces"
echo "• Ver recursos restantes: kubectl get all -n $NAMESPACE"
echo "• Verificar pods: kubectl get pods -n $NAMESPACE"

if [ "$DRY_RUN" = false ]; then
    log_success "Script de limpieza completado exitosamente para $ENVIRONMENT"
else
    log_info "Script de limpieza (dry-run) completado para $ENVIRONMENT"
fi