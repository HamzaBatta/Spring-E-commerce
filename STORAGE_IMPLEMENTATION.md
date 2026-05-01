# Storage Logic Implementation

## Overview
The Storage feature tracks inventory for products across different storage locations. Each storage location has a quantity of a specific product.

## Database Schema
Location: `src/main/resources/db/migration/V3__create_storages_table.sql`

```sql
CREATE TABLE storages (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT DEFAULT 0 NOT NULL,
    CONSTRAINT storages_products_id_fk 
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    INDEX storages_products_id_fk (product_id)
)
```

## Entity
**File:** `src/main/java/com/codewithmosh/store/entities/Storage.java`
- `id`: Long (auto-generated)
- `name`: String (storage location name, e.g., "Main Warehouse")
- `location`: String (physical location details)
- `product`: ManyToOne relationship to Product
- `quantity`: Integer (number of items in storage)

## DTO
**File:** `src/main/java/com/codewithmosh/store/dtos/StorageDto.java`
- `id`: Long
- `name`: String
- `location`: String
- `productId`: Long (exposed instead of full Product object)
- `quantity`: Integer

## Mapper
**File:** `src/main/java/com/codewithmosh/store/mappers/StorageMapper.java`
- `toDto(Storage)`: Converts entity to DTO, mapping `product.id` → `productId`
- `toEntity(StorageDto)`: Converts DTO to entity
- `update(StorageDto, Storage)`: Updates entity fields, ignoring `id`

## Repository
**File:** `src/main/java/com/codewithmosh/store/repositories/StorageRepository.java`
- Extends `JpaRepository<Storage, Long>`
- `findByProductId(Long)`: Fetch all storage records for a product (with eager product loading via `@EntityGraph`)
- `findAllWithProduct()`: Fetch all storage records with eager product loading

## Service
**File:** `src/main/java/com/codewithmosh/store/services/StorageService.java`

**Methods:**
1. `getAllStorages(Long productId)`: Get storages (optionally filtered by product)
2. `getStorage(Long id)`: Get single storage record
3. `createStorage(StorageDto)`: Create new storage
   - Validates that product exists
   - Sets the product relationship
   - Throws `ProductNotFoundException` if product not found
4. `updateStorage(Long id, StorageDto)`: Update storage
   - Validates storage and product exist
   - Updates fields and product relationship
5. `deleteStorage(Long id)`: Delete storage
   - Throws `StorageNotFoundException` if not found

## Controller
**File:** `src/main/java/com/codewithmosh/store/controllers/StorageController.java`

**Endpoints:**
- `GET /storages` - List all storages (optional `?productId=X` filter)
- `GET /storages/{id}` - Get single storage
- `POST /storages` - Create new storage (returns 201 with Location header)
- `PUT /storages/{id}` - Update storage
- `DELETE /storages/{id}` - Delete storage (returns 204)

**Error Handling:**
- Returns `404 Not Found` when storage not found
- Returns `400 Bad Request` when product not found
- Local exception handlers in controller catch domain exceptions

## Exception Classes
**Files:**
- `src/main/java/com/codewithmosh/store/exceptions/StorageNotFoundException.java`
- `src/main/java/com/codewithmosh/store/exceptions/ProductNotFoundException.java`

Both extend `RuntimeException` for unchecked exception handling.

## Patterns Used
1. **Lazy Loading Avoidance**: `@EntityGraph` ensures product is loaded with storage queries
2. **Relationship Management**: Manually set product before saving (not cascade.PERSIST)
3. **DTO Boundaries**: Expose `productId` instead of full Product object
4. **Status Codes**: REST conventions (201 Created, 204 No Content, 404 Not Found, 400 Bad Request)
5. **Service Layer**: Business logic in service, not controller
6. **MapStruct**: Automatic mapping between entities and DTOs

## Next Steps: Order Logic
After confirming storage is working, implement Order feature with:
- Order entity (main order + order items)
- Order status tracking
- Inventory deduction when order is placed
- Order history and management

