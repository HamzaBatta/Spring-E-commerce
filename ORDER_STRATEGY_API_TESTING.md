# Order API - Strategy Testing (default vs naive)

## Prerequisites
- App running: `./mvnw spring-boot:run` or `mvnw.cmd spring-boot:run`
- MySQL running and `store_api` database available
- At least one user exists
- At least one product exists
- One storage with items (warehouse + inventory)

---

## Common Order Payload
Use the same payload for both strategies, only change the `X-Strategy` header.

```json
{
  "userId": 1,
  "storageId": 1,
  "items": [
    { "productId": 1, "quantity": 1 }
  ]
}
```

---

## 1) Default strategy (locks + thread pool)
This uses pessimistic locking and the thread pool queue.

### Request
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "X-Strategy: order=default,invoice=async" \
  -d '{
    "userId": 1,
    "storageId": 1,
    "items": [
      { "productId": 1, "quantity": 1 }
    ]
  }'
```

### Expected
- Successful order when inventory is available.
- Under heavy load, some requests may fail with:
  - `{"error":"System is busy. Please try again."}` (thread pool queue full)
  - `{"error":"insufficient inventory"}` (stock exhausted)

---

## 2) Naive strategy (no lock, no queue)
This is unsafe and can oversell under concurrency.

### Request
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "X-Strategy: order=naive,invoice=async" \
  -d '{
    "userId": 1,
    "storageId": 1,
    "items": [
      { "productId": 1, "quantity": 1 }
    ]
  }'
```

### Expected
- Works normally with low concurrency.
- Under concurrent load, may oversell or allow negative stock.

---

## Quick concurrency test (Windows CMD)
Run two simultaneous requests with different users.

**Default (safe)**
```cmd
start "Order-1" cmd /k curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -H "X-Strategy: order=default,invoice=async" -d "{\"userId\":1,\"storageId\":1,\"items\":[{\"productId\":1,\"quantity\":1}]}"
start "Order-2" cmd /k curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -H "X-Strategy: order=default,invoice=async" -d "{\"userId\":2,\"storageId\":1,\"items\":[{\"productId\":1,\"quantity\":1}]}"
```

**Naive (unsafe)**
```cmd
start "Order-1" cmd /k curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -H "X-Strategy: order=naive,invoice=async" -d "{\"userId\":1,\"storageId\":1,\"items\":[{\"productId\":1,\"quantity\":1}]}"
start "Order-2" cmd /k curl -X POST http://localhost:8080/orders -H "Content-Type: application/json" -H "X-Strategy: order=naive,invoice=async" -d "{\"userId\":2,\"storageId\":1,\"items\":[{\"productId\":1,\"quantity\":1}]}"
```

---

## Verify inventory after tests
```sql
SELECT * FROM storage_items WHERE storage_id = 1 AND product_id = 1;
```

- **Default**: quantity should never go below 0.
- **Naive**: quantity might go below 0 under heavy load.

