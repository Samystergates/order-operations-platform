# API Contracts

## Order Platform API

Base path: `/api`

Route groups:

- `/v1/auth/*` — authentication, current-user details, and account operations
- `/index/*` — order loading, refresh, updates, exports, and custom orders
- `/archive/*` — archived-order operations
- `/transport/*` — transport planning and route execution
- `/transport/info/*` — route, driver, truck, and trailer data
- `/wheels/*` — wheel machine sizes and colors
- `/wheels/flow/*` — SME/SPU workflows and PDF output
- `/ser/*` — service mechanics workflow and PDF output
- `/sticker/*` — sticker-label management and printing
- `/user/*` — user and department administration

Protected endpoints use JWT bearer authentication. Login and WebSocket connections are public; user registration requires the `ADMIN` role.

### Write operations and compatibility

- `POST /index/refresh/orders` is the primary mutating CRM refresh route. The legacy GET route remains available for compatibility.
- Administrative maintenance actions use `POST /v1/auth/maintenance/*`.
- Legacy `GET /v1/auth/update-tekst` and `POST /v1/auth/maintenance/update-tekst` return `410 Gone`. Product-note synchronization is part of the order refresh flow.

### WebSocket

- SockJS endpoint: `/ws`
- Broker topics: `/topic/orderUpdate`, `/topic/smeSave`, `/topic/smeUpdate`, `/topic/smeDelete`, `/topic/spuSave`, `/topic/spuUpdate`, and `/topic/spuDelete`

## ERP Adapter API

Base path: `/api/feign`

The main API uses these integration endpoints:

- `GET /refresh/orders`
- `POST /mark/expired`
- `POST /check/existence`
- `POST /delete/orders`
- `POST /update/product/notes`
- `POST /update/mon/sub`

## Error response

API errors use a consistent response shape:

```json
{
  "timestamp": "2026-06-18T09:00:00",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "path": "/api/example",
  "traceId": "request-or-generated-id",
  "errors": {
    "field": "reason"
  }
}
```

## API documentation

The Order Platform API exposes generated OpenAPI documents at `/api-docs` and Swagger UI at `/swagger-ui.html`.
