output "vcn_id" {
  value = oci_core_vcn.main.id
}

output "subnet_id" {
  value = oci_core_subnet.public.id
}

output "security_list_id" {
  value = oci_core_security_list.public.id
}

output "instance_public_ip" {
  value       = try(oci_core_instance.app[0].public_ip, null)
  description = "Public IP when create_compute=true"
}

output "existing_prod_hint" {
  value = "Current prod VM: sla-monitoring-server-prod @ 84.8.216.210 (import or manage Security List only)"
}
