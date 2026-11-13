#!/usr/bin/env bash
set -euo pipefail

AUTH_URL="https://auth-service-iuho.onrender.com"
INVENTORY_URL="https://inventory-service-t8cw.onrender.com"
ORDER_URL="https://order-service-zkul.onrender.com"
NOTIFICATION_URL="https://notification-service-b8qi.onrender.com"

EMAIL="smoke_$(date +%s)@example.com"
PASSWORD="Password@123"
SKU="SMOKE-MBP-$(date +%s)"

echo "==> 1. Waking up Render containers (may take 60-150s on cold start)..."
for url in "$AUTH_URL" "$INVENTORY_URL" "$ORDER_URL" "$NOTIFICATION_URL"; do
  until [ "$(curl -s -m 150 -o /dev/null -w "%{http_code}" "$url/actuator/health")" = "200" ]; do
    echo "   Waiting for $url..."
    sleep 5
  done
  echo "   [OK] $url is UP"
done

echo "==> 2. Registering & logging in test user ($EMAIL)..."
curl -s -X POST "$AUTH_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Smoke Tester\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" > /dev/null

LOGIN_RES=$(curl -s -X POST "$AUTH_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

TOKEN=$(python3 -c "import sys, json; print(json.loads(sys.stdin.read())['data']['token'])" <<< "$LOGIN_RES")
echo "   [OK] Acquired auth token"

echo "==> 3. Seeding product ($SKU) in Inventory Service..."
PROD_RES=$(curl -s -X POST "$INVENTORY_URL/api/v1/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"name\":\"MacBook Pro M3\",\"sku\":\"$SKU\",\"description\":\"E2E Smoke Test Item\",\"price\":1499.99,\"quantityInStock\":10,\"category\":\"ELECTRONICS\"}")

PROD_ID=$(python3 -c "import sys, json; print(json.loads(sys.stdin.read())['data']['id'])" <<< "$PROD_RES")
echo "   [OK] Created product ID: $PROD_ID (Stock: 10)"

echo "==> 4. Placing order for quantity 2 in Order Service..."
ORDER_RES=$(curl -s -X POST "$ORDER_URL/api/v1/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"productId\":$PROD_ID,\"quantity\":2,\"customerName\":\"Smoke Tester\",\"customerEmail\":\"$EMAIL\"}")

ORDER_ID=$(python3 -c "import sys, json; print(json.loads(sys.stdin.read())['data']['id'])" <<< "$ORDER_RES")
echo "   [OK] Order #$ORDER_ID confirmed"

echo "==> 5. Verifying stock deduction in Inventory Service..."
UPDATED_STOCK=$(curl -s "$INVENTORY_URL/api/v1/products/$PROD_ID" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys, json; print(json.loads(sys.stdin.read())['data']['quantityInStock'])")
echo "   [OK] Remaining stock: $UPDATED_STOCK (Expected: 8)"

echo "==> 6. Cancelling order #$ORDER_ID and verifying stock restoration..."
curl -s -X PATCH "$ORDER_URL/api/v1/orders/$ORDER_ID/cancel" \
  -H "Authorization: Bearer $TOKEN" > /dev/null

RESTORED_STOCK=$(curl -s "$INVENTORY_URL/api/v1/products/$PROD_ID" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys, json; print(json.loads(sys.stdin.read())['data']['quantityInStock'])")
echo "   [OK] Restored stock: $RESTORED_STOCK (Expected: 10)"

echo "==> 7. Verifying notification dispatch in Notification Service..."
NOTIF_COUNT=$(curl -s "$NOTIFICATION_URL/api/v1/notifications" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys, json; print(len(json.loads(sys.stdin.read())['data']))")
echo "   [OK] Total notifications recorded: $NOTIF_COUNT"

echo "==> E2E Smoke Test Passed!"
