# Redis Caching for Most Requested Products

## Goal
Reduce direct database reads for frequently requested products by caching product responses in Redis.

## Solution in this project
- Cache product responses with Spring Cache + Redis using the "products" cache.
- Track product views in Redis (sorted set) to identify the most requested products.
- Expose stats endpoints to verify view counts and compare cache vs DB timing.

## Why Redis caching fits this project
- Product details are read-heavy and change less often than they are read.
- Cache hits reduce load on MySQL during traffic spikes.
- Redis provides fast, in-memory access and is already part of the stack.

## Where it is implemented
- `src/main/java/com/codewithmosh/store/config/RedisConfig.java`
  - RedisTemplate and CacheManager configuration.
- `src/main/java/com/codewithmosh/store/services/ProductService.java`
  - `@Cacheable("products")` on get-by-id.
- `src/main/java/com/codewithmosh/store/services/ProductViewTracker.java`
  - Tracks views and provides top-product rankings.
- `src/main/java/com/codewithmosh/store/controllers/ProductController.java`
  - Increments view count for every product request.
- `src/main/java/com/codewithmosh/store/controllers/ProductStatsController.java`
  - View counts and cache benchmark endpoint.
- `src/main/resources/application.yaml`
  - Redis host and port configuration.
- `docker-compose.redis.yml`
  - Local Redis container for development.

## How it works (short)
1. A product request increments its view count in Redis.
2. The product response is cached under "products::{id}".
3. The next request for the same product is served from Redis cache.
4. Stats endpoints show view counts and cache timing differences.

## How to verify
- Call `GET /products/{id}` multiple times.
- Check `GET /api/products/stats/{id}/views` to see view count increase.
- Call `GET /api/products/stats/{id}/benchmark` to compare first vs second call time.

## Alternatives considered
- Local in-memory cache: fast but not shared across instances.
- HTTP-level caching: helps clients but does not reduce server-side DB load.


