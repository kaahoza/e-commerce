## Order Service

Places orders on behalf of a user. This is the service that actually talks
to the other two — it's the best place to look if you want to see the
inter-service communication and resilience patterns in this project.

## Port
`8083`

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/orders` | Place a new order |
| GET | `/api/orders/{id}` | Get an order by id |
| GET | `/api/orders?userId={id}` | Get all orders for a user |
| GET | `/api/orders` | List all orders |

## Sample request

```json
POST /api/orders
{
  "userId": 1,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

## How order placement works

1. Calls **User Service** to confirm the user exists.
2. For each line item, calls **Product Service** to fetch current price/stock
   and confirms enough stock is available.
3. Saves the order (status `CONFIRMED`) with a computed total.
4. Calls **Product Service** again to decrement stock for each item.

## Resilience

Calls to Product Service and User Service go through `WebClient`, wrapped in
**Resilience4j**:

- **Circuit breaker** — if a dependency's failure rate crosses the threshold,
  the breaker opens and further calls fail fast (returning a `503`) instead
  of piling up slow/hanging requests.
- **Retry** — transient failures get retried a few times before the circuit
  breaker fallback kicks in.

Config lives in `application.yml` under `resilience4j.circuitbreaker` and
`resilience4j.retry`. Circuit breaker health is exposed at
`/actuator/health` when the service is running.

This matters because in a real system, Product Service being briefly slow
shouldn't take down order placement entirely — it should degrade gracefully
and recover once the dependency is healthy again.

## Running locally

Product Service and User Service must be running first (or reachable at the
URLs configured via `PRODUCT_SERVICE_URL` / `USER_SERVICE_URL`).

```bash
mvn spring-boot:run
```

## Testing

```bash
mvn test
```

`OrderServiceTest` mocks `ProductClient` and `UserClient` so the service
layer's logic (stock checks, total calculation, error handling) is tested
without needing the other services running.
