# 📁 Complete Project Structure

## Backend Source Code (46 Java Files)

```
src/main/java/com/codewithmosh/store/
│
├── StoreApplication.java ........................ Spring Boot main class
│
├── controllers/ (7 files) ....................... REST API endpoints
│   ├── CartController.java ...................... Cart management API
│   ├── GlobalExceptionHandler.java .............. Global validation error handler
│   ├── HomeController.java ...................... MVC home page
│   ├── MessageController.java ................... Test message endpoint
│   ├── ProductController.java ................... Product CRUD API
│   ├── StorageController.java ................... Storage/Inventory API
│   └── UserController.java ...................... User management API
│
├── services/ (2 files) .......................... Business logic layer
│   ├── CartService.java ......................... Cart operations
│   └── StorageService.java ...................... Storage/Inventory operations
│
├── repositories/ (7 files) ...................... Data access layer
│   ├── AddressRepository.java ................... Address data access
│   ├── CartRepository.java ...................... Cart data access (with @EntityGraph)
│   ├── CategoryRepository.java .................. Category data access
│   ├── ProductRepository.java ................... Product data access (with @EntityGraph)
│   ├── ProfileRepository.java ................... Profile data access
│   ├── StorageRepository.java ................... Storage data access (with @EntityGraph)
│   └── UserRepository.java ...................... User data access
│
├── entities/ (9 files) .......................... JPA entity models
│   ├── Address.java ............................. User address entity
│   ├── Cart.java ................................ Shopping cart (UUID key)
│   ├── CartItem.java ............................ Cart line items
│   ├── Category.java ............................ Product categories
│   ├── Message.java ............................. Message entity
│   ├── Product.java ............................. Product entity
│   ├── Profile.java ............................. User profile (1:1 with User)
│   ├── Storage.java ............................. Product inventory tracking
│   └── User.java ................................ User account entity
│
├── dtos/ (11 files) ............................. API data transfer objects
│   ├── AddItemToCartRequest.java ................ Request: add item to cart
│   ├── CartDto.java ............................. Response: cart with items
│   ├── CartItemDto.java ......................... Response: cart line item
│   ├── CartProductDto.java ...................... Response: product in cart
│   ├── ChangePasswordRequest.java ............... Request: password change
│   ├── ProductDto.java .......................... Response: product (with categoryId)
│   ├── RegisterUserRequest.java ................. Request: user registration
│   ├── StorageDto.java .......................... Response: storage (with productId)
│   ├── UpdateCartItemRequest.java ............... Request: update cart item quantity
│   ├── UpdateUserRequest.java ................... Request: update user
│   └── UserDto.java ............................. Response: user
│
├── mappers/ (4 files) ........................... Entity ↔ DTO converters (MapStruct)
│   ├── CartMapper.java .......................... Cart entity → DTO
│   ├── ProductMapper.java ....................... Product entity ↔ DTO
│   ├── StorageMapper.java ....................... Storage entity ↔ DTO
│   └── UserMapper.java .......................... User entity ↔ DTO
│
├── exceptions/ (3 files) ........................ Custom exceptions
│   ├── CartNotFoundException.java ............... Cart not found error
│   ├── ProductNotFoundException.java ............ Product not found error
│   └── StorageNotFoundException.java ............ Storage not found error
│
└── Validation/ (2 files) ........................ Custom validation
    ├── Lowercase.java ........................... @Lowercase annotation
    └── LowerCaseValidator.java .................. Lowercase validator implementation
```

## Resources & Configuration

```
src/main/resources/
│
├── application.yaml ............................. Spring Boot configuration
│   └── MySQL connection settings
│       └── root/root on localhost:3306/store_api
│
├── db/
│   └── migration/ (3 SQL files) ................ Flyway database migrations
│       ├── V1__initial_migration.sql ........... Users, Products, Categories, Addresses
│       ├── V2__create_cart_tables.sql .......... Carts and CartItems
│       └── V3__create_storages_table.sql ....... Storage/Inventory
│
└── templates/
    └── index.html ............................... Thymeleaf MVC template
```

