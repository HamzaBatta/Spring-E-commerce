# Stress Testing with k6

## Goal
Run a 100-concurrent-user load test to prove the API handles traffic without crashing or losing data.

## What is included
- `k6/products_read.js`: POST load test for `POST /orders` using `X-Strategy: default` (pessimistic locking).

## Prerequisites
- k6 installed (https://k6.io/docs/get-started/installation/).
- App running locally (default `http://localhost:8081`).

## Run the 100-user test
```powershell
k6 run loadtest/k6/products_read.js
```

## Optional overrides
```powershell
$env:BASE_URL = "http://localhost:8081"
$env:ORDER_PATH = "/orders"
$env:STRATEGY = "default"
$env:USER_ID = "1"
$env:STORAGE_ID = "1"
$env:PRODUCT_ID = "1"
$env:QUANTITY = "1"
$env:VUS = "100"
$env:DURATION = "2m"

k6 run loadtest/k6/products_read.js
```

## Report checklist
- k6 summary (p50/p95/p99 latency, error rate, throughput).
- App logs show no crashes.
- DB state is consistent after test.

## Running K6 on docker
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