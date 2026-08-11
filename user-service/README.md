# User Service

Handles registration, authentication, and user lookups for MiniShop.
Passwords are hashed with BCrypt; authenticated endpoints use stateless JWTs.

## Port
`8082`

## Endpoints

| Method | Path | Auth required | Description |
|---|---|---|---|
| POST | `/api/users/register` | No | Create a new user |
| POST | `/api/users/login` | No | Log in, returns a JWT |
| GET | `/api/users/{id}` | Yes | Fetch a user by id |

## Sample requests

**Register**
```json
POST /api/users/register
{
  "username": "jane",
  "email": "jane@example.com",
  "password": "password123"
}
```

**Login**
```json
POST /api/users/login
{
  "username": "jane",
  "password": "password123"
}
```
Response:
```json
{
  "token": "eyJhbGciOi...",
  "username": "jane",
  "userId": 1
}
```

Use the returned token as `Authorization: Bearer <token>` on protected endpoints.

## Security notes

- Passwords are never stored or returned in plaintext.
- JWT secret is read from the `JWT_SECRET` env var — the value in
  `application.yml` is a placeholder for local dev only and should never be
  used in a real deployment.
- Token expiry is configurable via `JWT_EXPIRATION_MS` (default: 1 hour).

## Running locally

```bash
mvn spring-boot:run
```

## Testing

```bash
mvn test
```
