provider "oci" {
  region = var.region
  # Auth options (pick one):
  # 1) ~/.oci/config API key
  # 2) export OCI_CLI_AUTH=security_token / instance_principal on a bastion
  # See README.md
}
