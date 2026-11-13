# Inventory & Order Management System

A distributed, cloud-deployed microservices system built with **Java 17**, **Spring Boot**, and **PostgreSQL**. The platform decomposes identity management, catalog/stock control, order orchestration, and audit notifications into four independently deployable services communicating via synchronous REST APIs.

---

## System Architecture

~~~text
                        +-----------------------------------------+
                        |            Client / API Consumer         |
                        +-------+-------------+-------------+-----+
                                |             |             |
                  POST /auth    |  CRUD/Stock |  Orders     |
                                v             v             v
                     +--------------+ +--------------+ +--------------+
                     | auth-service | |  inventory-  | |    order-    |
                     |   (:8081)    | |   service    | |   service    |
                     |              | |   (:8082)    | |   (:8083)    |
                     +------+-------+ +------^-------+ +------+-------+
                            |                |                |
                            |         PATCH /deduct-stock     |
                            |         PATCH /restore-stock    |
                            |                +----------------+
                            |                                 |
                            |                        POST /notifications
                            |                                 |
                            |                                 v
                            |                        +--------------+
                            |                        |notification- |
                            |                        |   service    |
                            |                        |   (:8084)    |
                            |                        +------+-------+
                            v                                 v
                     +------------------------------------------------+
                     |      PostgreSQL (Database-per-Service)         |
                     +------------------------------------------------+
~~~

### Microservices Overview

| Service | Live Base URL | Responsibility | Key Endpoints |
| :--- | :--- | :--- | :--- |
| **`auth-service`** | `[https://auth-service-iuho.onrender.com](https://auth-service-iuho.onrender.com)` | User registration, credential verification, and token issuance | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| **`inventory-service`** | `[https://inventory-service-t8cw.onrender.com](https://inventory-service-t8cw.onrender.com)` | Product catalog management, SKU uniqueness, soft deletes, and atomic stock mutations | `POST/GET/PUT/DELETE /api/v1/products`, `PATCH /api/v1/products/{id}/deduct-stock`, `PATCH /api/v1/products/{id}/restore-stock`, `GET /api/v1/products/{id}/stock-check` |
| **`order-service`** | `[https://order-service-zkul.onrender.com](https://order-service-zkul.onrender.com)` | Order lifecycle orchestration, price calculation, compensating stock restoration, and notification triggers | `POST /api/v1/orders`, `GET /api/v1/orders`, `PATCH /api/v1/orders/{id}/cancel` |
| **`notification-service`** | `[https://notification-service-b8qi.onrender.com](https://notification-service-b8qi.onrender.com)` | Persistent audit log for order placement and cancellation events | `POST /api/v1/notifications`, `GET /api/v1/notifications` |

*(Interactive OpenAPI / Swagger UI documentation is exposed at `/swagger-ui/index.html` on each service).*

---

## Core Engineering Patterns

### 1. Database-per-Service Isolation
Each microservice owns its schema and PostgreSQL persistence layer via Spring Data JPA, preventing tight coupling at the data tier and allowing independent scaling and schema evolution.

### 2. Distributed Order & Compensating Stock Lifecycle
* **Order Creation (`POST /api/v1/orders`)**:
  1. Queries `inventory-service` (`GET /api/v1/products/{id}`) to validate active status, pricing, and available stock.
  2. Executes an atomic stock deduction (`PATCH /api/v1/products/{id}/deduct-stock`) before persisting the confirmed order.
  3. Dispatches an `ORDER_PLACED` event to `notification-service`.
* **Order Cancellation (`PATCH /api/v1/orders/{id}/cancel`)**:
  1. Validates that the order is not already cancelled.
  2. Invokes a compensating transaction (`PATCH /api/v1/products/{id}/restore-stock`) to return reserved units back to `inventory-service` before committing the `CANCELLED` state.
  3. Dispatches an `ORDER_CANCELLED` event to `notification-service`.

### 3. Resilient Inter-Service Communication
* **Critical Path Fail-Fast**: Stock validation, deduction, and restoration throw domain-specific exceptions (`InsufficientStockException`, `ServiceCommunicationException`) mapped to structured HTTP error payloads via `@RestControllerAdvice` (`GlobalExceptionHandler`), preventing overselling or inconsistent inventory states.
* **Non-Critical Fire-and-Forget**: Notification delivery is isolated in a fault-tolerant block so transient downstream delays in `notification-service` never roll back a confirmed or cancelled order transaction.

---

## Tech Stack

* **Language & Runtime**: Java 17
* **Framework**: Spring Boot 3.4, Spring Web, Spring Data JPA, Spring Validation
* **Database**: PostgreSQL
* **API Documentation**: Springdoc OpenAPI 2.8 (Swagger UI)
* **Build & Containerization**: Maven Wrapper (`./mvnw`), Multi-stage Docker builds
* **Cloud Hosting**: Render

---

## End-to-End Verification

An automated Bash smoke-test suite (`scripts/e2e-smoke-test.sh`) validates the entire live deployment across all four containers:

~~~bash
bash scripts/e2e-smoke-test.sh
~~~

**Verified Workflow:**
1. Polls `/actuator/health` across all 4 Render services until warm and healthy.
2. Registers a fresh user and authenticates via `auth-service`.
3. Seeds a new product (`quantityInStock: 10`) in `inventory-service`.
4. Places an order (`quantity: 2`) via `order-service` and asserts stock drops to `8`.
5. Cancels the order and asserts compensating stock restoration returns inventory to `10`.
6. Confirms both `ORDER_PLACED` and `ORDER_CANCELLED` records in `notification-service`.

---

## Local Development

~~~bash
# Compile individual services (requires JDK 17)
(cd inventory-service && ./mvnw test-compile)
(cd order-service && ./mvnw test-compile)
(cd auth-service && ./mvnw test-compile)
(cd notification-service && ./mvnw test-compile)
~~~
