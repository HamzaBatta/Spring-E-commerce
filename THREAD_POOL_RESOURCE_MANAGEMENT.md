# Resource Management (Thread Pool Queue)

## What it is
This project uses a bounded thread pool with a queue to control how many order creations run in parallel, while keeping extra requests in a queue instead of rejecting them immediately.

## Where it is implemented
- `DefaultOrderCreationStrategy` owns a bounded `ThreadPoolExecutor`.
- Orders are submitted to the pool; when the queue is full, the request fails with `SystemBusyException`.
- Configuration lives in `application.yaml`:
  - `app.capacity.orders.maxConcurrent` (thread count)
  - `app.capacity.orders.queueSize` (queue capacity)

## How it works (flow)
1. A request hits `OrderService.createOrder(...)`.
2. The strategy selector resolves the `default` order creation strategy.
3. The request is submitted to the bounded thread pool.
4. If there is capacity, the task runs inside a transaction and completes normally.
5. If the queue is full, the executor rejects the task and a `SystemBusyException` is returned.

## Benefits
- **No request is wasted immediately**: extra traffic can wait in the queue.
- **Predictable resource usage**: fixed threads avoid overload of DB connections.
- **Controlled throughput**: queue absorbs bursts without crashing the app.

## Cost / tradeoffs
- **Higher latency under load**: queued requests wait longer.
- **Queue can become a bottleneck** if too small or too large.
- **Still needs limits**: if queue is unbounded, memory can grow and crash.

## Alternatives and comparisons
- **Semaphore (fail fast)**
  - Pros: low latency, predictable response time, simple.
  - Cons: drops traffic during spikes.
  - Use when you prefer rejection over delay.

- **Rate limiter (token bucket)**
  - Pros: smooths request rate at the edge.
  - Cons: doesn’t control concurrent DB work directly.
  - Use for API throttling before business logic.

- **Bulkhead pools per feature**
  - Pros: isolates traffic across subsystems.
  - Cons: more configuration and tuning.
  - Use in larger systems with many independent workloads.

## Why this project uses a thread pool
You wanted queued requests instead of rejection. A bounded thread pool provides that while still preventing the system from running too many order creations at once.

