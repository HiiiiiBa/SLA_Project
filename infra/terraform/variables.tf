variable "region" {
  description = "OCI region"
  type        = string
  default     = "af-casablanca-1"
}

variable "compartment_id" {
  description = "Compartment OCID (tenancy root is OK for free-tier demos)"
  type        = string
}

variable "project_name" {
  description = "Name prefix for resources"
  type        = string
  default     = "sla-monitoring"
}

variable "vcn_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "subnet_cidr" {
  type    = string
  default = "10.0.0.0/24"
}

variable "ssh_public_key" {
  description = "SSH public key for the compute instance"
  type        = string
  default     = ""
}

variable "ssh_allowed_cidr" {
  description = "CIDR allowed to SSH"
  type        = string
  default     = "0.0.0.0/0"
}

variable "create_compute" {
  description = "Create a new compute instance (false if you already have a VM)"
  type        = bool
  default     = false
}

variable "instance_shape" {
  type    = string
  default = "VM.Standard.E5.Flex"
}

variable "instance_ocpus" {
  type    = number
  default = 1
}

variable "instance_memory_gb" {
  type    = number
  default = 12
}

variable "ubuntu_image_id" {
  description = "Ubuntu image OCID for the region (required if create_compute=true)"
  type        = string
  default     = ""
}
