#!/bin/bash
set -euo pipefail

echo "==== OPEN PORTS 80/443 IN IPTABLES (before REJECT) ===="
# Insert ACCEPT rules just before the final REJECT
REJECT_LINE=$(sudo iptables -L INPUT -n --line-numbers | awk '/REJECT/ {print $1; exit}')
if [ -z "${REJECT_LINE:-}" ]; then
  sudo iptables -A INPUT -p tcp --dport 80 -j ACCEPT
  sudo iptables -A INPUT -p tcp --dport 443 -j ACCEPT
else
  sudo iptables -I INPUT "$REJECT_LINE" -p tcp -m state --state NEW -m tcp --dport 80 -j ACCEPT -m comment --comment "sla-monitoring http"
  sudo iptables -I INPUT "$REJECT_LINE" -p tcp -m state --state NEW -m tcp --dport 443 -j ACCEPT -m comment --comment "sla-monitoring https"
  # also allow traefik hostNetwork ports in case DNAT lands on them
  sudo iptables -I INPUT "$REJECT_LINE" -p tcp -m state --state NEW -m tcp --dport 8000 -j ACCEPT -m comment --comment "traefik web"
  sudo iptables -I INPUT "$REJECT_LINE" -p tcp -m state --state NEW -m tcp --dport 8443 -j ACCEPT -m comment --comment "traefik websecure"
fi

echo "==== INPUT RULES ===="
sudo iptables -L INPUT -n -v --line-numbers | head -25

echo "==== PERSIST IPTABLES ===="
if command -v netfilter-persistent >/dev/null 2>&1; then
  sudo netfilter-persistent save
elif [ -d /etc/iptables ]; then
  sudo sh -c 'iptables-save > /etc/iptables/rules.v4'
elif command -v iptables-save >/dev/null 2>&1; then
  sudo sh -c 'iptables-save > /etc/iptables.rules'
  # ensure restore on boot
  sudo tee /etc/network/if-pre-up.d/iptables >/dev/null <<'EOF' || true
#!/bin/sh
iptables-restore < /etc/iptables.rules
EOF
  sudo chmod +x /etc/network/if-pre-up.d/iptables 2>/dev/null || true
fi
# Ubuntu sometimes uses iptables-persistent package path
sudo sh -c 'iptables-save > /etc/iptables/rules.v4' 2>/dev/null || sudo mkdir -p /etc/iptables && sudo sh -c 'iptables-save > /etc/iptables/rules.v4'
echo "saved"

echo "==== LOCAL VERIFY ===="
curl -sS -o /dev/null -w "lo80=%{http_code}\n" --header "Host: sla-monitoring.local" http://127.0.0.1/
curl -sS -o /dev/null -w "extip=%{http_code}\n" --header "Host: sla-monitoring.local" http://84.8.216.210/ || true

# Detect OCI and hint
if curl -sS -m 2 -H "Authorization: Bearer Oracle" http://169.254.169.254/opc/v2/instance/shape 2>/dev/null | grep -q .; then
  echo "CLOUD=OCI — ouvrez aussi le Security List / NSG : TCP 80 et 443 Ingress 0.0.0.0/0"
fi
