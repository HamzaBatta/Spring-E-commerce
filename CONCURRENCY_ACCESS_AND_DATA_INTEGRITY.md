# Concurrent Access and Data Integrity

## Goal
Prevent race conditions when multiple users order the same inventory at the same time.

## Solution in this project
- Use pessimistic locking on inventory rows so only one transaction can modify a stock row at a time.
- Keep the naive/baseline order flow without locks so you can compare behavior.

## Why pessimistic locking fits this project
- Inventory must never go negative.
- Orders are write-heavy during bursts, so correctness is higher priority than maximum throughput.
- The lock is applied only in the safe strategy, which keeps the benchmark baseline unchanged.

## Where it is implemented
- `src/main/java/com/codewithmosh/store/repositories/StorageItemRepository.java`
  - `findForUpdate(storageId, productId)` uses `@Lock(PESSIMISTIC_WRITE)`.
- `src/main/java/com/codewithmosh/store/services/DefaultOrderCreationStrategy.java`
  - `reserveInventory(...)` calls `findForUpdate(...)` and decrements quantity.

## How it works (short)
1. Read storage item with `SELECT ... FOR UPDATE`.
2. Check quantity.
3. Update quantity and save.
4. Commit transaction to release the lock.

## How to verify
- Create a small stock quantity (ex: 5).
- Run concurrent orders for the same product.
- Expect: no negative inventory and some requests fail with `insufficient inventory` instead of overselling.

## Alternatives considered
- Optimistic locking: better throughput but requires retry on version conflicts.
- Distributed locks (Redis): adds infrastructure and operational overhead.


