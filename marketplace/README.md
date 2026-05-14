# On-Demand Home Services Marketplace
## DS Assignment 2 - Distributed Systems

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    REST Clients (Postman)                │
└──────────────┬─────────────────────┬────────────────────┘
               │                     │
    ┌──────────▼──────────┐ ┌────────▼────────┐ ┌─────────────────┐
    │   User Service      │ │  Offer Service  │ │ Booking Service │
    │   Port: 8081        │ │  Port: 8082     │ │  Port: 8083     │
    │   DB: H2 (userdb)   │ │  DB: H2(offerdb)│ │  DB: H2(bookdb) │
    │                     │ │                 │ │                 │
    │  EJBs:              │ │                 │ │                 │
    │  - Stateful Bean    │ │                 │ │                 │
    │    (UserSessionBean)│ │                 │ │                 │
    │  - Singleton Bean   │ │                 │ │                 │
    │   (SystemStatsBean) │ │                 │ │                 │
    └──────────┬──────────┘ └────────┬────────┘ └────────┬────────┘
               │                     │                   │
               └─────────────────────┴──────────┬────────┘
                                                 │
                                   ┌─────────────▼──────────────┐
                                   │          RabbitMQ           │
                                   │                             │
                                   │  Exchanges:                 │
                                   │  - notifications.exchange   │
                                   │    (Fanout) → customer +    │
                                   │    provider queues          │
                                   │  - payments.exchange        │
                                   │    (Direct, key=PaymentFailed│
                                   │    → admin queue)           │
                                   └─────────────────────────────┘
```

---

## EJB Implementation

### Stateful Bean: `UserSessionBean` (user-service)
- **Location:** `user-service/.../ejb/UserSessionBean.java`
- **Purpose:** Manages user login sessions. Each session holds its own state (logged-in user data).
- **Methods:** `createSession()`, `getUserFromSession()`, `removeSession()`
- **Used in:** `UserService.login()` and `UserService.logout()`

### Singleton Bean: `SystemStatsBean` (user-service)
- **Location:** `user-service/.../ejb/SystemStatsBean.java`
- **Purpose:** Single instance tracks system-wide statistics (total registrations, logins, etc.)
- **Methods:** `incrementRegistration()`, `incrementLogin()`, `getSystemStats()`
- **Used in:** `UserService.registerCustomer()`, `registerProvider()`, `login()`

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java (JDK)  | 17+     |
| Maven       | 3.8+    |
| RabbitMQ    | 3.x+    |

---

## Setup & Run Instructions

### Step 1: Install and Start RabbitMQ

#### Option A – Docker (recommended):
```bash
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management
```
RabbitMQ Management UI: http://localhost:15672 (guest/guest)

#### Option B – Local install:
- Windows: https://www.rabbitmq.com/install-windows.html
- Mac: `brew install rabbitmq && brew services start rabbitmq`
- Ubuntu: `sudo apt install rabbitmq-server && sudo service rabbitmq-server start`

---

### Step 2: Build All Services

Open **3 separate terminals**.

**Terminal 1 – User Service:**
```bash
cd user-service
mvn clean install -DskipTests
mvn spring-boot:run
```
Service starts on: http://localhost:8081
H2 Console: http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:userdb`)

**Terminal 2 – Service Offer Service:**
```bash
cd service-offer-service
mvn clean install -DskipTests
mvn spring-boot:run
```
Service starts on: http://localhost:8082
H2 Console: http://localhost:8082/h2-console (JDBC URL: `jdbc:h2:mem:offerdb`)

**Terminal 3 – Booking Service:**
```bash
cd booking-service
mvn clean install -DskipTests
mvn spring-boot:run
```
Service starts on: http://localhost:8083
H2 Console: http://localhost:8083/h2-console (JDBC URL: `jdbc:h2:mem:bookingdb`)

> **Important:** Start services in the order above. User Service must be up before Offer Service creates offers (it validates provider IDs via REST).

---

## Testing with Postman

1. Open Postman
2. Import `docs/postman_collection.json`
3. The collection includes a **"Full Scenario Flow"** folder — run it top-to-bottom to reproduce the exact assignment example scenario

### Quick Test Scenario:
```
1. Register Customer Alice with $200
2. Register Provider Bob (Plumber)
3. Bob creates offer: Plumbing repair $80
4. Alice searches for Plumbing
5. Alice books the offer
   → Wallet: $200 - $80 = $120
   → Both Alice and Bob notified via RabbitMQ
6. Check notifications at:
   GET /api/users/1/notifications  (customer)
   GET /api/offers/provider/2/notifications  (provider)
```

---

## API Reference

### User Service (port 8081)

