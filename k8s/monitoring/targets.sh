#!/bin/bash
curl -sS -m 8 --header "Host: prometheus.sla-monitoring.local" \
  "http://127.0.0.1:8000/api/v1/targets" | python3 -c 'import json,sys; d=json.load(sys.stdin); print(len(d["data"]["activeTargets"]),"targets");
[print(t["labels"].get("job"), t["labels"].get("pod",""), t["health"], (t.get("lastError") or "")[:150]) for t in d["data"]["activeTargets"]]'
