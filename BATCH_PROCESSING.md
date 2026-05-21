# Batch Processing

## Goal
Process large sets of orders in one run and produce a sales summary.

## Solution in this project
- A daily sales batch job aggregates order totals.
- Two processors exist: simple and chunked/parallel.

## Where it is implemented
- `src/main/java/com/codewithmosh/store/services/DailySalesJob.java`
  - Orchestrates the batch run.
- `src/main/java/com/codewithmosh/store/services/SimpleDailySalesProcessor.java`
  - Single-pass processing.
- `src/main/java/com/codewithmosh/store/services/ChunkedParallelDailySalesProcessor.java`
  - Chunked parallel processing.
- `src/main/java/com/codewithmosh/store/controllers/MetricsController.java`
  - `POST /metrics/daily-sales/run` to run manually.
  - `GET /metrics/daily-sales` to read last report.

## How it works (short)
1. Read eligible orders for the day.
2. Sum totals and count orders.
3. Store a `BatchReport` (in memory and/or DB store).

## Why batch processing fits this project
- Sales aggregation is periodic and not user-facing.
- Avoids expensive per-request aggregation.
- Scales with chunked processing when data grows.

## How to verify
- Run manual batch:
  - `POST /metrics/daily-sales/run`
- Fetch last report:
  - `GET /metrics/daily-sales`