| Method | Endpoint | Description | FR |
|--------|----------|-------------|-----|
| POST | `/api/users/register/customer` | Register customer with initial wallet balance | Customer 1,2 |
| POST | `/api/users/register/provider` | Register provider with profession type | Provider 4 |
| POST | `/api/users/login` | Login – creates Stateful EJB session | Provider 5 |
| POST | `/api/users/logout` | Logout – removes Stateful EJB session | – |
| POST | `/api/users/{id}/wallet/add` | Add funds to wallet | Customer 3 |
| GET | `/api/users/{id}/wallet` | View wallet balance | Customer 4 |
| GET | `/api/users/{id}` | Get user by ID (internal) | – |
| GET | `/api/users/admin/all` | View all registered users | Admin 1 |
| GET | `/api/users/admin/stats` | System stats from Singleton EJB | – |
| GET | `/api/users/{id}/notifications` | Customer booking notifications | Customer 7 |
| GET | `/api/users/admin/notifications` | Admin payment failure notifications | – |

### Service Offer Service (port 8082)

| Method | Endpoint | Description | FR |
|--------|----------|-------------|-----|
| POST | `/api/offers/categories` | Add service category | Admin 3 |
| GET | `/api/offers/categories` | List all categories | – |
| POST | `/api/offers` | Create service offer | Provider 6 |
| GET | `/api/offers/active` | View all active offers | Provider 7 |
| PUT | `/api/offers/{id}?providerId={id}` | Update offer price/availability | Provider 8 |
| GET | `/api/offers/provider/{id}/completed` | View completed services | Provider 9 |
| GET | `/api/offers/{id}` | Get offer by ID | – |
| GET | `/api/offers/category/{name}` | Browse by category | Customer 5 |
| GET | `/api/offers/provider/{id}/notifications` | Provider booking notifications | Provider 10 |

### Booking Service (port 8083)

| Method | Endpoint | Description | FR |
|--------|----------|-------------|-----|
| POST | `/api/bookings` | Create booking (with full payment flow) | Customer 6 |
| GET | `/api/bookings/customer/{id}` | View booking history | Customer 7b |
| GET | `/api/bookings/admin/all` | All transaction history | Admin 2 |
| GET | `/api/bookings/{id}` | Get booking by ID | – |

---

## RabbitMQ Design

### Exchanges & Queues

| Exchange | Type | Purpose |
|----------|------|---------|
| `notifications.exchange` | **Fanout** | Broadcast booking confirmations to ALL bound queues (customer + provider simultaneously) |
| `payments.exchange` | **Direct** | Route `PaymentFailed` events ONLY to admin queue using routing key |

| Queue | Bound To | Consumer |
|-------|----------|---------|
| `booking.confirmed.customer` | notifications.exchange (fanout) | User Service – customer notifications |
| `booking.confirmed.provider` | notifications.exchange (fanout) | Offer Service – provider notifications |
| `booking.rejected.customer` | Direct (no exchange) | User Service – rejection notifications |
| `payment.failed.admin` | payments.exchange (routing key: `PaymentFailed`) | User Service – admin PaymentFailed notifications |

### Booking Flow with RabbitMQ

```
Customer Books →
  ├── Balance OK?
  │   ├── YES → Deduct wallet → Mark offer BOOKED → Save booking
  │   │         → Publish to notifications.exchange (Fanout)
  │   │              ├── booking.confirmed.customer ← Customer notified
  │   │              └── booking.confirmed.provider ← Provider notified
  │   │
  │   └── NO  → Save REJECTED booking
  │             → Publish to booking.rejected.customer ← Customer notified
  │             → Publish to payments.exchange (key=PaymentFailed) ← Admin notified
```

---

## Assumptions & Design Decisions

1. **No JWT/Spring Security:** Authentication is simplified using UUID session tokens stored in the Stateful EJB (UserSessionBean). For production, we would use JWT.
2. **H2 in-memory DB:** Each service uses its own isolated H2 database to strictly enforce microservice DB isolation. Data resets on restart (by design for this assignment).
3. **EJB Simulation:** EJBs are implemented using Jakarta EJB annotations (`@Stateful`, `@Singleton`, `@Startup`) within a Spring Boot container. In a full Java EE environment, these would run in an EJB container (like WildFly). The behaviors (session isolation for Stateful, single shared instance for Singleton) are preserved.
4. **REST for inter-service communication:** Services communicate exclusively via REST (no shared databases). Booking Service calls User Service to validate customers and deduct wallets, and calls Offer Service to validate and lock offers.
5. **Default Categories:** The Offer Service pre-creates 5 default categories on startup: Plumbing, Carpentry, Electrical, Cleaning, Painting.
6. **Default Admin:** User Service creates a default admin user on startup: `admin` / `admin123`.
7. **Rollback strategy:** If wallet deduction succeeds but offer locking fails, the booking service explicitly refunds the wallet via REST before marking the booking as REJECTED.
