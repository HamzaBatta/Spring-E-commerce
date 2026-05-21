# Resource Management and Capacity Control (Thread Pool)

## Goal
Protect the service from overload by limiting how many orders run in parallel.

## Solution in this project
- Use a bounded `ThreadPoolExecutor` for order creation in the safe strategy.
- When the queue is full, reject requests with a clear "system busy" error.

## Why a thread pool fits this project
- Keeps latency stable under spikes (no unlimited thread creation).
- Provides backpressure with a bounded queue.
- Easy to tune with two numbers: pool size and queue size.

## Where it is implemented
- `src/main/java/com/codewithmosh/store/services/DefaultOrderCreationStrategy.java`
  - `ThreadPoolExecutor` with `ArrayBlockingQueue`.
  - Rejection mapped to `SystemBusyException`.
- `src/main/resources/application.yaml`
  - `app.capacity.orders.maxConcurrent`
  - `app.capacity.orders.queueSize`

## How it works (short)
1. Request enters the thread pool.
2. If a worker is free, it runs immediately.
3. If all workers are busy, it waits in the queue.
4. If the queue is full, the request is rejected.

## How to verify
- Lower `maxConcurrent` and `queueSize`.
- Run a concurrency test:
  - `POST /orders/test/concurrency`
- Expect some requests to return "System is busy" under heavy load.

## Alternatives considered
- Semaphore: simple but drops work instead of queueing it.
- Reactive model: higher throughput but larger refactor.


