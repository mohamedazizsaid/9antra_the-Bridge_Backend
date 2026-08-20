#!/bin/bash
set -e

echo "🚀 Starting deployment to Minikube..."

# 1. Namespace
kubectl apply -f k8s/namespace.yaml

# 2. ConfigMap
kubectl apply -f k8s/configmap.yaml

# 3. Secret
if [ -f "k8s/secret.yaml" ]; then
    echo "📦 Applying custom secret.yaml..."
    kubectl apply -f k8s/secret.yaml
else
    echo "⚠️ k8s/secret.yaml not found. Creating default dev secret..."
    kubectl create secret generic the-bridge-secrets -n the-bridge \
        --from-literal=DATABASE_URL="jdbc:postgresql://localhost:5432/thebridge" \
        --from-literal=DATABASE_PASSWORD="postgres_password" \
        --from-literal=JWT_SECRET="dGVzdC1zZWNyZXQta2V5LWZvci1jaS1vbmx5LW5ldmVyLXVzZS1pbi1wcm9k" \
        --from-literal=MAIL_USERNAME="test@example.com" \
        --from-literal=MAIL_PASSWORD="password" \
        --from-literal=CLOUDINARY_CLOUD_NAME="demo" \
        --from-literal=CLOUDINARY_API_KEY="123456" \
        --from-literal=CLOUDINARY_API_SECRET="secret" \
        --from-literal=GOOGLE_CLIENT_ID="dummy" \
        --from-literal=GOOGLE_CLIENT_SECRET="dummy" \
        --from-literal=FACEBOOK_CLIENT_ID="dummy" \
        --from-literal=FACEBOOK_CLIENT_SECRET="dummy" \
        --from-literal=STRIPE_SECRET_KEY="sk_test_123" \
        --from-literal=STRIPE_PUBLISHABLE_KEY="pk_test_123" \
        --from-literal=STRIPE_WEBHOOK_SECRET="whsec_123" \
        --from-literal=BLOCKCHAIN_PRIVATE_KEY="0x0" \
        --from-literal=SPRING_DATA_REDIS_PASSWORD="" \
        --dry-run=client -o yaml | kubectl apply -f -
fi

# 4. Deployment & Service
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# 5. Monitoring
echo "📊 Applying Monitoring (Prometheus + Grafana)..."
kubectl apply -f k8s/monitoring/prometheus-configmap.yaml
kubectl apply -f k8s/monitoring/prometheus-deployment.yaml
kubectl apply -f k8s/monitoring/grafana-deployment.yaml

echo "✅ Minikube resources applied successfully!"
echo "ℹ️ Access app: minikube service the-bridge-service -n the-bridge"
echo "ℹ️ Access Grafana: minikube service grafana-service -n the-bridge"
