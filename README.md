# EstateX – A Polish Property Marketplace

## 🎯 Project Vision

**EstateX** is a comprehensive MVP of a property marketplace platform for Poland, designed to demonstrate modern full-stack development practices with an **AI-first testing philosophy**. The project showcases how to build a production-grade application using clean architecture, comprehensive test coverage, and automated bug detection through well-crafted tests.

The primary goal was to fully **vibe code** an application that could actually serve Polish users looking to rent or sell properties.

---

## 🏗️ Business Context

### What is EstateX?

EstateX is a Polish real-estate marketplace where:

- **Property Owners** can create and manage listings for properties they want to rent or sell
- **Property Seekers** can browse, filter, and search for properties across Poland
- **Users** can save favorite listings, chat directly with property owners, and view analytics
- **Photos** are centrally managed with cover photo mechanics and storage optimization
- **Messaging** happens in thread-based conversations scoped to specific listings

### Core Features

1. **User Management** — Registration, profile updates, public profiles with listing counts
2. **Listing Operations** — Create, update, delete, search with robust filtering (location, price, property type, dimensions)
3. **Media Management** — Upload up to 20 photos per listing with automatic and manual cover mechanics
4. **Chat & Messaging** — Conversation threads between buyers and owners with real-time WebSocket messaging
5. **Favorites Engine** — Bookmark and manage favorite listings (idempotent operations)
6. **Real-time Analytics** — Owners see aggregated view counts across their active listings

---

## 🛠️ Technology Stack

### Backend
- **Language:** Java 21
- **Framework:** Spring Boot 3.3.4 (latest stable)
- **Build Tool:** Gradle 8.10 (Kotlin DSL)
- **Database:** PostgreSQL 16 (Alpine)
- **Migrations:** Flyway (managed by Spring Boot)
- **ORM:** Hibernate + Spring Data JPA
- **Mapping:** MapStruct 1.5.5 (strict entity ↔ DTO mapping)
- **Real-time:** Spring WebSocket + STOMP + SockJS
- **Architecture Pattern:** Hexagonal (Ports & Adapters)

### Frontend
- **Language:** TypeScript 5.9+
- **Framework:** React 19
- **Build Tool:** Vite 8
- **Router:** react-router-dom 7
- **State Management:** Zustand 5
- **Data Fetching:** TanStack React Query 5
- **HTTP Client:** Axios 1.14 (with custom interceptors for auth)
- **WebSocket Client:** @stomp/stompjs 7.3 + sockjs-client 1.6
- **Icons:** Lucide React 1.7

### Testing & Quality
- **Backend Unit Tests:** JUnit 5 + Mockito
- **Backend Integration Tests:** Testcontainers + Spring Boot Test (PostgreSQL)
- **Backend Mutation Testing:** PiTest 1.2.1
- **Backend Coverage:** JaCoCo
- **Frontend Unit Tests:** Vitest 4.1 + Testing Library 16.3 + MSW 2.13
- **Frontend E2E Tests:** Playwright 1.59
- **Frontend Mutation Testing:** Stryker

### Infrastructure
- **Containerization:** Docker + Docker Compose 3.9
- **Reverse Proxy:** Nginx (serves SPA, proxies /api/ and /ws/)
- **Development Tooling:** Gradle wrapper, npm/node

---

## 🏛️ Architecture Overview

### Design Pattern: Hexagonal Architecture (Ports & Adapters)

