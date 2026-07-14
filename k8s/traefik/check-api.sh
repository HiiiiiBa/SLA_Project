#!/bin/bash
set -euo pipefail
echo "==== baked strings in frontend image ===="
sudo kubectl exec -n sla-monitoring deploy/frontend -- sh -c 'find /app -name "*.js" 2>/dev/null | head -5; grep -Roh "http[^\"]*8080\|localhost:8080\|84.8.216.210" /app/.next 2>/dev/null | sort -u | head -30'
echo "==== login from browser perspective (via traefik host) ===="
curl -sS -m 5 -o /dev/null -w "frontend_login_page=%{http_code}\n" -H "Host: sla-monitoring.local" http://127.0.0.1:8000/login
curl -sS -m 5 -o /dev/null -w "backend_direct=%{http_code}\n" http://10.43.13.246:8080/api/auth/login -X POST -H 'Content-Type: application/json' -d '{}' || true
curl -sS -m 5 -w "\npublic8080=%{http_code}\n" http://127.0.0.1:8080/api/auth/login -X POST -H 'Content-Type: application/json' -d '{}' | head -c 200; echo
