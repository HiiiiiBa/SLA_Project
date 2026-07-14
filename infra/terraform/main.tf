locals {
  name = var.project_name
}

resource "oci_core_vcn" "main" {
  compartment_id = var.compartment_id
  display_name   = "${local.name}-vcn"
  cidr_blocks    = [var.vcn_cidr]
  dns_label      = replace(local.name, "-", "")
}

resource "oci_core_internet_gateway" "igw" {
  compartment_id = var.compartment_id
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${local.name}-igw"
  enabled        = true
}

resource "oci_core_route_table" "public" {
  compartment_id = var.compartment_id
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${local.name}-rt-public"

  route_rules {
    network_entity_id = oci_core_internet_gateway.igw.id
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
  }
}

resource "oci_core_security_list" "public" {
  compartment_id = var.compartment_id
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${local.name}-sl-public"

  egress_security_rules {
    protocol    = "all"
    destination = "0.0.0.0/0"
    description = "Allow all egress"
  }

  # SSH
  ingress_security_rules {
    protocol    = "6"
    source      = var.ssh_allowed_cidr
    description = "SSH"
    tcp_options {
      min = 22
      max = 22
    }
  }

  # HTTP (Traefik / app)
  ingress_security_rules {
    protocol    = "6"
    source      = "0.0.0.0/0"
    description = "HTTP"
    tcp_options {
      min = 80
      max = 80
    }
  }

  # HTTPS
  ingress_security_rules {
    protocol    = "6"
    source      = "0.0.0.0/0"
    description = "HTTPS"
    tcp_options {
      min = 443
      max = 443
    }
  }

  # ICMP (ping / path MTU)
  ingress_security_rules {
    protocol    = "1"
    source      = "0.0.0.0/0"
    description = "ICMP"
  }
}

resource "oci_core_subnet" "public" {
  compartment_id             = var.compartment_id
  vcn_id                     = oci_core_vcn.main.id
  display_name               = "${local.name}-subnet-public"
  cidr_block                 = var.subnet_cidr
  route_table_id             = oci_core_route_table.public.id
  security_list_ids          = [oci_core_security_list.public.id]
  prohibit_public_ip_on_vnic = false
  dns_label                  = "public"
}

resource "oci_core_instance" "app" {
  count = var.create_compute ? 1 : 0

  compartment_id      = var.compartment_id
  availability_domain = data.oci_identity_availability_domains.ads.availability_domains[0].name
  display_name        = "${local.name}-server"
  shape               = var.instance_shape

  shape_config {
    ocpus         = var.instance_ocpus
    memory_in_gbs = var.instance_memory_gb
  }

  create_vnic_details {
    subnet_id        = oci_core_subnet.public.id
    assign_public_ip = true
    display_name     = "${local.name}-vnic"
  }

  source_details {
    source_type = "image"
    source_id   = var.ubuntu_image_id
  }

  metadata = {
    ssh_authorized_keys = var.ssh_public_key
  }

  lifecycle {
    precondition {
      condition     = var.ubuntu_image_id != "" && var.ssh_public_key != ""
      error_message = "ubuntu_image_id and ssh_public_key are required when create_compute=true"
    }
  }
}

data "oci_identity_availability_domains" "ads" {
  compartment_id = var.compartment_id
}
