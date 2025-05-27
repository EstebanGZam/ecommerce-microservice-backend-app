#!/usr/bin/env bash
# service-health-monitor.sh - Verifica el estado de múltiples endpoints HTTP/HTTPS

declare -A services=(
  ["Productos"]="http://localhost:8080/product-service/api/products"
  ["Usuarios"]="http://localhost:8080/user-service/api/users"
  ["Pagos"]="http://localhost:8080/payment-service/api/payments"
  ["Favoritos"]="http://localhost:8080/favourite-service/api/favourites"
  ["Envíos"]="http://localhost:8080/shipping-service/api/shippings"
  ["Catálogo"]="http://localhost:8080/app/api/products"
)

function check_status() {
  local response_code=$(curl -sLk -o /dev/null -w "%{http_code}" \
    --connect-timeout 5 \
    --max-time 10 \
    -H "Accept: application/json" \
    "$1")

  printf "%-35s | Código: %3d | " "$2" "$response_code"
  
  if [[ $response_code -ge 200 && $response_code -lt 300 ]]; then
    printf "\e[32mOperacional\e[0m\n"
  elif [[ $response_code -ge 500 ]]; then
    printf "\e[31mError del Servidor\e[0m\n"
  elif [[ $response_code -ge 400 ]]; then
    printf "\e[33mSolicitud Incorrecta\e[0m\n"
  else
    printf "\e[35mEstado Desconocido\e[0m\n"
  fi
}

echo "══════════════════════════════════════════════════"
echo "   Monitor de Salud de Servicios - $(date +'%H:%M:%S')   "
echo "══════════════════════════════════════════════════"

for service_name in "${!services[@]}"; do
  check_status "${services[$service_name]}" "$service_name"
done

echo "══════════════════════════════════════════════════"