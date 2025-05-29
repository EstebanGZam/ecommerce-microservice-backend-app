#!/bin/bash

# Script para verificar recursos del cluster antes del despliegue

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m' 
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

echo "=== VERIFICACIÓN DE RECURSOS DEL CLUSTER ==="

# Información del cluster
log_info "Información del cluster:"
kubectl cluster-info --context=$(kubectl config current-context) | head -1

echo ""
log_info "Nodos del cluster:"
kubectl get nodes -o wide

echo ""
log_info "Recursos por nodo:"
kubectl describe nodes | grep -A 5 "Allocated resources"

echo ""
log_info "Capacidad total del cluster:"
echo "CPU:"
kubectl get nodes -o jsonpath='{range .items[*]}{.status.capacity.cpu}{"\n"}{end}' | awk '{sum+=$1} END {print "  Total: " sum " vCPUs"}'

echo "Memory:"
kubectl get nodes -o jsonpath='{range .items[*]}{.status.capacity.memory}{"\n"}{end}' | awk '{gsub(/Ki/, ""); sum+=$1/1024/1024} END {printf "  Total: %.1f GB\n", sum}'

echo ""
log_info "Recursos disponibles (allocatable):"
echo "CPU:"
kubectl get nodes -o jsonpath='{range .items[*]}{.status.allocatable.cpu}{"\n"}{end}' | awk '{sum+=$1} END {print "  Disponible: " sum " vCPUs"}'

echo "Memory:"
kubectl get nodes -o jsonpath='{range .items[*]}{.status.allocatable.memory}{"\n"}{end}' | awk '{gsub(/Ki/, ""); sum+=$1/1024/1024} END {printf "  Disponible: %.1f GB\n", sum}'

echo ""
log_info "Uso actual de recursos:"
kubectl top nodes 2>/dev/null || log_warning "Metrics server no disponible - instalar con: kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml"

echo ""
log_info "Pods por namespace:"
kubectl get pods --all-namespaces --field-selector=status.phase=Running | awk '{print $1}' | sort | uniq -c | sort -nr

echo ""
log_info "Eventos recientes del cluster:"
kubectl get events --all-namespaces --sort-by='.lastTimestamp' | tail -10

echo ""
log_info "Verificando pods en estado Pending:"
PENDING_PODS=$(kubectl get pods --all-namespaces --field-selector=status.phase=Pending --no-headers 2>/dev/null | wc -l)
if [ $PENDING_PODS -gt 0 ]; then
    log_warning "Encontrados $PENDING_PODS pods en estado Pending:"
    kubectl get pods --all-namespaces --field-selector=status.phase=Pending
    echo ""
    log_info "Razones de pods Pending:"
    kubectl describe pods --all-namespaces --field-selector=status.phase=Pending | grep -A 10 "Events:"
else
    log_success "No hay pods en estado Pending"
fi

echo ""
log_info "=== RECOMENDACIONES ==="

# Calcular recursos necesarios estimados para microservicios
ESTIMATED_CPU_NEEDED="2.0"  # 10 servicios × 0.2 CPU promedio
ESTIMATED_MEMORY_NEEDED="4.0"  # 10 servicios × 400MB promedio

AVAILABLE_CPU=$(kubectl get nodes -o jsonpath='{range .items[*]}{.status.allocatable.cpu}{"\n"}{end}' | awk '{sum+=$1} END {print sum}')
AVAILABLE_MEMORY=$(kubectl get nodes -o jsonpath='{range .items[*]}{.status.allocatable.memory}{"\n"}{end}' | awk '{gsub(/Ki/, ""); sum+=$1/1024/1024} END {print sum}')

echo "Recursos estimados necesarios para microservicios:"
echo "  CPU: ${ESTIMATED_CPU_NEEDED} vCPUs"
echo "  Memory: ${ESTIMATED_MEMORY_NEEDED} GB"
echo ""
echo "Recursos disponibles:"
echo "  CPU: ${AVAILABLE_CPU} vCPUs"
echo "  Memory: ${AVAILABLE_MEMORY} GB"

# Verificar si hay suficientes recursos
if (( $(echo "$AVAILABLE_CPU < $ESTIMATED_CPU_NEEDED" | bc -l) )); then
    log_error "CPU insuficiente! Considerar:"
    echo "  • Reducir recursos solicitados en deployments"
    echo "  • Escalar cluster: az aks scale --node-count 3"
    echo "  • Usar VMs más grandes: Standard_DS3_v2 (4 vCPUs)"
else
    log_success "CPU suficiente para el despliegue"
fi

if (( $(echo "$AVAILABLE_MEMORY < $ESTIMATED_MEMORY_NEEDED" | bc -l) )); then
    log_error "Memoria insuficiente! Considerar las mismas acciones que para CPU"
else
    log_success "Memoria suficiente para el despliegue"
fi

echo ""
log_info "Comandos útiles:"
echo "  • Escalar cluster: az aks scale --resource-group <rg> --name <cluster> --node-count 3"
echo "  • Ver uso en tiempo real: kubectl top nodes"
echo "  • Ver uso por pods: kubectl top pods --all-namespaces"