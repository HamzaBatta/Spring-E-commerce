# AGENTS.md

## Project snapshot
- Spring Boot 4.0.5 + Java 25 + Maven (`mvnw`/`mvnw.cmd`); app entry is `src/main/java/com/codewithmosh/store/StoreApplication.java`.
- This repo is a mixed app: REST APIs under `controllers/` plus a small Thymeleaf page (`HomeController` -> `src/main/resources/templates/index.html`).

## Architecture to preserve
- Keep the controller → service → repository layering visible in `controllers/`, `services/`, and `repositories/`.
- Most domain data lives in JPA entities under `entities/`; API payloads are separate DTOs in `dtos/`.
- Use `MapStruct` mappers (`mappers/`) to translate between entities and DTOs instead of exposing entities directly.

## API conventions in this codebase
- Controllers usually return `ResponseEntity` with explicit status codes and `Location` headers for creates; see `ProductController`, `CartController`, `StorageController`, and `UserController`.
- Create/update flows typically fetch related entities first, then set relationships manually before saving (e.g. `ProductController` sets `category`, `StorageService` sets `product`).
- Nested IDs are exposed through DTO fields like `ProductDto.categoryId` and `StorageDto.productId`.

## Persistence patterns
- `@EntityGraph` is used to avoid lazy-loading surprises in read endpoints (for example `CartRepository.getCartWithItems()` and `StorageRepository.findAllWithProduct()`).
- The cart uses a UUID primary key and eager `items` loading (`entities/Cart.java`); `Profile` uses `@MapsId` for its one-to-one key sharing.
- Flyway migrations in `src/main/resources/db/migration/` are the schema source of truth; do not treat `script.sql` as the canonical schema.

## Validation and errors
- Request DTOs use `jakarta.validation` annotations; `RegisterUserRequest` also uses the custom `@Lowercase` constraint from `Validation/`.
- Global validation failures are converted to `{field: message}` maps in `controllers/GlobalExceptionHandler.java`.
- Some domain errors are handled locally in controllers/services with custom exceptions like `CartNotFoundException`, `ProductNotFoundException`, and `StorageNotFoundException`.

## Build and debug workflow
- Run the app/tests with Maven wrapper: `./mvnw test`, `./mvnw spring-boot:run` (or `mvnw.cmd` on Windows).
- Database connection defaults to local MySQL `store_api` with `root/root` in `src/main/resources/application.yaml` and `pom.xml` Flyway plugin config.
- Generated output lives under `target/`; avoid editing it directly.

## File-specific examples to follow
- `mappers/ProductMapper.java` and `mappers/StorageMapper.java` show the preferred create/update mapping style (`@MappingTarget`, ignore `id` on update).
- `repositories/ProductRepository.java` and `repositories/CartRepository.java` show repository methods paired with fetch plans.
- `controllers/MessageController.java` and `controllers/HomeController.java` show the minimal MVC/JSON endpoints already in the project.

## Storage module (complete implementation)
- `entities/Storage.java`: Product inventory tracker with name, location, product reference, and quantity.
- `dtos/StorageDto.java`: Exposes `productId` instead of full Product object.
- `services/StorageService.java`: Validates product existence before create/update; manually sets relationships.
- `repositories/StorageRepository.java`: Uses `@EntityGraph` to load products eagerly; provides `findByProductId()` and `findAllWithProduct()`.
- `controllers/StorageController.java`: Standard CRUD with local exception handlers for `StorageNotFoundException` and `ProductNotFoundException`.
- `db/migration/V3__create_storages_table.sql`: Schema with CASCADE delete on product and unique index on (product_id, location).
- Reference: `STORAGE_IMPLEMENTATION.md` for complete patterns and design decisions.
