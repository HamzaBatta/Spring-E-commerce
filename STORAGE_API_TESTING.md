# Storage API - Quick Reference & Testing Guide

## Testing the Storage Endpoints

### Prerequisites
- App running: `.\mvnw.cmd spring-boot:run`
- MySQL with `store_api` database
- At least one product created in the system

### 1. Get All Storages
```bash
curl -X GET http://localhost:8080/storages
```
Response (200 OK):
```json
[
  {
    "id": 1,
    "name": "Main Warehouse",
    "location": "Downtown",
    "productId": 1,
    "quantity": 100
  }
]
```

### 2. Get Storages by Product ID
```bash
curl -X GET "http://localhost:8080/storages?productId=1"
```

### 3. Get Single Storage
```bash
curl -X GET http://localhost:8080/storages/1
```
Response (200 OK):
```json
{
  "id": 1,
  "name": "Main Warehouse",
  "location": "Downtown",
  "productId": 1,
  "quantity": 100
}
```

Response (404 Not Found):
```json
(empty body)
```

### 4. Create New Storage
```bash
curl -X POST http://localhost:8080/storages \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Secondary Warehouse",
    "location": "North Side",
    "productId": 1,
    "quantity": 50
  }'
```
Response (201 Created):
- Header: `Location: http://localhost:8080/storages/2`
- Body:
```json
{
  "id": 2,
  "name": "Secondary Warehouse",
  "location": "North Side",
  "productId": 1,
  "quantity": 50
}
```

Response (400 Bad Request - Product not found):
```json
(empty body)
```

### 5. Update Storage
```bash
curl -X PUT http://localhost:8080/storages/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Main Warehouse Updated",
    "location": "Downtown Center",
    "productId": 1,
    "quantity": 150
  }'
```
Response (200 OK):
```json
{
  "id": 1,
  "name": "Main Warehouse Updated",
  "location": "Downtown Center",
  "productId": 1,
  "quantity": 150
}
```

Response (404 Not Found - Storage doesn't exist):
```json
(empty body)
```

Response (400 Bad Request - Invalid product):
```json
(empty body)
```

### 6. Delete Storage
```bash
curl -X DELETE http://localhost:8080/storages/1
```
Response (204 No Content):
```
(no body)
```

Response (404 Not Found):
```
(no body)
```

## Error Scenarios

| Scenario | Status | Response |
|----------|--------|----------|
| Storage not found | 404 | Empty body |
| Product not found | 400 | Empty body |
| Missing required field in create/update | 400 | Validation error map |
| Invalid JSON | 400 | Parse error |

## Spring Boot Profile Testing

### Using Postman

1. **Create Collection**: "Storage API"
2. **Set Base URL**: `{{base_url}}`
3. **Add Variable**: `base_url = http://localhost:8080`

**Request Examples:**

```
GET /storages?productId=1
POST /storages
PUT /storages/1
DELETE /storages/1
```

### Using curl with Environment Variables

```bash
BASE_URL="http://localhost:8080"
PRODUCT_ID=1

# List all
curl -X GET $BASE_URL/storages

# Create
curl -X POST $BASE_URL/storages \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Warehouse A",
    "location": "Zone 1",
    "productId": '$PRODUCT_ID',
    "quantity": 100
  }'

# Update
curl -X PUT $BASE_URL/storages/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Warehouse A Updated",
    "location": "Zone 1",
    "productId": '$PRODUCT_ID',
    "quantity": 200
  }'

# Delete
curl -X DELETE $BASE_URL/storages/1
```

## Common Issues

### 1. 404 Storage Not Found
**Cause**: Invalid storage ID
**Solution**: Check if storage exists with `GET /storages`

### 2. 400 Product Not Found
**Cause**: Product ID doesn't exist
**Solution**: Create a product first with `POST /products`

### 3. Quantity doesn't change
**Cause**: May need to refresh or check in database
**Solution**: Verify with `GET /storages/{id}`

## Database Queries for Testing

### View all storages
```sql
SELECT * FROM storages;
```

### View storages for a product
```sql
SELECT * FROM storages WHERE product_id = 1;
```

### View storage with product details
```sql
SELECT s.*, p.name as product_name 
FROM storages s
JOIN products p ON s.product_id = p.id;
```

### Count storages
```sql
SELECT COUNT(*) FROM storages;
```

### Update quantity directly (for testing)
```sql
UPDATE storages SET quantity = 250 WHERE id = 1;
```

