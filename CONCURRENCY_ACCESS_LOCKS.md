# Concurrency Access Control (Pessimistic Locks)

## What it is
This project uses **pessimistic locking** to prevent two requests from modifying the same inventory record at the same time. The lock is applied on a `StorageItem` row when reserving stock for an order.

## Where it is implemented
- `StorageItemRepository.findForUpdate(...)` uses `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
- `OrderService.reserveInventory(...)` calls that method before reading and updating quantity.

## How it works (flow)
1. Request A reaches `reserveInventory(...)`.
2. The database locks the `storage_items` row for that `storage_id + product_id`.
3. Request A reads current quantity and updates it.
4. Request A commits and releases the lock.
5. Request B waits until the lock is released, then proceeds and sees the updated quantity.

This prevents two requests from reading the same quantity and both decrementing it (classic race condition).

## Benefits
- **Correctness**: prevents double‑sell and negative inventory.
- **Simple**: uses standard database locks; easy to reason about.
- **Consistent**: all writes to the same row are serialized.

## Cost / tradeoffs
- **Blocking**: concurrent requests can wait, increasing response time.
- **Lower throughput**: hot products with high traffic can become a bottleneck.
- **Risk of deadlocks**: possible if multiple rows are locked in different orders.

## Alternatives and comparisons
- **Optimistic locking (version column)**
  - Pros: higher throughput, no blocking.
  - Cons: requires retry on conflict; more application logic.
  - Use when conflicts are rare.

- **Atomic SQL update (single statement)**
  - Example: `UPDATE storage_items SET quantity = quantity - ? WHERE id = ? AND quantity >= ?`
  - Pros: very fast, minimal locking window.
  - Cons: still needs careful handling for multi‑item orders.

- **Distributed locks (e.g., Redis Redlock)**
  - Pros: works across multiple services/instances.
  - Cons: more infrastructure, complexity, and failure modes.
  - Use when multiple services compete for the same resource.

## Why this project uses pessimistic locks
Inventory conflicts are likely in e‑commerce. Pessimistic locking provides a clear, reliable correctness guarantee with simple code and fits the current single‑service architecture.

