# Diagnostic et correction Traefik — sla-monitoring.local
#
# CAUSE RÉELLE (corrigée le 2026-07-14) :
# Traefik tourne en hostNetwork=true mais ne pouvait PAS joindre l'API
# Kubernetes via le ClusterIP 10.43.0.1:443 (timeout).
# Les providers kubernetesingress / kubernetescrd démarraient puis restaient bloqués :
# aucune route Ingress/IngressRoute n'était jamais chargée → 404 Traefik.
#
# FIX appliqué sur le cluster :
#   KUBERNETES_SERVICE_HOST=127.0.0.1
#   KUBERNETES_SERVICE_PORT=6443
#   dnsPolicy=ClusterFirstWithHostNet
# Persisté via : /var/lib/rancher/k3s/server/manifests/traefik-api-host.yaml
#
# Autres causes possibles de "404 page not found" Traefik :
# Host manquant, mauvaise IngressClass, entrypoint web non associé,
# provider kubernetesingress désactivé / CRD IngressRoute manquante.

## 1) Diagnostic (à exécuter sur le serveur / avec kubeconfig valide)

```bash
# IngressClass
kubectl get ingressclass
kubectl describe ingressclass traefik

# Ingress actuel
kubectl get ingress -n sla-monitoring -o yaml
kubectl describe ingress -n sla-monitoring

# CRD Traefik présentes ?
kubectl get crd | grep -i traefik

# IngressRoutes existantes
kubectl get ingressroute -A
kubectl get ingressroute -n sla-monitoring -o yaml

# Service + endpoints frontend
kubectl get svc,endpoints,ep -n sla-monitoring frontend -o wide
kubectl get pods -n sla-monitoring -l app=frontend -o wide

# Où tourne Traefik + ses args (provider kubernetesingress / entrypoints)
kubectl get pods -A | grep -i traefik
TRAEFIK_NS=$(kubectl get pods -A | awk '/traefik/ {print $1; exit}')
TRAEFIK_POD=$(kubectl get pods -n "$TRAEFIK_NS" -l app.kubernetes.io/name=traefik -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
# Fallback labels courants (k3s / helm)
[ -z "$TRAEFIK_POD" ] && TRAEFIK_POD=$(kubectl get pods -n "$TRAEFIK_NS" --no-headers 2>/dev/null | awk '/traefik/ {print $1; exit}')
kubectl -n "$TRAEFIK_NS" get pod "$TRAEFIK_POD" -o yaml | grep -E 'providers|entryPoints|ingressClass|kubernetesingress|kubernetescrd' -i
kubectl -n "$TRAEFIK_NS" logs "$TRAEFIK_POD" --tail=100

# Service LB Traefik (port 80 → targetPort 8000 = entrypoint web)
kubectl get svc -A | grep -i traefik
kubectl get svc -n "$TRAEFIK_NS" -o yaml | grep -A20 -E 'ports:|type:'
```

## 2) Test Host header (critique)

Sans le Host `sla-monitoring.local`, Traefik répond toujours `404 page not found`.

```bash
# ÉCHEC typique (IP seule, pas de Host match) :
curl -v http://84.8.216.210/

# SUCCÈS attendu après correction :
curl -v -H "Host: sla-monitoring.local" http://84.8.216.210/

# Ou via /etc/hosts :
# 84.8.216.210  sla-monitoring.local
curl -v http://sla-monitoring.local/
```

## 3) Appliquer la correction recommandée (IngressRoute)

```bash
# Supprimer l'Ingress standard qui ne crée pas de route utilisable
kubectl delete ingress frontend -n sla-monitoring --ignore-not-found

# Appliquer IngressRoute Traefik native (entrypoint web explicite)
kubectl apply -f k8s/traefik/ingressroute-frontend.yaml

# Vérifier
kubectl get ingressroute -n sla-monitoring
kubectl describe ingressroute frontend -n sla-monitoring

# Test
curl -v -H "Host: sla-monitoring.local" http://84.8.216.210/
```

## 4) Alternative : Ingress Kubernetes corrigé

```bash
kubectl apply -f k8s/traefik/ingress-frontend.yaml
curl -v -H "Host: sla-monitoring.local" http://84.8.216.210/
```

## 5) Si IngressRoute est rejeté (CRD absente)

```bash
kubectl api-resources | grep -i ingressroute
# Si vide → le chart Traefik n'a pas installé les CRD, ou mauvais groupe API.
# Traefik v3 utilise : traefik.io/v1alpha1
# Ancien groupe : traefik.containo.us/v1alpha1
```

Pour l'ancien groupe API, remplacez dans le YAML :
`apiVersion: traefik.io/v1alpha1` → `apiVersion: traefik.containo.us/v1alpha1`

## 6) Checklist causes fréquentes

| Symptôme | Cause | Fix |
|----------|--------|-----|
| 404 Traefik sur IP seule | Pas de Host match | Utiliser Host `sla-monitoring.local` |
| 404 malgré bon Host | Entrypoint non câblé | `entryPoints: [web]` ou annotation router.entrypoints |
| Ingress existe mais pas de route | provider kubernetesingress off / mauvaise class | Vérifier args Traefik + ingressClassName: traefik |
| IngressRoute apply échoue | CRD manquante / mauvais apiVersion | Installer CRD Traefik v3 ou changer apiVersion |
| 502/504 (pas 404) | Backend down | Service/endpoints frontend |

Le message exact `404 page not found` (texte brut Traefik) = **aucun router**.
Un 404 HTML Next.js = routage OK, problème appli.
