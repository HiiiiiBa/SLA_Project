#!/bin/bash
set -euo pipefail
HDR1="Authorization: Bearer Oracle"
echo "==== INSTANCE ===="
curl -sS -m 5 -H "$HDR1" http://169.254.169.254/opc/v2/instance/ | python3 -m json.tool | head -100
echo "==== VNICS ===="
curl -sS -m 5 -H "$HDR1" http://169.254.169.254/opc/v2/vnics/ | python3 -m json.tool
