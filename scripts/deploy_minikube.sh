#!/bin/bash

# Asegúrate de que Minikube esté en ejecución antes de correr este script.
# Ejemplo: minikube start

# Define la ruta base para los manifiestos de Kubernetes
K8S_MANIFESTS_DIR="k8s-manifests"

echo "Aplicando el Namespace..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/ecommerce-namespace.yaml"

echo "Esperando a que el namespace 'ecommerce-ns' esté creado..."
RETRY_COUNT=0
MAX_RETRIES=30 # Esperar un máximo de 30 segundos (30 * 1 segundo)
NAMESPACE_READY=false
until ${NAMESPACE_READY} || [ ${RETRY_COUNT} -eq ${MAX_RETRIES} ]; do
    if kubectl get namespace ecommerce-ns > /dev/null 2>&1; then
        echo "Namespace 'ecommerce-ns' encontrado."
        NAMESPACE_READY=true
    else
        echo "Namespace 'ecommerce-ns' aún no encontrado, reintentando en 1 segundo..."
        sleep 1
        RETRY_COUNT=$((RETRY_COUNT + 1))
    fi
done

if ! ${NAMESPACE_READY}; then
    echo "Error: El namespace 'ecommerce-ns' no se pudo crear después de ${MAX_RETRIES} segundos. Abortando."
    exit 1
fi

echo "Aplicando el ConfigMap común..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/common-env-configmap.yaml"

echo "Desplegando Zipkin..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/zipkin/zipkin-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/zipkin/zipkin-service.yaml"

echo "Esperando a que Zipkin esté listo..."
kubectl rollout status deployment/zipkin -n ecommerce-ns --timeout=120s

echo "Desplegando Service Discovery (Eureka)..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/service-discovery/service-discovery-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/service-discovery/service-discovery-service.yaml"

echo "Esperando a que Service Discovery esté listo..."
kubectl rollout status deployment/service-discovery -n ecommerce-ns --timeout=180s

echo "Desplegando Cloud Config Server..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/cloud-config/cloud-config-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/cloud-config/cloud-config-service.yaml"

echo "Esperando a que Cloud Config Server esté listo..."
kubectl rollout status deployment/cloud-config -n ecommerce-ns --timeout=120s

echo "Desplegando API Gateway..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/api-gateway/api-gateway-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/api-gateway/api-gateway-service.yaml"

echo "Desplegando Order Service..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/order-service/order-service-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/order-service/order-service-service.yaml"

echo "Desplegando Payment Service..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/payment-service/payment-service-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/payment-service/payment-service-service.yaml"

echo "Desplegando Product Service..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/product-service/product-service-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/product-service/product-service-service.yaml"

echo "Desplegando Shipping Service..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/shipping-service/shipping-service-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/shipping-service/shipping-service-service.yaml"

echo "Desplegando User Service..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/user-service/user-service-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/user-service/user-service-service.yaml"

echo "Desplegando Favourite Service..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/favourite-service/favourite-service-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/favourite-service/favourite-service-service.yaml"

echo "Desplegando Proxy Client..."
kubectl apply -f "${K8S_MANIFESTS_DIR}/proxy-client/proxy-client-deployment.yaml"
kubectl apply -f "${K8S_MANIFESTS_DIR}/proxy-client/proxy-client-service.yaml"

echo "Esperando a que todos los despliegues principales estén listos..."
# Puedes agregar más comandos rollout status para los otros servicios si es necesario,
# o verificar el estado general en el namespace.
kubectl rollout status deployment/api-gateway -n ecommerce-ns --timeout=180s
kubectl rollout status deployment/order-service -n ecommerce-ns --timeout=180s
# ... y así sucesivamente para los demás servicios si quieres esperas individuales.

echo ""
echo "Todos los microservicios han sido aplicados."
echo "Verifica el estado de los pods con: kubectl get pods -n ecommerce-ns"
echo "Verifica el estado de los services con: kubectl get services -n ecommerce-ns"
echo "Si usas Minikube, puedes necesitar 'minikube tunnel' en otra terminal para acceder a los LoadBalancers." 