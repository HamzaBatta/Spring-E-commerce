# Benchmarking & Tracing — what was added and how to test

This document describes the changes made to add Micrometer timing/tracing and a small benchmark endpoint, and gives step‑by‑step instructions to test them (Postman / Zipkin / optional k6). Follow these steps to reproduce and validate metrics and traces.

## Summary of changes
- pom.xml
  - Added dependencies: `io.micrometer:micrometer-core`, `io.micrometer:micrometer-tracing-bridge-brave`, `io.zipkin.reporter2:zipkin-reporter-brave`.

- `src/main/resources/application.yaml`
  - Added tracing/zipkin settings and enabled percentiles for timers:
    - `management.tracing.sampling.probability: 1.0`
    - `spring.zipkin.base-url: http://localhost:9411`
    - `management.zipkin.tracing.endpoint: http://localhost:9411/api/v2/spans`
    - Percentiles and histograms for `order.simple` and `order.optimized`.

- Instrumentation in order strategies
  - `NaiveOrderCreationStrategy` (the unsafe/"simple" strategy) — injected `MeterRegistry` and wrapped the `create(...)` body with:
    `meterRegistry.timer("order.simple").recordCallable(() -> { ...existing logic... })`.
  - `DefaultOrderCreationStrategy` (the safe/pessimistic strategy) — injected `MeterRegistry` and wrapped the `create(...)` method with:
    `meterRegistry.timer("order.optimized").recordCallable(() -> { ...existing logic... })`.
  - Important: business logic inside both methods was NOT changed — only timing wrappers were added.

- Controller
  - `BenchmarkController` at `GET /api/benchmark/metrics` returns timer metrics for `order.simple` and `order.optimized` (count, mean_ms, max_ms, p95_ms, p99_ms).

## Files changed (for reference)
- `pom.xml`
- `src/main/resources/application.yaml`
- `src/main/java/com/codewithmosh/store/services/NaiveOrderCreationStrategy.java`
- `src/main/java/com/codewithmosh/store/services/DefaultOrderCreationStrategy.java`
- `src/main/java/com/codewithmosh/store/controllers/BenchmarkController.java`

## How this works (brief)
- Micrometer timers record the duration of the wrapped method bodies.
- Percentiles require either the percentile settings (enabled) and enough samples (many requests) for stable p95/p99 values. With very few samples the percentile values are unreliable.
- Traces are exported to Zipkin (configured to `http://localhost:9411`). Zipkin UI shows traces when the application successfully sends spans.

---

## Quick start (what to run locally)

1) Start Zipkin (if you want to see traces)

```powershell
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin:2.23
```

Open Zipkin UI: http://localhost:9411

2) Build and run the Spring application (one-time build)

```powershell
# from project root
.\mvnw.cmd -DskipTests package
java -jar .\target\store-0.0.1-SNAPSHOT.jar
```

Or run from your IDE (run `StoreApplication`).

3) Confirm the app is healthy

In Postman / browser: GET http://localhost:8081/actuator/health → 200 OK (status UP).

4) Find valid IDs (Postman)

- GET http://localhost:8081/users → pick a `userId`
- GET http://localhost:8081/products → pick a `productId`
- GET http://localhost:8081/storages → pick a `storageId`

Use those IDs for the requests below.

---

## Test using Postman (step‑by‑step)

1) Create a Postman environment with variables:
- `baseUrl` = `http://localhost:8081`
- `userId`, `productId`, `storageId` = values discovered earlier

2) Single-order (default strategy)

Request:
- Method: POST
- URL: `{{baseUrl}}/orders`
- Headers: `Content-Type: application/json`, `X-Strategy: default`
- Body (raw JSON):

```json
{
  "userId": {{userId}},
  "storageId": {{storageId}},
  "items": [ { "productId": {{productId}}, "quantity": 1 } ]
}
```

Expected: HTTP 201 Created. This produces samples for `order.optimized`.

3) Single-order (naive strategy)

