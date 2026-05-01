# Order Logic - Implementation Blueprint

## Overview
The Order feature will manage customer orders with items, order status tracking, and inventory management.

## Database Schema (V4__create_orders_table.sql)

```sql
-- Orders main table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    notes LONGTEXT,
    CONSTRAINT orders_users_id_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX orders_users_id_fk (user_id),
    INDEX orders_status (status),
    INDEX orders_created_at (created_at)
);

-- Order items (line items)
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    CONSTRAINT order_items_orders_id_fk FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT order_items_products_id_fk FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    INDEX order_items_orders_id_fk (order_id),
    INDEX order_items_products_id_fk (product_id),
    UNIQUE KEY unique_order_product (order_id, product_id)
);
```

## Entity Classes

### 1. Order Entity
```
Entity: Order
- id: Long (auto-generated)
- user: User (ManyToOne) - required
- items: Set<OrderItem> (OneToMany, mappedBy="order", cascade=PERSIST/MERGE/REMOVE)
- status: OrderStatus (enum) - default PENDING
- totalPrice: BigDecimal (calculated from items)
- createdAt: LocalDateTime (insertable=false, auto-generated)
- updatedAt: LocalDateTime (auto-updated)
- notes: String (optional)
- Methods:
  - getTotalPrice(): BigDecimal (sum of item subtotals)
  - addItem(Product, quantity): OrderItem
  - removeItem(Long productId): void
  - canTransitionTo(OrderStatus): boolean
```

### 2. OrderItem Entity
```
Entity: OrderItem
- id: Long (auto-generated)
- order: Order (ManyToOne, JoinColumn="order_id")
- product: Product (ManyToOne, JoinColumn="product_id")
- quantity: Integer - required
- unitPrice: BigDecimal (captured at order time, not live price)
- subtotal: BigDecimal (calculated: quantity * unitPrice)
- Methods:
  - getSubtotal(): BigDecimal
```

### 3. OrderStatus Enum
```java
public enum OrderStatus {
    PENDING,      // Just created, not yet confirmed
    CONFIRMED,    // Payment confirmed, inventory reserved
    SHIPPED,      // Order dispatched
    DELIVERED,    // Order received
    CANCELED,     // Order canceled
    RETURNED      // Order returned
}
```

## DTOs

### 1. OrderDto
```
- id: Long
- userId: Long
- status: OrderStatus
- totalPrice: BigDecimal
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
- items: List<OrderItemDto>
- notes: String
```

### 2. OrderItemDto
```
- id: Long
- productId: Long
- productName: String (optional, from CartProductDto pattern)
- productPrice: BigDecimal
- quantity: Integer
- subtotal: BigDecimal
```

### 3. CreateOrderRequest
```
- userId: Long (required)
- items: List<CreateOrderItemRequest>
- notes: String (optional)

CreateOrderItemRequest:
- productId: Long (required)
- quantity: Integer (required, min=1, max=1000)
```

### 4. UpdateOrderStatusRequest
```
- status: OrderStatus (required)
```

## Mapper

### OrderMapper
```java
@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "totalPrice", expression = "java(order.getTotalPrice())")
    OrderDto toDto(Order order);
    
    Order toEntity(CreateOrderRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // Set in service
    void update(UpdateOrderStatusRequest request, @MappingTarget Order order);
}
```

## Repository

### OrderRepository
```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"items.product", "user"})
    List<Order> findByUserId(Long userId);
    
    @EntityGraph(attributePaths = {"items.product", "user"})
    List<Order> findByStatus(OrderStatus status);
    
    @EntityGraph(attributePaths = {"items.product", "user"})
    Optional<Order> findById(Long id);
}
```

## Service

### OrderService
```java
@Service
public class OrderService {
    // Methods:
    1. createOrder(CreateOrderRequest): OrderDto
       - Validate user exists
       - Validate all products exist
       - Check inventory (deduct from storage)
       - Create order with items
       - Save and return DTO
    
    2. getOrder(Long id): OrderDto
       - Throw OrderNotFoundException if not found
    
    3. getUserOrders(Long userId): List<OrderDto>
       - Validate user exists
       - Return all orders for user
    
    4. getOrdersByStatus(OrderStatus status): List<OrderDto>
       - Return all orders with given status
    
    5. updateOrderStatus(Long id, OrderStatus newStatus): OrderDto
       - Validate transition rules
       - If CONFIRMED: reserve inventory
       - If CANCELED: return inventory to storage
       - Update and return
    
    6. cancelOrder(Long id): void
       - Validate order can be canceled
       - Return inventory
       - Update status
    
    7. deleteOrder(Long id): void
       - Soft delete or remove if PENDING only
}
```

## Controller

### OrderController
```
Endpoints:
- POST /orders - Create new order
- GET /orders/{id} - Get single order
- GET /orders/user/{userId} - Get user's orders
- GET /orders?status=PENDING - Get orders by status
- PUT /orders/{id}/status - Update order status
- DELETE /orders/{id} - Cancel/delete order

Error Handling:
- 404: OrderNotFoundException, UserNotFoundException
- 400: Insufficient inventory, Invalid transition
- 409: Conflict (order already shipped, etc.)
```

## Exception Classes

```java
public class OrderNotFoundException extends RuntimeException { }
public class InvalidOrderTransitionException extends RuntimeException { }
public class InsufficientInventoryException extends RuntimeException { }
```

## Business Logic Rules

1. **Order Creation**
   - User must exist
   - All products must exist
   - Inventory must be available for all items
   - Initial status: PENDING
   - Inventory not reserved until CONFIRMED

2. **Status Transitions**
   ```
   PENDING → CONFIRMED (with inventory check)
   CONFIRMED → SHIPPED
   SHIPPED → DELIVERED
   ANY → CANCELED (if not delivered)
   DELIVERED → RETURNED (within window)
   ```

3. **Inventory Management**
   - PENDING: No inventory change
   - CONFIRMED: Reserve (deduct from storage)
   - CANCELED: Release reserved inventory
   - DELIVERED: Finalize deduction

4. **Price Snapshot**
   - Unit price captured at order time
   - Not affected by product price changes
   - Total calculated from item subtotals

5. **Audit Trail**
   - created_at: Order creation time
   - updated_at: Last status change
   - Status history optional (can add OrderHistory table later)

## Patterns to Follow

✅ Same as Storage:
- Controller → Service → Repository
- DTO boundaries
- MapStruct for mapping
- @EntityGraph for eager loading
- Manual relationship setting
- Nested IDs in DTOs
- Enum for status (not string)
- Service validates business rules
- Controller catches exceptions

✨ New:
- Enum for order status
- Timestamp fields (created_at, updated_at)
- Generated columns for subtotal (MySQL 5.7+)
- Complex transaction management (inventory + order)
- State transition validation
- Audit trail considerations

## Testing Strategy

1. Create order with valid products
2. Verify inventory deduction on CONFIRMED
3. Verify inventory restoration on CANCEL
4. Test invalid status transitions
5. Test user order retrieval
6. Test order status filtering

## Next Steps After Order

1. **Order History**: Track status changes
2. **Notifications**: Email on order status change
3. **Shipment**: Link orders to shipments
4. **Returns**: Manage order returns
5. **Analytics**: Order reports and dashboard

