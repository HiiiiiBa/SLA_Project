#!/bin/bash
set -euo pipefail
HDR="Authorization: Bearer Oracle"
echo "==== VNIC details ===="
curl -sS -m 5 -H "$HDR" http://169.254.169.254/opc/v2/vnics/ | python3 -m json.tool
echo "==== Try install OCI CLI quickly? ===="
# Check security list via metadata is not available; show current public tests
echo "==== Listening / counters ===="
sudo ss -lntp | grep -E ':80|:8000' || true
sudo iptables -L INPUT -n -v --line-numbers | grep -E 'dpt:80|dpt:8000|REJECT|dpt:22'