Same as above but header `X-Strategy: naive` → produces samples for `order.simple`.

4) Server-side concurrency quick test

Request:
- Method: POST
- URL: `{{baseUrl}}/orders/test/concurrency`
- Header: `X-Strategy: naive` or `default`
- Body (raw JSON):

```json
{
  "userId": {{userId}},
  "storageId": {{storageId}},
  "productId": {{productId}},
  "quantity": 1,
  "requests": 10
}
```

This endpoint runs many concurrent requests server-side and returns a summary: `requested`, `success`, `busy`, `insufficientInventory`, `otherErrors`.

5) Generate many samples for stable percentiles (Postman Runner)

- Put the POST `/orders` request into a collection and run the Collection Runner with 200–500 iterations (or more). That will populate p95/p99 values.

6) Fetch benchmark metrics

Request:
- Method: GET
- URL: `{{baseUrl}}/api/benchmark/metrics`

Response contains `simple_order` and/or `optimized_order` objects with:
- `count`, `mean_ms`, `max_ms`, `p95_ms`, `p99_ms`.

Notes: If `count` is small (e.g. 1–10) percentiles are unreliable — generate many samples.

---

## Validate traces in Zipkin

1) In Zipkin UI (http://localhost:9411) set:
- Service name: `store` (default `spring.application.name`)
- Lookback: Last 1 minute
- Click `Search`

2) If nothing appears:
- Verify Zipkin is reachable at http://localhost:9411/api/v2/services (should return JSON array; may be empty initially).
- Ensure you sent order requests after restarting the app (spans are sent only after requests).
- If `/api/v2/services` does not show `store` after you sent requests, check app logs for tracing/export errors.

3) Helpful Zipkin API checks (Postman)
- GET http://localhost:9411/api/v2/services
- GET http://localhost:9411/api/v2/traces?serviceName=store

If traces appear in the API but not UI, refresh the Zipkin UI; the UI uses the same backend APIs.

---

## Troubleshooting checklist

- 404 on `/api/benchmark/metrics`: restart the app — new controller requires a restart after file changes.
- Empty `simple_order`/`optimized_order`: send more requests; timers are created only after samples.
- Percentiles `NaN` or odd values: too few samples — run larger load.
- No traces in Zipkin:
  - Confirm Zipkin container is running and reachable.
  - Confirm `spring.zipkin.base-url` and `management.tracing.sampling.probability: 1.0` are set and app restarted.
  - Check application logs for errors from Micrometer/Brave/Zipkin reporter.

Run this quick debug in your browser/Postman to confirm Zipkin knows about services:

```
GET http://localhost:9411/api/v2/services
```

If you see `[]` and no `store` even after sending requests, paste 50–100 lines of your application log here and I will inspect.

---

## Data integrity verification (after concurrency tests)

To prove no data loss / oversell after a concurrency experiment, run this SQL against the `store_api` database:

```sql
SELECT COUNT(*) AS negative_rows
FROM storage_items
WHERE quantity < 0;
```

Expected: `negative_rows = 0` for the safe (`default`) strategy. If `naive` yields > 0, the race condition was triggered.

---

## Optional next steps (recommended)
- Use `k6` for larger, repeatable load tests (we already have example scripts in `loadtest/k6`).
- Add an executor-queue size gauge (Micrometer) to expose queue length of the order executor as a metric if you want to correlate queueing with high latency.
- Tune `app.capacity.orders.maxConcurrent` and `queueSize` for the `default` strategy and re-run experiments to see effect on p95.

If you want I can prepare a Postman Collection + Runner CSV you can import directly — say "prepare collection" and I'll add it to the repo.

---

Last notes
- I wrapped the two order creation flows with Micrometer timers but did not change business logic. Restart the app after pulling these changes. Generate many samples to get meaningful percentiles and use Zipkin to inspect slow spans.

If anything fails during these steps, paste the app logs (last ~100 lines) and the output of `GET http://localhost:9411/api/v2/services` and I'll continue troubleshooting.

