#!/bin/bash
curl -sS -m 8 -H "Host: sla-monitoring.local" -H "Content-Type: application/json" \
  -X POST "http://127.0.0.1:8000/api/auth/login" \
  -d '{"email":"admin@sla.com","password":"Admin123!"}'
echo
