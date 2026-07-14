# Infrastructure as Code — SLA Monitoring

| Tool | Responsibility |
|------|----------------|
| **Terraform** (`infra/terraform`) | OCI network: VCN, subnet, Security List 22/80/443, optional VM |
| **Ansible** (`infra/ansible`) | Ubuntu host: packages, iptables, K3s health checks |

```
GitHub push → CD builds images → Ansible/K8s node pulls & restarts
                 ↑
        Terraform (cloud ports / network)
```

## Quick start

### Terraform

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
terraform init && terraform plan
```

### Ansible

```bash
cd infra/ansible
cp inventory/hosts.ini.example inventory/hosts.ini
ansible-playbook playbooks/site.yml
```

## Current production

- Host: `84.8.216.210` (`sla-monitoring-server-prod`)
- Region: `af-casablanca-1`
- Stack: K3s + Traefik + Prometheus/Grafana/Loki
