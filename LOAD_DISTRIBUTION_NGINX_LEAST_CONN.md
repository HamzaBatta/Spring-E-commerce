# Load Distribution with Nginx (least_conn)

## Goal
Distribute traffic across multiple app instances and avoid overloading one node.

## Solution in this project
- Run three app instances on ports 8081, 8082, 8083.
- Use Nginx with the `least_conn` algorithm to route requests.

## Why least_conn fits this project
- Order creation can take time (locks + queue), so connections stay open.
- `least_conn` sends new requests to the least busy instance.
- Works well for mixed load and uneven request times.

## Example Nginx config
```
upstream ecommerce_backend {
    least_conn;
    server 127.0.0.1:8081;
    server 127.0.0.1:8082;
    server 127.0.0.1:8083;
}

server {
    listen 80;
    location / {
        proxy_pass http://ecommerce_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## How to run
- Start three app instances with profiles `instance1`, `instance2`, `instance3`.
- Start Nginx.
- Send requests to `http://localhost`.

## How to verify
- Use Postman Runner or a burst of requests to `/orders/test/concurrency`.
- Check logs in each instance; requests should be distributed across ports.

