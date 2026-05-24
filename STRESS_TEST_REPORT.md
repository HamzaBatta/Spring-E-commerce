# Stress Test Report (100 Concurrent Users)

## Date
2026-05-24

## Environment
- Load balancer: Nginx (least connections)
- App instances: 3
- Database: MySQL
- Cache: Redis
- Tool: k6 (Docker)

## Test Plan
- Endpoint: `POST /orders`
- Strategy: `X-Strategy: default` (pessimistic locking)
- VUs: 100
- Duration: 2 minutes
- Payload:
  - `userId=1`
  - `storageId=1`
  - `productId=1`
  - `quantity=1`

## Results (k6 summary)
- http_req_failed: 0.00% (0 / 7,992)
- p95 latency: 1.6s (threshold p95 < 1.0s failed)
- p90 latency: 1.54s
- median latency: 1.41s
- throughput: 65.73 req/s
- total requests: 7,992

## Stability
- App crashes: none observed
- Error rate: 0.00% (PASS)

## Data Integrity Checks
- Negative inventory check:
  - SQL:
    ```sql
    SELECT COUNT(*) AS negative_rows
    FROM storage_items
    WHERE quantity < 0;
    ```
  - Result: `negative_rows = 0` (PASS)

## Conclusion
The system handled 100 concurrent users for 2 minutes with 0% request failures and no negative inventory. The p95 latency target of < 1s was not met (p95 = 1.6s). The system is stable under 100 concurrent users, with data integrity preserved, and performance tuning is recommended to meet the latency target.

