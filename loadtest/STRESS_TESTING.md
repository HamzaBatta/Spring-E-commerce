# Stress Testing (100 Concurrent Users)

## Requirement
Prove the system can serve at least 100 concurrent users without crashing or data loss.

## What this test does
- Uses k6 to send concurrent `POST /orders` requests through the load balancer.
- Forces the safe locking strategy with `X-Strategy: default`.
- Produces console output you can paste into the report.

## Prerequisites
- App instances are running behind Nginx.
- `loadtest/k6/products_read.js` exists and targets `POST /orders`.

## Run the full 100-user test (Docker)
> Use the Nginx port in `BASE_URL`. If Nginx is on `localhost:80`, leave it as-is. If it is on `8080`, use `http://host.docker.internal:8080`.

```powershell
docker run --rm -i `
  -v "${PWD}\loadtest\k6:/scripts" `
  -e BASE_URL="http://host.docker.internal" `
  -e ORDER_PATH="/orders" `
  -e STRATEGY="default" `
  -e USER_ID="1" `
  -e STORAGE_ID="1" `
  -e PRODUCT_ID="1" `
  -e QUANTITY="1" `
  -e VUS=100 `
  -e DURATION=2m `
  grafana/k6 run /scripts/products_read.js
```

## Optional: save a JSON summary for the report
```powershell
docker run --rm -i `
  -v "${PWD}\loadtest\k6:/scripts" `
  -v "${PWD}\loadtest:/out" `
  -e BASE_URL="http://host.docker.internal" `
  -e ORDER_PATH="/orders" `
  -e STRATEGY="default" `
  -e USER_ID="1" `
  -e STORAGE_ID="1" `
  -e PRODUCT_ID="1" `
  -e QUANTITY="1" `
  -e VUS=100 `
  -e DURATION=2m `
  grafana/k6 run --summary-export=/out/summary.json /scripts/products_read.js
```

## Console output to include in the report
- The k6 summary (thresholds, http_req_failed, p95 latency, throughput).
- Total requests and duration.
- Any threshold failures (if p95 exceeds the target).