## Configuration Files (Root)

```
project-root/
│
├── pom.xml ..................................... Maven project configuration
│   ├── Spring Boot 4.0.5
│   ├── Java 21 target/source
│   ├── MySQL driver
│   ├── Flyway migrations
│   ├── JPA/Hibernate
│   ├── Lombok annotations
│   ├── MapStruct mapping
│   ├── Thymeleaf templating
│   ├── SpringDoc OpenAPI
│   └── Validation
│
├── mvnw / mvnw.cmd ............................. Maven wrapper (run on Unix/Windows)
├── README.md .................................... Original project README
└── script.sql ................................... Legacy SQL (not used - use Flyway)
```

## Documentation Files (New - Created)

```
project-root/
│
├── AGENTS.md .................................... AI agent guidance for developers
│   └── Architecture patterns, conventions, file examples
│
├── DOCUMENTATION_INDEX.md ....................... Navigation guide for all docs
│   └── Quick reference and FAQ
│
├── README_IMPLEMENTATION.md ..................... Complete implementation overview
│   └── Architecture, endpoints, workflow, conventions
│
├── COMPLETION_SUMMARY.md ........................ What's been completed
│   └── Features, components, testing status
│
├── STORAGE_IMPLEMENTATION.md .................... Storage feature detailed guide
│   └── Schema, entities, DTOs, services, patterns
│
├── STORAGE_CHECKLIST.md ......................... Implementation verification
│   └── All files listed, validation rules, status
│
├── STORAGE_API_TESTING.md ....................... Testing and API reference
│   └── curl examples, responses, troubleshooting
│
├── STORAGE_VISUAL_SUMMARY.md .................... Diagrams and visuals
│   └── Architecture flow, data model, performance
│
└── ORDER_BLUEPRINT.md ........................... Next feature design
    └── Complete specification for Order management
```

## Database Schema

```
MySQL: store_api

Tables (9 total):
├── users                 [BIGINT id, name, email, password]
├── profiles              [BIGINT id (1:1), bio, phone, dob, loyalty_points]
├── addresses             [BIGINT id, street, city, state, zip, user_id (FK)]
├── categories            [TINYINT id, name]
├── products              [BIGINT id, name, description, price, category_id (FK)]
├── wishlist              [user_id (FK), product_id (FK) - M:M junction]
├── carts                 [UUID id, date_created]
├── cart_items            [BIGINT id, cart_id (FK), product_id (FK), quantity]
└── storages              [BIGINT id, name, location, product_id (FK), quantity]
```

## Dependencies (pom.xml)

```
Spring Boot Framework
├── spring-boot-starter-web ..................... REST API support
├── spring-boot-starter-data-jpa ............... JPA/Hibernate ORM
├── spring-boot-starter-validation ............ Jakarta validation
└── spring-boot-starter-thymeleaf ............. MVC template engine

Database
├── mysql-connector-j ........................... MySQL JDBC driver
├── flyway-core ................................. Schema migrations
└── flyway-mysql ................................ MySQL-specific migration support

Code Generation
├── lombok ....................................... Annotation processor
├── mapstruct .................................... DTO mapping
└── lombok-mapstruct-binding .................... Integration

Documentation
└── springdoc-openapi ........................... OpenAPI/Swagger auto-generation

Testing
├── spring-boot-starter-test ................... JUnit, Mockito, etc.
```

## Build Artifacts

```
target/
├── classes/
│   ├── com/codewithmosh/store/
│   │   ├── *.class files (compiled Java)
│   │   └── Generated mappers (MapStruct)
│   ├── application.yaml (resource)
│   └── db/migration/ (Flyway SQL)
└── generated-sources/
    └── Lombok/MapStruct generated code
```

