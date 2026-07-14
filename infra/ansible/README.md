# Ansible — configure & verify the SLA Monitoring server

## Goal

Automate **OS / host** operations on the existing Ubuntu + K3s node:

1. Base packages
2. Host firewall rules (80/443/Traefik)
3. Kubernetes health checks (deployments, IngressRoutes)

Terraform manages cloud networking. Ansible manages the server.

## Prerequisites

```bash
# Windows (example with pip)
pip install ansible

# Or WSL / Linux
sudo apt install ansible
```

SSH key used in this project:

`C:\Users\HP\.ssh\ssh-key-2026-07-09.key`

## Setup inventory

```bash
cd infra/ansible
cp inventory/hosts.ini.example inventory/hosts.ini
# edit ansible_ssh_private_key_file if needed
```

Example `hosts.ini` override:

```ini
[sla_prod]
sla-monitoring-server-prod ansible_host=84.8.216.210 ansible_user=ubuntu ansible_ssh_private_key_file=C:\Users\HP\.ssh\ssh-key-2026-07-09.key
```

## Run

```bash
# Full check
ansible-playbook playbooks/site.yml

# Only firewall
ansible-playbook playbooks/02-firewall.yml

# Only K3s health
ansible-playbook playbooks/03-k3s-check.yml
```

## What a successful run proves (demo)

- Idempotent host configuration
- Ports required by Traefik are open
- K3s is healthy and SLA deployments are Ready
- Clear separation Terraform (cloud) / Ansible (node)