EstateX is built as a **modular monorepo** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────┐
│        React 19 SPA (TypeScript)                    │
│   Nginx proxy: /api/ → backend, /ws/ → WebSocket    │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP REST + WebSocket (STOMP/SockJS)
┌──────────────────▼──────────────────────────────────┐
│     adapter-web: Controllers, DTOs, Config          │
│  REST endpoints, WebSocket STOMP, Exception handlers│
│  LocalFileStorageAdapter, CORS config, Swagger docs │
└──────────────────┬──────────────────────────────────┘
┌──────────────────▼──────────────────────────────────┐
│     application: Services & Ports,
│  Commands, Result DTOs, use-case orchestration      │
└──────────────────┬──────────────────────────────────┘
┌──────────────────▼──────────────────────────────────┐
│  domain: Pure Java (zero framework deps)            │
│  Aggregates, Value Objects, Enums,                  |
|  Domain Rules, Repository Ports,                    │
│  Exception hierarchy, business logic                │
└──────────────────┬──────────────────────────────────┘
┌──────────────────▼──────────────────────────────────┐
│  adapter-persistence: JPA & Flyway                  │
│  JPA Entities, Spring Data Repositories             │
│  Repository Adapters (implement domain ports)       │
│  Flyway migrations                                  │
│  PostgreSQL integration layer                       │
└─────────────────────────────────────────────────────┘
```

### Key Architectural Principles

1. **Dependency Inversion** — High-level modules (domain) never depend on low-level modules (persistence, web)
2. **Port-Adapter Pattern** — Services declare `FileStoragePort`; adapters implement it
3. **Unidirectional Dependencies** — Layers flow: domain ← application ← adapters
4. **Zero Framework in Domain** — Business logic is pure Java, testable without Spring
5. **Simplified Auth** — Header-based (`X-User-Id`), no JWT/OAuth complexity for MVP

---

## 🧪 Testing Philosophy: AI-Driven Quality Assurance

### Core Principle

**Tests are not just for regression prevention — they are instructions for AI agents to identify bugs, issues, and incorrect behavior independently, without manual testing.**

Every test is written with the assumption that an AI model will:
1. **Run appropriate tests** whenever source code or tests are modified
2. **Analyze test failures** to diagnose root causes
3. **Detect logical errors** and edge cases
4. **Suggest fixes** based on test expectations

### Test Coverage Strategy

| Component | Strategy | Threshold |
|-----------|----------|-----------|
| **Domain Layer** | Exhaustive unit tests (no external deps) | 100% line + branch coverage, 100% mutation threshold |
| **Application Layer** | Acceptance tests with in-memory fakes | 100% coverage, 100% mutation threshold |
| **Persistence Layer** | Testcontainers integration tests (real PostgreSQL) | ≥95% coverage, E2E validation |
| **Web Layer** | @WebMvcTest slice tests (no real DB) | ≥90% coverage, contract verification |
| **Backend E2E Integration** | Full-stack tests with Testcontainers | All critical user journeys covered |
| **Frontend Unit** | Component + hook tests with MSW mocks | ≥85% coverage |
| **Frontend E2E** | Playwright user journeys | Critical workflows |
| **Frontend Mutation** | Stryker (api & store only) | 100% mutation score |

### Running Tests by Change Type

The project uses a **minimal test matrix** to avoid unnecessary overhead:

| Changed File Path | Run These Tests |
|---|---|
| `domain/src/main/**` | `:domain:test` + `:application:test` |
| `application/src/main/**` | `:application:test` |
| `adapter-persistence/src/main/**` | `:adapter-persistence:test` + `:e2e:test` |
| `adapter-web/src/main/**` | `:adapter-web:test` (+ `:e2e:test` if API contracts change) |
| `e2e/**` or Flyway migrations | `:e2e:test` |
| Multiple modules | `./gradlew test` (full suite) |
| `frontend/src/**` | `npm test` (Vitest) |
| `frontend/src/api/**` or `frontend/src/store/**` | `npm test` + `npm run test:mutation` |
| `frontend/e2e/**` | `npm run test:e2e` (Playwright) |

---

## ⚡ Quick Start

### Prerequisites
- Docker & Docker Compose
- Node.js 22+, npm 10+
- Java 21 (if running backend locally)
- Gradle 8.10 (included via wrapper)

### Option 1: Full Stack (Docker Compose)

```bash
docker compose up
# Backend: http://localhost:8080
# Frontend: http://localhost
# Database: localhost:5432
```

### Option 2: Local Development

**Terminal 1 — Database:**
```bash
docker compose up postgres
```

**Terminal 2 — Backend:**
```bash
cd backend
./gradlew :adapter-web:bootRun
# Runs on http://localhost:8080
```

**Terminal 3 — Frontend:**
```bash
cd frontend
npm install && npm run dev
# Runs on http://localhost:5173
```

### Running Tests

**Backend (all modules):**
```bash
cd backend
./gradlew test
```

**Backend (specific module):**
```bash
cd backend
./gradlew :domain:test          # Domain layer only
./gradlew :application:test     # Application + domain
./gradlew :adapter-web:test     # Web slice tests
./gradlew :e2e:test             # Full integration tests
```

**Frontend (unit tests):**
```bash
cd frontend
npm test
```

**Frontend (unit + mutation):**
```bash
cd frontend
npm test && npm run test:mutation
```

**Frontend (E2E):**
```bash
cd frontend
npm run test:e2e
```

---

## 🎯 Future Enhancements

Potential features (not yet implemented):
- Advanced search with saved filters
- Email notifications
- Two-factor authentication
- Report/flagging system
- Image optimization & CDN integration
