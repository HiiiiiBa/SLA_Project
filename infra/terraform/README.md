# Terraform — OCI infrastructure for SLA Monitoring

## Goal

Declare the cloud network required by the project:

- VCN + public subnet
- Internet Gateway + route table
- Security List (**22 / 80 / 443**) — the rules that unblocked `sla-monitoring.local`
- Optional compute instance (disabled by default because prod already exists)

## Prerequisites

1. [Terraform](https://developer.hashicorp.com/terraform/install) >= 1.5
2. [OCI CLI / API key](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/apisigningkey.htm) configured in `~/.oci/config`
3. Compartment OCID

## Usage

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars
# edit compartment_id

terraform init
terraform plan
terraform apply
```

## Existing production VM

Your live server is already created:

| Field | Value |
|-------|-------|
| Name | `sla-monitoring-server-prod` |
| Region | `af-casablanca-1` |
| Public IP | `84.8.216.210` |
| Private IP | `10.0.0.247` |

Keep `create_compute = false` and use Terraform mainly for **network + Security List** documentation / reproducibility.

To attach Terraform to an existing Security List later, use `terraform import`.

## Why this matters (DevOps demo)

- Infra as Code (reproducible cloud)
- Security List as code (ports HTTP/HTTPS)
- Clear split: **Terraform = cloud**, **Ansible = OS/K3s config**