## Summary Statistics

```
📊 Project Metrics

Code Files:
  ├── Controllers:      7 files
  ├── Services:         2 files
  ├── Repositories:     7 files
  ├── Entities:         9 files
  ├── DTOs:            11 files
  ├── Mappers:          4 files
  ├── Exceptions:       3 files
  ├── Validation:       2 files
  └── Total Java:      46 files

SQL/Database:
  ├── Migrations:       3 files (V1, V2, V3)
  ├── Tables:           9 tables
  ├── Foreign Keys:     8 constraints
  └── Indexes:          5+ indexes

Documentation:
  ├── Guides:           8 documents
  ├── Total Pages:      ~100+ pages equivalent
  └── Code Examples:    50+ curl/Java examples

API Endpoints:
  ├── Products:         5 endpoints
  ├── Users:            6 endpoints (including change-password)
  ├── Carts:            6 endpoints
  ├── Storages:         5 endpoints
  ├── Static/MVC:       2 endpoints
  └── Total:           24 endpoints

Lines of Code:
  ├── Backend Java:     ~2,500 LOC
  ├── SQL Migrations:   ~115 LOC
  ├── Config:           ~150 LOC (pom.xml)
  └── Documentation:    ~1,500 LOC
```

## How It All Fits Together

```
API Request (e.g., POST /storages)
         ↓
[StorageController] 
         ↓ (deserialize JSON to StorageDto)
[Request Validation]
         ↓ (check product exists)
[StorageService]
         ↓ (fetch Product entity, set relationship)
[StorageRepository]
         ↓ (JPA persist)
[Hibernate/JPA]
         ↓
[MySQL Database]
    ↓ (query executed)
[Insert into storages table]
    ↓ (retrieves generated id)
[Hibernate loads result]
    ↓
[StorageMapper]
    ↓ (convert Storage entity to StorageDto)
[StorageController]
    ↓ (wrap in ResponseEntity with 201 status)
JSON Response to Client
```

## File Organization Principles

✅ **By Layer** (not by feature)
- controllers/ - all controllers
- services/ - all services
- repositories/ - all repositories
- entities/ - all entities
- dtos/ - all DTOs

✅ **Standard Spring Boot Structure**
- Follows Maven standard directory layout
- Resources in src/main/resources
- Tests in src/test (when added)

✅ **Naming Conventions**
- Controllers: `*Controller.java`
- Services: `*Service.java`
- Repositories: `*Repository.java`
- Entities: `*.java` (plain names)
- DTOs: `*Dto.java` or `*Request.java` or `*Response.java`
- Mappers: `*Mapper.java`
- Exceptions: `*Exception.java`

## Execution Flow Example

```
1. Client sends: POST /storages with JSON
   └─ {"name": "Warehouse", "location": "Zone 1", "productId": 1, "quantity": 50}

2. Dispatcher servlet routes to StorageController

3. StorageController.createStorage()
   └─ Validates @RequestBody using @Valid
   └─ Calls storageService.createStorage(StorageDto)

4. StorageService.createStorage()
   └─ Fetches Product from repository (validates exists)
   └─ Creates Storage entity via mapper
   └─ Manually sets product relationship
   └─ Calls storageRepository.save()

5. StorageRepository.save()
   └─ Delegates to Hibernate/JPA
   └─ Generates INSERT SQL

6. MySQL Database
   └─ Executes INSERT statement
   └─ Returns generated ID
   └─ Returns storage record

7. Mapper converts Storage entity → StorageDto

8. Controller returns ResponseEntity
   └─ Status: 201 Created
   └─ Header: Location: /storages/1
   └─ Body: StorageDto as JSON

9. Response sent to client
```

This structure enables:
- ✅ Clean separation of concerns
- ✅ Easy testing (mock dependencies)
- ✅ Clear code navigation
- ✅ Reusable components
- ✅ Scalable architecture

