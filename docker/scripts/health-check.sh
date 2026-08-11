#!/usr/bin/env bash
# IndianRoadmap — Backend Health Check Script
# Polls all service health endpoints and exits non-zero if any are unhealthy.
#
# Usage:
#   ./docker/scripts/health-check.sh
#
# Requirements: curl
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"

readonly SERVICES=(
  "API Gateway|${GATEWAY_URL}/actuator/health"
  "destination-service|http://localhost:8081/actuator/health"
  "roadmap-service|http://localhost:8082/actuator/health"
  "story-service|http://localhost:8083/actuator/health"
  "audio-service|http://localhost:8084/actuator/health"
  "recommendation-service|http://localhost:8085/actuator/health"
  "user-service|http://localhost:8086/actuator/health"
)

MAX_WAIT="${MAX_WAIT:-120}"
INTERVAL=5
elapsed=0
all_healthy=false

echo "IndianRoadmap health check — waiting up to ${MAX_WAIT}s..."

while [[ $elapsed -lt $MAX_WAIT ]]; do
  all_healthy=true
  for entry in "${SERVICES[@]}"; do
    name="${entry%%|*}"
    url="${entry##*|}"
    if ! curl -sf "$url" | grep -q '"status":"UP"' 2>/dev/null; then
      echo "  [WAITING] $name ($url)"
      all_healthy=false
    fi
  done

  if $all_healthy; then
    break
  fi

  sleep "$INTERVAL"
  elapsed=$((elapsed + INTERVAL))
done

echo ""
echo "═══════════════════════════════════════"
failed=0
for entry in "${SERVICES[@]}"; do
  name="${entry%%|*}"
  url="${entry##*|}"
  response=$(curl -sf "$url" 2>/dev/null || echo '{}')
  status=$(echo "$response" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "DOWN")
  if [[ "$status" == "UP" ]]; then
    echo "  ✓  $name"
  else
    echo "  ✗  $name  [$status]"
    failed=$((failed + 1))
  fi
done
echo "═══════════════════════════════════════"

if [[ $failed -gt 0 ]]; then
  echo ""
  echo "ERROR: $failed service(s) are not healthy."
  exit 1
fi

echo ""
echo "All services are healthy."
echo "Gateway: ${GATEWAY_URL}"
