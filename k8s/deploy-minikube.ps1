# ──────────────────────────────────────────────────────────────────────────────
# Deploy The Bridge to Minikube (PowerShell)
# ──────────────────────────────────────────────────────────────────────────────
Write-Host "🚀 Starting deployment to Minikube..." -ForegroundColor Cyan

# 1. Ensure Namespace exists
kubectl apply -f k8s/namespace.yaml

# 2. Apply ConfigMap
kubectl apply -f k8s/configmap.yaml

# 3. Apply Secret (use secret.yaml if exists, otherwise create dummy for testing)
if (Test-Path "k8s/secret.yaml") {
    Write-Host "📦 Applying custom secret.yaml..." -ForegroundColor Green
    kubectl apply -f k8s/secret.yaml
} else {
    Write-Host "⚠️ k8s/secret.yaml not found. Creating default development secret in namespace 'the-bridge'..." -ForegroundColor Yellow
    kubectl create secret generic the-bridge-secrets -n the-bridge `
        --from-literal=DATABASE_URL="jdbc:postgresql://localhost:5432/thebridge" `
        --from-literal=DATABASE_PASSWORD="postgres_password" `
        --from-literal=JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1jaS1vbmx5LW5ldmVyLXVzZS1pbi1wcm9k" `
        --from-literal=MAIL_USERNAME="test@example.com" `
        --from-literal=MAIL_PASSWORD="password" `
        --from-literal=CLOUDINARY_CLOUD_NAME="demo" `
        --from-literal=CLOUDINARY_API_KEY="123456" `
        --from-literal=CLOUDINARY_API_SECRET="secret" `
        --from-literal=GOOGLE_CLIENT_ID="dummy" `
        --from-literal=GOOGLE_CLIENT_SECRET="dummy" `
        --from-literal=FACEBOOK_CLIENT_ID="dummy" `
        --from-literal=FACEBOOK_CLIENT_SECRET="dummy" `
        --from-literal=STRIPE_SECRET_KEY="sk_test_123" `
        --from-literal=STRIPE_PUBLISHABLE_KEY="pk_test_123" `
        --from-literal=STRIPE_WEBHOOK_SECRET="whsec_123" `
        --from-literal=BLOCKCHAIN_PRIVATE_KEY="0x0" `
        --from-literal=SPRING_DATA_REDIS_PASSWORD="" `
        --dry-run=client -o yaml | kubectl apply -f -
}

# 4. Apply Application Deployment & Service
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 5. Apply Prometheus & Grafana Monitoring
Write-Host "📊 Applying Prometheus & Grafana monitoring stack..." -ForegroundColor Cyan
kubectl apply -f k8s/monitoring/prometheus-configmap.yaml
kubectl apply -f k8s/monitoring/prometheus-deployment.yaml
kubectl apply -f k8s/monitoring/grafana-deployment.yaml

Write-Host "✅ Minikube resources applied successfully!" -ForegroundColor Green
Write-Host "ℹ️ Run 'minikube service the-bridge-service -n the-bridge' to access the Backend." -ForegroundColor Yellow
Write-Host "ℹ️ Run 'minikube service grafana-service -n the-bridge' to access Grafana." -ForegroundColor Yellow
