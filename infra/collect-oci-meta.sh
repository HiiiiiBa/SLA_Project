#!/bin/bash
curl -sS -m 5 -H "Authorization: Bearer Oracle" http://169.254.169.254/opc/v2/instance/ > /tmp/oci-instance.json
curl -sS -m 5 -H "Authorization: Bearer Oracle" http://169.254.169.254/opc/v2/vnics/ > /tmp/oci-vnics.json
python3 <<'PY'
import json
d=json.load(open("/tmp/oci-instance.json"))
v=json.load(open("/tmp/oci-vnics.json"))[0]
print("region="+d.get("canonicalRegionName",""))
print("compartment_id="+d.get("compartmentId",""))
print("instance_id="+d.get("id",""))
print("display_name="+d.get("displayName",""))
print("shape="+d.get("shape",""))
print("private_ip="+v.get("privateIp",""))
print("subnet_cidr="+v.get("subnetCidrBlock",""))
print("vnic_id="+v.get("vnicId",""))
PY
