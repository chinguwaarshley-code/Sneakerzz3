# SneakyLink — Spring Boot Footwear E-Commerce Platform

A full-stack Java 25 / Spring Boot 3.5 e-commerce platform for footwear featuring a clean black and blue interface, role-based Customer/Manager authentication, a persistent H2 database seeded with **48 distinct shoe models**, dynamic multi-attribute filtering, persistent shopping cart, streamlined checkout with dynamic delivery dates & in-store pickup windows, and a protected Manager Inventory Backend.

## Quick Start

### Prerequisites
- **Java 25 (LTS)**
- **Maven 3.9+**

### Run Locally
```bash
mvn spring-boot:run
```
Then open: **http://localhost:8080**

### Default Credentials
- **Customer Portal**: No password required (enter an optional display name or continue as guest).
- **Manager Portal**: Password `SneakyAdmin2026!` (configurable in `src/main/resources/application.properties` via `sneakylink.manager.password`).
- **H2 Database Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/sneakylinkdb`, User: `sa`, Password: empty).

### Run Tests
```bash
mvn test
```

## Architecture Overview
- **Backend**: Spring Boot 3.5.16, Spring Web, Spring Data JPA (`JpaSpecificationExecutor`), Jakarta Bean Validation, Persistent H2 Database.
- **Database Seeding**: `DataInitializer` automatically seeds **48 footwear models** (`SL-001` through `SL-048`) with distinct colorways, size ranges, materials, pricing, stock levels, and custom colorway-matched SVG product imagery.
- **Role-Based Auth**: `AuthService` & `AuthController` manage session-backed Customer and Manager roles, using constant-time password validation for Manager access and enforcing role checks on `/api/admin/**`.
- **Dynamic Fulfillment**: `FulfillmentService` computes real upcoming business-day delivery windows (Standard 3–5 business days, Express 1–2 business days) and same-day/next-day 2-hour in-store pickup slots based on the current server time.
- **Frontend**: Responsive black & blue SPA served from `src/main/resources/static/` (`index.html`, `css/styles.css`, `js/app.js`).
