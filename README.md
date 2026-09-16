# Couple Verification Platform — Backend

A Spring Boot REST API for managing and verifying relationship status between two consenting users. Couples opt in together (mutual PIN-based acceptance), and the platform tracks relationship health, promises, dreams, and eventually offers a privacy-safe public verification lookup.

> **Status: ~70% complete.** Auth, User, and Couple modules are implemented and working. Promise, Dream, Restriction, Conflict, Breakup, KYC, Health Scoring, Dashboard, Timeline, Verification, and Admin modules are planned but not yet built. See [Roadmap](#roadmap) below.

---

## Features

### Implemented
- **JWT authentication** — stateless auth via a custom `JwtAuthenticationFilter`, tokens validated on every request.
- **Role-based authorization** — `ROLE_USER`, `ROLE_ADMIN`, `ROLE_MODERATOR`, enforced at the filter-chain level via `SecurityConfig`, with `@EnableMethodSecurity` in place for endpoint-level `@PreAuthorize` checks.
- **User registration & login** — age verification (18+ required), duplicate-email checks, BCrypt password hashing.
- **Mutual-consent couple linking**:
  - One user sends a couple request with a shared PIN.
  - The partner must accept using the same PIN before the couple becomes `ACTIVE`.
  - PINs are BCrypt-hashed, never stored in plaintext.
  - Self-coupling, duplicate requests, and "already in an active relationship" cases are all guarded against.
- **Relationship health & loyalty scoring** — numeric fields on the `Couple` entity, updatable via service methods.

### Planned (not yet implemented)
| Module | Purpose |
|---|---|
| Promise | Mutual-approval promises between partners |
| Dream | Shared goals, no approval required |
| Restriction | Time-bound relationship restrictions |
| Conflict | Issue raising with a cooling-off period |
| Breakup | Breakup handling with a patch-up cooldown |
| KYC | Annual "are you still together" verification |
| Health Service | Scheduled, activity-based health recalculation |
| Dashboard | Aggregated relationship summary |
| Timeline | Relationship history / milestones |
| **Verification** | Public, privacy-safe status lookup by couple ID + PIN (no personal data returned) |
| Admin | User/couple management, platform stats |

---

## Tech Stack

- **Java 17**, **Spring Boot 4**
- Spring Security + JWT (`io.jsonwebtoken` / jjwt 0.12.6)
- Spring Data JPA / Hibernate
- MySQL
- Lombok
- Maven (packaged as **WAR**, run on an external Tomcat container)
- Docker & Kubernetes for deployment

---

## Known Issues

Being upfront about the current state so nothing here is a surprise in review:

1. **`relationshipStatus` is computed but not attached to the response.** Both `login()` and `register()` in `AuthServiceImpl` build a `RelationshipStatusInfo` object but never call it on the `AuthResponse` builder — clients currently get no relationship status back on login/register.
2. **Secrets are hardcoded in `application.properties`** (DB password, JWT secret) in source. Must be moved to environment variables before any real deployment — see [Configuration](#configuration).
3. **`respondToCoupleRequest` returns `null` on rejection** instead of a typed response — callers need to handle this explicitly.
4. **`Couple.status` is a raw `String`**, not an enum — typo-prone.
5. No tests currently exercise the security filter chain or couple-linking flow.

---

## Getting Started

### Prerequisites
- JDK 17
- Maven
- MySQL 8, or Docker

### Local run

```bash
# clone and enter the repo
git clone https://github.com/couple-varification-app/couple-varification-backend.git
cd couple-varification-backend

# build
./mvnw clean package -DskipTests

# run (expects MySQL reachable at the URL in application.properties)
./mvnw spring-boot:run
```

### Configuration

By default, `application.properties` contains hardcoded local dev values. For anything beyond local development, override these via environment variables:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/couple_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

**Rotate the credentials currently in the repo before deploying anywhere shared.**

### Run with Docker Compose

```bash
docker compose up --build
```

This starts MySQL and the app together. The API will be available at `http://localhost:8080`.

### Deploy to Kubernetes

```bash
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/app-deployment.yaml
kubectl get pods
kubectl get svc couple-verification-service
```

---

## API Overview

| Endpoint | Access | Description |
|---|---|---|
| `POST /api/auth/register` | Public | Register a new user (18+ only) |
| `POST /api/auth/login` | Public | Log in, receive JWT |
| `POST /api/couples` | User | Send a couple request with a shared PIN |
| `POST /api/couples/respond` | User | Accept/reject a pending couple request |
| `GET /api/couples/{coupleId}` | User | Get couple details |
| `GET /api/couples/all` | Admin | List all couples |
| `GET /api/verification/**` | Public | *(planned)* Privacy-safe status lookup |

---

## Roadmap

Remaining work, roughly in priority order:

1. **Promise module** — mutual-approval promises
2. **Dream module** — shared goals
3. **Restriction module** — time-bound restrictions
4. **Conflict module** — cooling-off period, resolution tracking
5. **Breakup module** — breakup + patch-up cooldown
6. **KYC module** — annual re-verification
7. **Health calculation service** — scheduled scoring
8. **Verification module** — the core "verification" feature the project is named for
9. **Dashboard & Timeline**
10. **Admin controller**

Estimated remaining effort: ~25–30 hours.

---

## License

Not yet specified.
