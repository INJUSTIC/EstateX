# EstateX (estateX) — Project Knowledge Base

> **Purpose:** A Polish real-estate marketplace (MVP) — users list properties for rent/sale, browse listings, chat with owners, and save favourites.

---

# Table of contents

0. [Knowledge base maintenance](#knowledge-base-maintenance)
1. [Project functional requirements](#project-functional-requirements)
2. [Architecture overview](#architecture-overview)
3. [Backend technical reference](#backend-technical-reference)
4. [Frontend technical reference](#frontend-technical-reference)
5. [API contract reference](#api-contract-reference)
6. [Data model reference](#data-model-reference)
7. [Infrastructure & deployment](#infrastructure--deployment)
8. [Development standards](#development-standards)
9. [Known quirks & caveats](#known-quirks--caveats)

---

# Knowledge base maintenance

> **Directive:** Any agent that discovers information during task execution that contradicts, extends, or clarifies what is documented here **must update this file** before ending its turn. Keep updates minimal and surgical — only change the affected section(s).

## When to update

| Trigger | Section(s) to update |
|---|---|
| A new endpoint is added or an existing one changes its path, method, request, or response shape | [API contract reference](#api-contract-reference) |
| A domain aggregate, value object, field, or business rule changes | [Backend technical reference → Domain module](#domain-module) |
| A service method is added, removed, or its signature changes | [Backend technical reference → Application module](#application-module) |
| A JPA entity column, Flyway migration, or DB constraint is added or changed | [Backend technical reference → Persistence module](#persistence-module) |
| A new Spring config class, controller, or global handler is added or changed | [Backend technical reference → Web module](#web-module) |
| A new frontend page, component, route, or API function is added or changed | [Frontend technical reference](#frontend-technical-reference) |
| A field name mismatch between backend and frontend is discovered or fixed | [Data model reference](#data-model-reference) |
| A new enum value is added on either side | [Data model reference → Enum value differences](#enum-value-differences) |
| A new service is added to docker-compose or a Dockerfile changes | [Infrastructure & deployment](#infrastructure--deployment) |
| A known quirk is resolved or a new one is discovered | [Known quirks & caveats](#known-quirks--caveats) |
| A new test class or test module is added, or coverage thresholds change | [Development standards](#development-standards) |

## How to update

1. **Locate the exact table row, bullet point, or paragraph** that needs changing — do not rewrite entire sections.
2. **Use `replace_string_in_file` or `multi_replace_string_in_file`** to make targeted edits with sufficient context lines.
3. **Add newly discovered quirks** to [Known quirks & caveats](#known-quirks--caveats) as a numbered bullet — never delete existing entries unless the quirk is fully resolved (in that case, replace it with a struck-through note and resolution summary).
4. **Do not update** formatting, punctuation, or wording that is not related to the factual change.
5. After updating, run a quick self-check: does the changed entry still accurately describe the current codebase state?

## What NOT to update

- Do not rewrite prose style, headings, or the overall document structure.
- Do not add speculative or unverified information — only document what you have confirmed by reading source files or running the code.
- Do not add feature plans or roadmap items — this is a factual reference, not a design document.

---

# Project functional requirements

## 1. User Management
* **UC-1.1 Registration:** New users can register for the platform using an email address and a display name. Registration enforces email uniqueness across the system. Newly created users are placed in an `ACTIVE` state.
* **UC-2.1 Profile Management:** Users can update their profile information, specifically their `displayName` and `phone` number.
* **UC-2.2 Public Profiles:** Users have a public profile detailing their public information, which also keeps track of their total active listing count.
* **UC-10.2 Administration:** System administrators have the ability to deactivate an existing user.

## 2. Listing Operations
* **UC-3.1 Creation:** Users can create property listings. A listing encapsulates:
   * **Location Data:** Street, City, Voivodeship, Postal Code, Country (defaults to Poland), Geolocation (Latitude/Longitude).
   * **Categorization:** Property Type (e.g., apartment, house) and Transaction Type (Rent or Sale).
   * **Financials:** Strictly encapsulated price amounts in Polish Złoty (PLN). Negative amounts are rejected. Multiple currencies are not supported for the MVP.
   * **Dimensions:** Total Area (SqMeters) and Number of Rooms.
* **UC-3.3 Updating:** Only verified listing owners can mutate the core details of an active listing.
* **UC-3.4 Deletion:** Listings can be deleted strictly by the designated owner or by a system administrator. Deleting a listing cascades to remote file storage, automatically cleaning up associated images.
* **UC-3.5 Lifecycle:** The listing owner dictates the state lifecycle (e.g., Active, Archived).
* **UC-4 Discovery & Search:** Listings can be queried and paginated based on robust filters (`ListingSearchCriteria`).
* **UC-4.6 Details Validation & Views:** Requesting an individual listing's details strictly verifies its existence and automatically increments its internal `viewCount` tracker by exactly one.

## 3. Media & Photography
* **UC-3.2 Photo Management:** Property visuals are strictly capped at a **maximum of 20 photos per listing** to optimize storage and load speeds.
* **Cover Mechanics:** 
   * The first photo uploaded to a blank listing is automatically promoted to the Cover Photo.
   * If the active Cover Photo is explicitly deleted, the next photo sequentially is promoted automatically to assume Cover Photo status.
   * Owners can manually dictate which photo acts as the Cover Photo (this action inherently demarcates all other photos).

## 4. Chat & Messaging
* **UC-5.1 Conversation Threads:** A buyer (Initiator) can open a context-bound thread to communicate with a Listing Owner. 
   * **Rule:** An Initiator cannot open duplicate conversations against the same listing. Identical requests yield the existing context.
* **UC-5.2 Messaging:** Participants can send messages containing either text `content` or file `attachments`. 
   * **Rule:** A message cannot be empty (blank content without an attachment is automatically rejected by the domain aggregate).
   * **Security:** A user cannot interact with or read threads unless they are strictly the verified Initiator or the Listing Owner.
* **UC-5.3 Inbox Analytics:** Users can view their inbox containing active conversation summaries coupled dynamically with unread outbound message badges. Fetching a conversation automatically flags internal unread messages as `isRead=true`.

## 5. Favorites Engine
* **UC-6.1 Save to Favorites:** Users can bookmark property listings. The operation is designed to be idempotent; duplicate requests safely return the existing favorite without side effects.
* **UC-6.2 Removal:** Bookmarks can be reversed via ID.
* **UC-6.3 Library:** Users can query their favorited catalogue.

## 6. Real-time Analytics
* **UC-11 Dashboard:** Owners can fetch aggregated statistical snapshots containing raw cumulative view-counts bound dynamically against their list of active properties.

# Development standards

## Testing strategy by change type

For every code change, run the **minimum set** of tests that provides confidence without unnecessary overhead.

### Domain model changes
(`domain/src/main/java/**` — entities, value objects, enums, domain exceptions, `ListingSearchCriteria`, etc.)
- Run: `:domain:test`
- If the change affects business rules that flow into services, also run: `:application:test`

### Application service changes
(`application/src/main/java/**` — `UserService`, `ListingService`, `ChatService`, etc.)
- Run: `:application:test`
- This covers both unit tests and acceptance tests (in-memory fakes, no Spring context, fast feedback).

### Persistence adapter changes
(`adapter-persistence/src/main/java/**` — JPA entities, repositories, `*RepositoryAdapter`)
- Run: `:adapter-persistence:test` (Testcontainers-backed integration tests for the persistence slice)
- Also run: `:e2e:test` to verify the full stack still works end-to-end.

### Web adapter changes
(`adapter-web/src/main/java/**` — controllers, DTOs, exception handlers, CORS/WebSocket config)
- Run: `:adapter-web:test` (`@WebMvcTest` slice — fast, no real DB)
- If the change touches request/response contracts or error codes, also run: `:e2e:test`

### E2E / infrastructure changes
(`e2e/**`, `docker-compose.yml`, `Dockerfile`, Flyway migrations under `adapter-persistence/src/main/resources/db`)
- Run: `:e2e:test` (full Testcontainers stack)

### Cross-cutting changes
(Any change that touches multiple modules, or a refactor that renames/moves types used everywhere)
- Run the full suite: `./gradlew test` (all modules)

### Quick reference

| Changed path | Tests to run |
|---|---|
| `domain/src/main/**` | `:domain:test`, `:application:test` |
| `application/src/main/**` | `:application:test` |
| `adapter-persistence/src/main/**` | `:adapter-persistence:test`, `:e2e:test` |
| `adapter-web/src/main/**` | `:adapter-web:test` (+ `:e2e:test` for contract changes) |
| `e2e/**` or migrations | `:e2e:test` |
| Multiple modules / rename | `./gradlew test` |

---

# Architecture overview

## High-level architecture

**Pattern:** Hexagonal Architecture (Ports & Adapters), monorepo with `backend/` (Java/Gradle) and `frontend/` (React/Vite).

```
┌──────────────────────────────────────────────────────────────────┐
│  frontend (React 19 SPA)                                         │
│  Nginx → proxy /api/ & /ws/ → backend                           │
└────────────────────────────┬─────────────────────────────────────┘
                             │ HTTP (REST) + WebSocket (STOMP/SockJS)
┌────────────────────────────▼─────────────────────────────────────┐
│  adapter-web (Spring Boot 3.3, Controllers, DTOs, Config)        │
│    ├── REST controllers (@RestController)                        │
│    ├── WebSocket config (STOMP + SockJS at /ws)                  │
│    ├── GlobalExceptionHandler → ErrorResponse                    │
│    └── LocalFileStorageAdapter (implements FileStoragePort)       │
├──────────────────────────────────────────────────────────────────┤
│  application (Services, Ports, Commands, Result DTOs)            │
│    ├── UserService, ListingService, ChatService, FavouriteService│
│    └── FileStoragePort (outbound port interface)                 │
├──────────────────────────────────────────────────────────────────┤
│  domain (Pure Java — zero framework deps)                        │
│    ├── Aggregates: User, Listing (+ Photo), Conversation (+ Msg)│
│    ├── Value Objects: Address, Money, ListingSearchCriteria      │
│    ├── Enums: ListingStatus, PropertyType, ListingTransactionType│
│    ├── Repository ports (driven interfaces)                      │
│    └── Domain exceptions hierarchy                               │
├──────────────────────────────────────────────────────────────────┤
│  adapter-persistence (JPA, Flyway, PostgreSQL 16)                │
│    ├── JPA entities (*JpaEntity) + Spring Data repositories      │
│    ├── Repository adapters (implement domain ports)              │
│    └── Flyway migrations (V1–V4)                                 │
└──────────────────────────────────────────────────────────────────┘
```

## Module dependency graph

```
domain (zero deps — pure Java)
  ↑
application (domain, spring-context, spring-tx, jakarta.validation-api)
  ↑
adapter-persistence (domain, application, spring-data-jpa, flyway, postgresql, mapstruct)
  ↑ (runtimeOnly)
adapter-web (domain, application; runtimeOnly adapter-persistence; spring-web, spring-websocket, springdoc)
  ↑
e2e (testImpl: adapter-web, spring-boot-testcontainers, testcontainers:postgresql)
```

## Authentication model

**Simplified header-based auth** — no JWT, no OAuth, no sessions.
- Every authenticated request carries `X-User-Id: <UUID>` header.
- The Axios interceptor on the frontend reads `localStorage.getItem('userId')` and injects this header.
- There is NO server-side token verification — the UUID is trusted directly.
- Admin role is not formalized — `deleteListing` accepts a boolean `isAdmin` parameter.

## Technology stack summary

| Layer | Technology | Version |
|---|---|---|
| Language (BE) | Java | 21 |
| Framework (BE) | Spring Boot | 3.3.4 |
| Build (BE) | Gradle (Kotlin DSL) | 8.10 |
| Database | PostgreSQL | 16-alpine |
| Migrations | Flyway | (managed by Spring Boot) |
| ORM | Hibernate / Spring Data JPA | (managed by Spring Boot) |
| Mapping (persistence) | MapStruct | 1.5.5.Final |
| API docs | Springdoc OpenAPI | 2.3.0 |
| WebSocket | Spring WebSocket + STOMP + SockJS | (managed by Spring Boot) |
| Language (FE) | TypeScript | ~5.9 |
| Framework (FE) | React | 19 |
| Build (FE) | Vite | 8 |
| Router (FE) | react-router-dom | 7 |
| State (FE) | Zustand | 5 |
| Data fetching (FE) | TanStack React Query | 5 |
| HTTP (FE) | Axios | 1.14 |
| WebSocket (FE) | @stomp/stompjs + sockjs-client | 7.3 / 1.6 |
| Icons (FE) | Lucide React | 1.7 |
| Unit tests (BE) | JUnit 5 + Mockito | 5.10 / 5.8 |
| Unit tests (FE) | Vitest + Testing Library + MSW | 4.1 / 16.3 / 2.13 |
| E2E tests (BE) | Testcontainers + Spring Boot Test | 1.21 |
| E2E tests (FE) | Playwright | 1.59 |
| Mutation testing | PiTest | 1.2.1 (JUnit5 plugin) |
| Coverage | JaCoCo | (managed by Gradle) |
| Containerization | Docker + Docker Compose | 3.9 |

---

# Backend technical reference

## Domain module (`domain/src/main/java/com/estateX/domain/`)

> **Rule:** Zero framework dependencies — pure Java. No Spring, no JPA, no Lombok. All business logic lives here.

### Aggregates & entities

#### User (`domain.user.User`)

| Field | Type | Notes |
|---|---|---|
| `id` | `UUID` (final) | Generated via `UUID.randomUUID()` at creation |
| `email` | `String` | Unique across system |
| `displayName` | `String` | |
| `phone` | `String` | Nullable |
| `createdAt` | `LocalDateTime` (final) | Set at creation |
| `active` | `boolean` | Default `true` |

**Business methods:**
- `static create(email, displayName)` → new ACTIVE user with random UUID
- `updateProfile(displayName, phone)` → updates name (if not blank) and phone
- `deactivate()` → sets `active = false`

**Repository port** (`UserRepository`): `save`, `findById`, `findByEmail`, `existsByEmail`, `countActiveListings(UUID)`

---

#### Listing (`domain.listing.Listing`)

| Field | Type | Notes |
|---|---|---|
| `id` | `UUID` (final) | |
| `title` | `String` | |
| `description` | `String` | |
| `address` | `Address` | Value object |
| `propertyType` | `PropertyType` | Enum |
| `transactionType` | `ListingTransactionType` | Enum |
| `price` | `Money` | Value object, PLN only |
| `areaSqMeters` | `double` | |
| `numberOfRooms` | `int` | |
| `status` | `ListingStatus` | Enum |
| `ownerId` | `UUID` (final) | |
| `photos` | `List<Photo>` | Mutable ArrayList |
| `viewCount` | `int` | |
| `createdAt` | `LocalDateTime` (final) | |
| `updatedAt` | `LocalDateTime` | |

**Invariants & domain rules:**
- `MAX_PHOTOS = 20` — throws `IllegalStateException` if exceeded
- `addPhoto(url)`: first photo auto-promoted to cover. Returns the created `Photo`
- `removePhoto(photoId)`: if removed photo was cover, next photo auto-promoted
- `setCoverPhoto(photoId)`: unmarks all, marks specified
- `incrementViewCount()`: `viewCount++`
- `isOwnedBy(userId)`: ownership check
- `changeStatus(newStatus)`: updates status + `updatedAt`
- `update(...)`: updates all core fields + `updatedAt`
- `static create(...)`: status = ACTIVE, viewCount = 0, empty photos

**Photo** (entity within Listing aggregate):
- Fields: `id` (UUID), `listingId` (UUID), `url` (String), `cover` (boolean), `uploadedAt` (LocalDateTime)
- Methods: `markAsCover()`, `unmarkCover()`

**Repository port** (`ListingRepository`): `save`, `findById`, `findByOwnerId`, `search(criteria, page, size)`, `delete`, `incrementViewCount`
- Inner record: `ListingPage(items, totalElements, totalPages, page)`

---

#### Conversation (`domain.chat.Conversation`)

| Field | Type | Notes |
|---|---|---|
| `id` | `UUID` (final) | |
| `listingId` | `UUID` (final) | |
| `initiatorId` | `UUID` (final) | The buyer |
| `listingOwnerId` | `UUID` (final) | |
| `startedAt` | `LocalDateTime` (final) | |

**Rules:**
- `create(...)`: throws `DomainException` if `initiatorId == listingOwnerId` (can't chat with yourself)
- `isParticipant(userId)`: checks if user is initiator OR owner
- `otherParticipant(userId)`: returns the other participant's ID

**Repository port** (`ConversationRepository`): `save`, `findById`, `findByParticipantId`, `existsByListingIdAndInitiatorId`, `findByListingIdAndInitiatorId`

---

#### Message (`domain.chat.Message`)

| Field | Type | Notes |
|---|---|---|
| `id` | `UUID` (final) | |
| `conversationId` | `UUID` (final) | |
| `senderId` | `UUID` (final) | |
| `content` | `String` | Nullable (if attachment present) |
| `attachmentUrl` | `String` | Nullable (if content present) |
| `sentAt` | `LocalDateTime` (final) | |
| `read` | `boolean` | Default `false` |

**Rules:**
- `create(...)`: message MUST have `content` OR `attachmentUrl` (throws `IllegalArgumentException` if both empty)
- `markAsRead()`: sets `read = true`

**Repository port** (`MessageRepository`): `save`, `findByConversationId(UUID, page, size)`, `countByConversationId`, `countUnread(conversationId, userId)`, `markAllAsRead(conversationId, recipientId)`

---

#### Favourite (`domain.favourite.Favourite`)

| Field | Type | Notes |
|---|---|---|
| `id` | `UUID` (final) | |
| `userId` | `UUID` (final) | |
| `listingId` | `UUID` (final) | |
| `savedAt` | `LocalDateTime` (final) | |

**Repository port** (`FavouriteRepository`): `save`, `findByUserId`, `findByUserIdAndListingId`, `deleteByUserIdAndListingId`, `existsByUserIdAndListingId`

---

### Value objects

| Value Object | Type | Fields | Invariants |
|---|---|---|---|
| `Address` | record | `street`, `city`, `voivodeship`, `postalCode`, `country`, `latitude` (Double), `longitude` (Double) | `city` is required (not blank). `country` defaults to `"Poland"` |
| `Money` | record | `amount` (BigDecimal) | `amount != null && amount >= 0`. Methods: `pln(BigDecimal)`, `pln(double)`, `isGreaterThan(Money)`, `isGreaterThanOrEqual(Money)` |
| `ListingSearchCriteria` | record | `keyword`, `city`, `voivodeship`, `minPrice`, `maxPrice`, `propertyType`, `transactionType`, `minRooms`, `maxRooms`, `minArea`, `maxArea`, `sortBy`, `sortDirection` | Inner enums: `SortBy` (CREATED_AT, PRICE, AREA), `SortDirection` (ASC, DESC). Factory: `empty()` defaults to CREATED_AT DESC |

### Enums

| Enum | Values |
|---|---|
| `ListingStatus` | `ACTIVE`, `PAUSED`, `RENTED` |
| `PropertyType` | `APARTMENT`, `HOUSE`, `ROOM`, `STUDIO` |
| `ListingTransactionType` | `RENT`, `SALE`, `EXCHANGE`, `WANTED` |

### Exception hierarchy

| Exception | Parent | Mapped HTTP status |
|---|---|---|
| `DomainException` | `RuntimeException` | 400 |
| `AccessDeniedException` | `DomainException` | 403 |
| `UserNotFoundException` | `DomainException` | 404 |
| `ListingNotFoundException` | `DomainException` | 404 |
| `ConversationNotFoundException` | `DomainException` | 404 |

---

## Application module (`application/src/main/java/com/estateX/application/`)

### Outbound port

**`FileStoragePort`** (interface):
- `String store(String filename, InputStream data, String contentType)` → returns URL
- `void delete(String url)`

### Services

#### UserService

| Method | Signature | Use case |
|---|---|---|
| `register` | `UserResult register(RegisterCommand)` | UC-1.1: checks email uniqueness → `User.create()` |
| `login` | `UserResult login(String email)` | Finds user by email |
| `updateProfile` | `UserResult updateProfile(UpdateProfileCommand)` | UC-2.1 |
| `getPublicProfile` | `UserResult getPublicProfile(UUID userId)` | UC-2.2: includes activeListingsCount |
| `getCurrentUser` | `UserResult getCurrentUser(UUID userId)` | Current user data |
| `deactivateUser` | `void deactivateUser(UUID userId)` | UC-10.2 |

**Commands:** `RegisterCommand(email, displayName)`, `UpdateProfileCommand(userId, displayName, phone)`

#### ListingService

| Method | Signature | Use case |
|---|---|---|
| `createListing` | `ListingResult createListing(CreateListingCommand)` | UC-3.1 |
| `updateListing` | `ListingResult updateListing(UpdateListingCommand)` | UC-3.3: verifies ownership |
| `deleteListing` | `void deleteListing(UUID listingId, UUID requesterId, boolean isAdmin)` | UC-3.4: cascades file deletion |
| `changeStatus` | `ListingResult changeStatus(UUID listingId, UUID ownerId, ListingStatus)` | UC-3.5 |
| `uploadPhoto` | `ListingResult uploadPhoto(UploadPhotoCommand)` | UC-3.2: stores via FileStoragePort |
| `setCoverPhoto` | `ListingResult setCoverPhoto(UUID listingId, UUID ownerId, UUID photoId)` | UC-3.2 |
| `deletePhoto` | `ListingResult deletePhoto(UUID listingId, UUID ownerId, UUID photoId)` | UC-3.2: deletes from storage |
| `searchListings` | `ListingPage searchListings(ListingSearchCriteria, page, size)` | UC-4 |
| `getListingDetail` | `ListingResult getListingDetail(UUID listingId)` | UC-4.6: auto-increments viewCount |
| `getMyListings` | `List<ListingResult> getMyListings(UUID ownerId)` | Owner's listings |
| `getAnalytics` | `List<ListingAnalyticsResult> getAnalytics(UUID ownerId)` | UC-11 |

**Commands:** `CreateListingCommand(ownerId, title, description, street, city, voivodeship, postalCode, country, lat, lng, propertyType, transactionType, price, areaSqMeters, numberOfRooms)`, `UpdateListingCommand(listingId, requesterId, ...)`, `UploadPhotoCommand(listingId, requesterId, filename, data, contentType)`

**Result records:** `ListingPage(items, totalElements, totalPages, page)`, `ListingAnalyticsResult(listingId, title, viewCount)`

#### ChatService

| Method | Signature | Use case |
|---|---|---|
| `startConversation` | `ConversationResult startConversation(UUID listingId, UUID initiatorId)` | UC-5.1: idempotent — returns existing |
| `sendMessage` | `MessageResult sendMessage(SendMessageCommand)` | UC-5.2: participant check, optional file storage |
| `getConversations` | `List<ConversationSummaryResult> getConversations(UUID userId)` | UC-5.3: inbox + unreadCount |
| `getMessages` | `MessagePage getMessages(UUID convId, UUID userId, page, size)` | Marks unread as read |

**Command:** `SendMessageCommand(conversationId, senderId, content, filename, attachmentData, attachmentContentType)`

**Results:** `ConversationResult(id, listingId, listingTitle, initiatorId, listingOwnerId, startedAt)`, `ConversationSummaryResult(..., unreadCount)`, `MessagePage(items, totalElements, totalPages, page)`

#### FavouriteService

| Method | Signature | Use case |
|---|---|---|
| `saveFavourite` | `FavouriteResult saveFavourite(UUID userId, UUID listingId)` | UC-6.1: idempotent |
| `removeFavourite` | `void removeFavourite(UUID userId, UUID listingId)` | UC-6.2 |
| `getFavourites` | `List<FavouriteResult> getFavourites(UUID userId)` | UC-6.3 |
| `isFavourite` | `boolean isFavourite(UUID userId, UUID listingId)` | Status check |

### Application-layer result DTOs

| Record | Key fields |
|---|---|
| `UserResult` | id, email, displayName, phone, createdAt, active, activeListingsCount |
| `ListingResult` | id, title, description, address fields, propertyType, transactionType, price, areaSqMeters, numberOfRooms, status, ownerId, ownerName, photos (List of PhotoResult), viewCount, createdAt, updatedAt |
| `ListingResult.PhotoResult` | id, url, cover (boolean) |
| `MessageResult` | id, conversationId, senderId, content, attachmentUrl, sentAt, read |
| `FavouriteResult` | id, userId, listingId, savedAt |

---

## Persistence module (`adapter-persistence/src/main/java/com/estateX/adapter/persistence/`)

### Configuration
- `@EntityScan("com.estatex.adapter.persistence")`
- `@EnableJpaRepositories("com.estatex.adapter.persistence")`

### Database tables (Flyway migrations)

| Migration | Tables created | Key constraints |
|---|---|---|
| V1 | `users` | UNIQUE(email), index on email |
| V2 | `listings`, `photos` | FK owner_id→users ON DELETE CASCADE, 5 indexes on listings, 1 on photos |
| V3 | `conversations`, `messages` | UNIQUE(listing_id + initiator_id), FK→listings ON DELETE SET NULL (buggy — see V6), FK→users ON DELETE CASCADE, 2 indexes on messages |
| V4 | `favourites`, `reviews`, `reports` | UNIQUE(user_id + listing_id). **`reviews` and `reports` exist only in DB — no JPA entities or domain objects.** Reserved for future development |
| V5 | — | `ALTER TABLE listings ADD COLUMN available_from DATE` |
| V6 | — | Fixes FK on `conversations.listing_id`: drops old `ON DELETE SET NULL`, adds `ON DELETE CASCADE`. V3 had a contradiction: column is NOT NULL but FK was SET NULL on delete. |

### JPA entities → DB columns

#### `UserJpaEntity` → `users`

| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE |
| `display_name` | VARCHAR(150) | NOT NULL |
| `phone` | VARCHAR(30) | — |
| `avatar_url` | VARCHAR(500) | — |
| `created_at` | TIMESTAMP | NOT NULL |
| `active` | BOOLEAN | NOT NULL |

#### `ListingJpaEntity` → `listings`

| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK |
| `title` | VARCHAR(255) | NOT NULL |
| `description` | TEXT | — |
| `street` | VARCHAR(255) | — |
| `city` | VARCHAR(100) | NOT NULL |
| `voivodeship` | VARCHAR(100) | — |
| `postal_code` | VARCHAR(20) | — |
| `country` | VARCHAR(100) | — |
| `latitude` | DOUBLE | — |
| `longitude` | DOUBLE | — |
| `property_type` | VARCHAR(50) | NOT NULL |
| `transaction_type` | VARCHAR(50) | NOT NULL |
| `price` | NUMERIC(12,2) | NOT NULL |
| `currency` | VARCHAR(10) | — |
| `area_sq_meters` | DOUBLE | NOT NULL |
| `number_of_rooms` | INTEGER | NOT NULL |
| `status` | VARCHAR(50) | NOT NULL |
| `owner_id` | UUID | NOT NULL, FK→users |
| `view_count` | INTEGER | NOT NULL |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | — |

**Relation:** `photos` — `@OneToMany(cascade = ALL, orphanRemoval = true, fetch = EAGER)`

#### `PhotoJpaEntity` → `photos`

| Column | Type |
|---|---|
| `id` | UUID (PK) |
| `listing_id` | FK→listings (`@ManyToOne LAZY`) |
| `url` | VARCHAR(500) NOT NULL |
| `cover` | BOOLEAN NOT NULL |
| `uploaded_at` | TIMESTAMP NOT NULL |

#### `ConversationJpaEntity` → `conversations`

| Column | Type |
|---|---|
| `id` | UUID (PK) |
| `listing_id` | UUID NOT NULL, FK→listings |
| `initiator_id` | UUID NOT NULL, FK→users |
| `listing_owner_id` | UUID NOT NULL, FK→users |
| `started_at` | TIMESTAMP NOT NULL |

#### `MessageJpaEntity` → `messages`

| Column | Type |
|---|---|
| `id` | UUID (PK) |
| `conversation_id` | UUID NOT NULL, FK→conversations |
| `sender_id` | UUID NOT NULL, FK→users |
| `content` | TEXT |
| `attachment_url` | VARCHAR(500) |
| `sent_at` | TIMESTAMP NOT NULL |
| `read` | BOOLEAN NOT NULL |

#### `FavouriteJpaEntity` → `favourites`

| Column | Type |
|---|---|
| `id` | UUID (PK) |
| `user_id` | UUID NOT NULL, FK→users |
| `listing_id` | UUID NOT NULL, FK→listings |
| `saved_at` | TIMESTAMP NOT NULL |

**UniqueConstraint:** `(user_id, listing_id)`

### Repository adapters

Each adapter implements the domain repository port. Mapping between JPA entities and domain objects is done via **manual mapping methods** (toEntity/toDomain) inside each adapter.

| Adapter class | Implements port | Special logic |
|---|---|---|
| `UserRepositoryAdapter` | `UserRepository` | — |
| `ListingRepositoryAdapter` | `ListingRepository` | Builds Spring `Sort` from domain `SortBy`/`SortDirection` |
| `ConversationRepositoryAdapter` | `ConversationRepository` | — |
| `MessageRepositoryAdapter` | `MessageRepository` | — |
| `FavouriteRepositoryAdapter` | `FavouriteRepository` | — |

### JPA repository custom queries

| Repository | Notable custom methods |
|---|---|
| `UserJpaRepository` | `countActiveListings(UUID)` — JPQL counting ACTIVE listings by owner |
| `ListingJpaRepository` | `search(...)` — JPQL with 11 optional filter parameters. String params (`keyword`, `city`, `voivodeship`) are pre-lowercased in `ListingRepositoryAdapter` before being passed; the JPQL uses `:keyword` directly in `LIKE` to let PostgreSQL infer text type from the operator context; `incrementViewCount(UUID)` — `@Modifying @Query` |
| `MessageJpaRepository` | `countUnread(convId, userId)` — JPQL; `markAllAsRead(convId, recipientId)` — `@Modifying @Query` |

### Test infrastructure
- Profile: `application-test.yml` — H2 in-memory with `MODE=PostgreSQL`, Flyway enabled
- Tests use `@DataJpaTest` + H2

---

## Web module (`adapter-web/src/main/java/com/estateX/adapter/web/`)

### Entry point
`EstateXApplication` — `@SpringBootApplication`

### Configuration classes

| Class | Purpose |
|---|---|
| `ModulesConfig` | `@ComponentScan` for `com.estatex.adapter.persistence` and `com.estatex.application` |
| `CorsConfig` | CORS for `/api/**`. Allowed origins: `http://localhost`, `http://localhost:80`. All methods. Credentials: true. MaxAge: 3600s |
| `WebSocketConfig` | STOMP over SockJS. Endpoint: `/ws`. Broker prefixes: `/topic`, `/queue`. App prefix: `/app` |
| `FileStorageConfig` | ResourceHandler mapping `/files/**` → `file:{upload-dir}/` |

### File storage

**`LocalFileStorageAdapter`** implements `FileStoragePort`:
- `store()`: generates `UUID + extension`, copies InputStream to disk
- `delete()`: extracts filename from URL, deletes from disk (failure is non-critical)
- Configured via `app.file-storage.upload-dir` and `app.file-storage.base-url`

### Exception handler (`GlobalExceptionHandler`)

| Exception type | HTTP status | Response body |
|---|---|---|
| `UserNotFoundException` | 404 | `ErrorResponse { message, timestamp }` |
| `ListingNotFoundException` | 404 | `ErrorResponse { message, timestamp }` |
| `ConversationNotFoundException` | 404 | `ErrorResponse { message, timestamp }` |
| `AccessDeniedException` | 403 | `ErrorResponse { message, timestamp }` |
| `DomainException` | 400 | `ErrorResponse { message, timestamp }` |
| `IllegalArgumentException` | 400 | `ErrorResponse { message, timestamp }` |
| `IllegalStateException` | 400 | `ErrorResponse { message, timestamp }` |
| `MethodArgumentNotValidException` | 400 | `ErrorResponse` with field-level error map |
| `Exception` (generic fallback) | 500 | `ErrorResponse { message, timestamp }` |

### Application properties

| Property | Default | Prod override |
|---|---|---|
| DB URL | `jdbc:postgresql://localhost:5432/estateX` | `${DATABASE_URL}` |
| DB credentials | `estateX` / `estateX_secret` | `${DATABASE_USER}` / `${DATABASE_PASSWORD}` |
| Hibernate DDL | `validate` | — |
| Flyway | enabled, `classpath:db/migration` | — |
| Multipart | max-file: 10MB, max-request: 50MB | — |
| Upload dir | `./uploads` | `${UPLOAD_DIR:/app/uploads}` |
| File base URL | `http://localhost:8080/files` | `${FILES_BASE_URL}` |
| Frontend URL | `http://localhost:5173` | `${FRONTEND_URL}` |
| Swagger | `/api-docs`, `/swagger-ui.html` | — |

---

## E2E module (`e2e/src/test/java/com/estateX/e2e/`)

### Test infrastructure

**`E2ETestBase`:**
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` with `EstateXApplication`
- **Testcontainers:** `PostgreSQLContainer("postgres:16-alpine")` — singleton, shared across all test classes
- `@DynamicPropertySource` to wire container DB
- `@BeforeEach`: **TRUNCATE** all tables with CASCADE (clean state per test)
- Profile: `application-e2e.yml` — upload dir in tmpdir, Flyway enabled
- Helpers: `registerUser(email, name)`, `createListing(ownerId, ...)`, `userHeaders(userId)`
- Uses Apache HttpComponents for PATCH support

### Test classes (39 tests total)

| Class | Tests | Coverage |
|---|---|---|
| `AuthE2ETest` | 4 | register, duplicate email, login, login nonexistent |
| `UserE2ETest` | 3 | get profile, update profile, public profile |
| `ListingE2ETest` | 8 | create, detail+viewCount, update, update 403, delete, delete 403, changeStatus, myListings |
| `ListingSearchE2ETest` | 5 | no filters, city+propertyType, transactionType, priceRange, pagination |
| `ListingPhotoE2ETest` | 4 | upload+auto cover, cover promote on delete, manual set cover, max 20 photos |
| `ConversationE2ETest` | 7 | open conversation, idempotent duplicate, send+retrieve message, markAsRead, 403 stranger, empty message 400, inbox unreadCount |
| `FavouriteE2ETest` | 6 | add favourite, idempotent add, remove, status true, status false, get all |
| `AnalyticsE2ETest` | 2 | viewCount analytics, empty analytics |

---

# API contract reference

## Auth endpoints (`/api/auth`)

| Method | Path | Request body | Response | Status |
|---|---|---|---|---|
| POST | `/api/auth/register` | `{ email: string, displayName: string }` | `UserResult` | 200 |
| POST | `/api/auth/login` | `{ email: string }` | `UserResult` | 200 |

## User endpoints (`/api/users`)

| Method | Path | Auth | Request body | Response | Status |
|---|---|---|---|---|---|
| GET | `/api/users/me` | `X-User-Id` | — | `UserResult` | 200 |
| PUT | `/api/users/me` | `X-User-Id` | `{ displayName: string, phone?: string }` | `UserResult` | 200 |
| GET | `/api/users/{userId}/profile` | — | — | `UserResult` | 200 |

## Listing endpoints (`/api/listings`)

| Method | Path | Auth | Params / Body | Response | Status |
|---|---|---|---|---|---|
| GET | `/api/listings` | — | Query: `keyword`, `city`, `voivodeship`, `minPrice`, `maxPrice`, `propertyType`, `transactionType`, `minRooms`, `maxRooms`, `minArea`, `maxArea`, `availableEarliest` (date), `availableLatest` (date), `sortBy` (def: CREATED_AT), `sortDirection` (def: DESC), `page` (def: 0), `size` (def: 20) | `ListingPage` | 200 |
| GET | `/api/listings/{id}` | — | — | `ListingResult` (increments viewCount) | 200 |
| POST | `/api/listings` | `X-User-Id` | Body: `CreateListingRequest` | `ListingResult` | 200 |
| PUT | `/api/listings/{id}` | `X-User-Id` | Body: `CreateListingRequest` | `ListingResult` | 200 |
| DELETE | `/api/listings/{id}` | `X-User-Id` | — | — | 204 |
| PATCH | `/api/listings/{id}/status` | `X-User-Id` | Body: `{ status: ListingStatus }` | `ListingResult` | 200 |
| POST | `/api/listings/{id}/photos` | `X-User-Id` | Multipart: `file` | `ListingResult` | 200 |
| PATCH | `/api/listings/{id}/photos/{photoId}/cover` | `X-User-Id` | — | `ListingResult` | 200 |
| DELETE | `/api/listings/{id}/photos/{photoId}` | `X-User-Id` | — | — | 204 |
| GET | `/api/listings/my` | `X-User-Id` | — | `List<ListingResult>` | 200 |
| GET | `/api/listings/my/analytics` | `X-User-Id` | — | `List<ListingAnalyticsResult>` | 200 |

**`CreateListingRequest` fields:** `title` (required, max 255), `description`, `street`, `city` (required), `voivodeship`, `postalCode`, `country`, `latitude`, `longitude`, `propertyType` (required), `transactionType` (required), `price` (required, positive), `currency`, `areaSqMeters` (positive), `numberOfRooms` (positive, max 50)

## Conversation endpoints (`/api/conversations`)

| Method | Path | Auth | Body | Response | Status |
|---|---|---|---|---|---|
| POST | `/api/conversations` | `X-User-Id` | `{ listingId: UUID }` | `ConversationResult` | 200 |
| GET | `/api/conversations` | `X-User-Id` | — | `List<ConversationSummaryResult>` | 200 |
| GET | `/api/conversations/{id}/messages` | `X-User-Id` | Query: `page` (def: 0), `size` (def: 50) | `MessagePage` | 200 |
| POST | `/api/conversations/{id}/messages` | `X-User-Id` | `{ content: string }` | `MessageResult` + WebSocket broadcast to `/topic/conversation.{id}` | 200 |
| POST | `/api/conversations/{id}/messages/attachment` | `X-User-Id` | Multipart: `file`, optional `content` | `MessageResult` + WebSocket broadcast | 200 |

## Favourite endpoints (`/api/favourites`)

| Method | Path | Auth | Response | Status |
|---|---|---|---|---|
| GET | `/api/favourites` | `X-User-Id` | `List<FavouriteResult>` | 200 |
| POST | `/api/favourites/{listingId}` | `X-User-Id` | `FavouriteResult` (idempotent) | 200 |
| DELETE | `/api/favourites/{listingId}` | `X-User-Id` | — | 204 |
| GET | `/api/favourites/{listingId}/status` | `X-User-Id` | `boolean` | 200 |

## WebSocket

- **Endpoint:** `/ws` (SockJS fallback)
- **STOMP broker prefixes:** `/topic`, `/queue`
- **App prefix:** `/app`
- **Chat subscription:** `/topic/conversation.{conversationId}` — receives `MessageResult` on new messages

---

# Data model reference

## Backend ↔ Frontend field mapping

> **Critical:** Backend and frontend use DIFFERENT field names for some Listing properties. This mapping table is essential for any cross-boundary work.

### Listing fields

| Backend (domain / API) | Frontend (TypeScript) | Notes |
|---|---|---|
| `areaSqMeters` | `area` | Different name |
| `numberOfRooms` | `rooms` | Different name |
| `price` (Money → BigDecimal) | `price` (number) | Same name, different type |
| `Photo.cover` (boolean) | `Photo.isCover` (boolean) | Different name |
| — | `Photo.position` (number) | Frontend-only field |

### ListingPage fields

| Backend (application) | Frontend (TypeScript) | Notes |
|---|---|---|
| `items` | `content` | Different name |
| `page` | `number` | Different name |
| — | `size` | Frontend-only field |

### Conversation fields

| Backend (ConversationResult) | Frontend (Conversation) | Notes |
|---|---|---|
| `listingOwnerId` | `ownerId` | Different name |

### Favourite API mismatch

| Backend endpoint | Frontend API call | Notes |
|---|---|---|
| `POST /api/favourites/{listingId}` (path param) | `POST /api/favourites` with body `{ listingId }` | **API mismatch — frontend sends body, backend expects path parameter** |

### Enum value differences

| Enum | Backend values | Frontend values | Notes |
|---|---|---|---|
| `PropertyType` | APARTMENT, HOUSE, ROOM, STUDIO | APARTMENT, HOUSE, STUDIO, COMMERCIAL, LAND | Frontend has extra values not in backend |
| `ListingTransactionType` / `TransactionType` | RENT, SALE, EXCHANGE, WANTED | RENT, SALE | Backend has extra values not in frontend |
| `ListingStatus` | ACTIVE, PAUSED, RENTED | ACTIVE, ARCHIVED, DELETED | Completely different sets |

---

# Frontend technical reference

## Project setup

| Config | Value |
|---|---|
| Entry point | `frontend/src/main.tsx` |
| Build output | `frontend/dist/` |
| Dev server | `vite` on port 5173 |
| Preview server | `vite preview` on port 4173 |
| TypeScript | strict mode, ES2023 target |

### npm scripts

| Script | Command |
|---|---|
| `dev` | `vite` |
| `build` | `tsc -b && vite build` |
| `lint` | `eslint .` |
| `preview` | `vite preview` |
| `test` | `vitest run` |
| `test:watch` | `vitest` |
| `test:e2e` | `playwright test` |

## Routing (`App.tsx`)

**QueryClient config:** `retry: 1`, `staleTime: 30_000` (30 seconds)

| Path | Component | Layout | Notes |
|---|---|---|---|
| `/login` | `LoginPage` | None (standalone) | |
| `/` (index) | `BrowsePage` | `AppLayout` | |
| `/listings/new` | `CreateListingPage` | `AppLayout` | |
| `/listings/:id` | `ListingDetailPage` | `AppLayout` | |
| `/listings/:id/edit` | `EditListingPage` | `AppLayout` | Owner-only edit form; pre-fills from `listingApi.getById` |
| `/favourites` | `FavouritesPage` | `AppLayout` | |
| `/inbox` | `InboxPage` | `AppLayout` | |
| `/inbox/:id` | `InboxPage` | `AppLayout` | Same component, different route. `id === 'new'` = pending new conversation |
| `/inbox/new?listingId=…` | `InboxPage` | `AppLayout` | Pending conversation — created only on first message send |
| `/profile` | `UserProfilePage` | `AppLayout` | |
| `/my-listings` | `MyListingsPage` | `AppLayout` | Owner's own listings |
| `/users/:userId` | `PublicUserProfilePage` | `AppLayout` | Read-only public profile of any user |
| `*` (fallback) | `Navigate to /` | `AppLayout` | |

**`AppLayout`** requires `userId` in auth store — redirects to `/login` if null. Renders `Topbar` + `Sidebar` + `<Outlet />`.

## State management (`store/auth.ts`)

Zustand store with `persist` middleware (localStorage key: `"auth"`):

```typescript
interface AuthState {
  userId: string | null;
  displayName: string | null;
  setUser(userId: string, displayName: string): void;  // also sets localStorage('userId')
  clearUser(): void;  // also removes localStorage('userId')
}
```

> **Note:** `setUser` duplicates `userId` into `localStorage.setItem('userId')` specifically for the Axios interceptor to read.

## HTTP client (`lib/axios.ts`)

- **Base URL:** `import.meta.env.VITE_API_URL ?? 'http://localhost:8080'`
- **Default header:** `Content-Type: application/json`
- **Request interceptor:** reads `localStorage.getItem('userId')` → sets `X-User-Id` header on every request
- No response interceptor, no token refresh logic

## API layer (`src/api/`)

### `userApi` (`api/users.ts`)

| Function | Method | URL | Parameters | Return |
|---|---|---|---|---|
| `register(email, displayName)` | POST | `/api/auth/register` | `{ email, displayName }` | `User` |
| `login(email)` | POST | `/api/auth/login` | `{ email }` | `User` |
| `getMe()` | GET | `/api/users/me` | — | `User` |
| `updateMe(data)` | PUT | `/api/users/me` | `{ displayName, phone? }` | `User` |
| `getPublicProfile(userId)` | GET | `/api/users/{userId}/profile` | — | `User` |

### `listingApi` (`api/listings.ts`)

| Function | Method | URL | Parameters | Return |
|---|---|---|---|---|
| `search(criteria)` | GET | `/api/listings` | query params from `ListingSearchCriteria` | `ListingPage` |
| `getById(id)` | GET | `/api/listings/{id}` | — | `Listing` |
| `create(data)` | POST | `/api/listings` | `Partial<Listing>` body | `Listing` |
| `update(id, data)` | PUT | `/api/listings/{id}` | `Partial<Listing>` body | `Listing` |
| `delete(id)` | DELETE | `/api/listings/{id}` | — | `void` |
| `changeStatus(id, status)` | PATCH | `/api/listings/{id}/status` | `{ status }` body | `Listing` |
| `getMyListings()` | GET | `/api/listings/my` | — | `Listing[]` |

### `chatApi` (`api/chat.ts`)

| Function | Method | URL | Parameters | Return |
|---|---|---|---|---|
| `getConversations()` | GET | `/api/conversations` | — | `Conversation[]` |
| `startConversation(listingId)` | POST | `/api/conversations` | `{ listingId }` | `Conversation` |
| `getMessages(conversationId)` | GET | `/api/conversations/{id}/messages` | — | `Message[]` |
| `sendMessage(conversationId, content)` | POST | `/api/conversations/{id}/messages` | `{ content }` | `Message` |

### `favouriteApi` (`api/favourites.ts`)

| Function | Method | URL | Parameters | Return |
|---|---|---|---|---|
| `getAll()` | GET | `/api/favourites` | — | `Favourite[]` |
| `save(listingId)` | POST | `/api/favourites` | `{ listingId }` body | `Favourite` |
| `remove(id)` | DELETE | `/api/favourites/{id}` | — | `void` |

## Pages

### LoginPage (`pages/LoginPage.tsx`)
- Toggle between `'login'` and `'register'` modes
- **Login:** POST `/api/auth/login` with `{ email }` → `setUser(id, displayName)` → navigate `/`
- **Register:** POST `/api/auth/register` with `{ email, displayName }` → `setUser(id, displayName)` → navigate `/`
- Fields: email (always), displayName (register only)
- Buttons: `Sign in` / `Create account` (submit), `Register` / `Sign in` (toggle)
- Branding: "EstateX" with Zap icon

### BrowsePage (`pages/BrowsePage.tsx`)
- **Query:** `useQuery(['listings', criteria], listingApi.search)`, keepPreviousData
- **Filters:** city (text), transactionType (select), propertyType (select), minPrice (number), maxPrice (number), minRooms (number) — controlled inputs bound to `draft` state
- Filter changes update `draft` state only; "Apply" button commits `draft → criteria` and resets page to 0
- Pagination buttons update `criteria` directly (bypass draft)
- **Grid:** renders `ListingCard` for each listing in `data.content`
- **Pagination:** Prev/Next buttons, "Page X / Y" text
- Shows total elements count in subtitle

### CreateListingPage (`pages/CreateListingPage.tsx`)
- **Mutation:** `listingApi.create(...)` → navigate `/listings/{id}`
- **Sections:** Basic Info, Financials & Dimensions, Location
- **Fields sent:** title, description, propertyType, transactionType, price (Number), area (Number), rooms (Number|undefined), street, city, voivodeship, postalCode, country (hardcoded "Poland")
- Buttons: "Create Listing" (submit), "Cancel" (navigate back)

### ListingDetailPage (`pages/ListingDetailPage.tsx`)
- **Query:** `useQuery(['listing', id], listingApi.getById)`
- **Mutations:** `listingApi.delete(id)` → navigate `/`
- **Message Owner button:** navigates to `/inbox/new?listingId={id}` — no immediate API call (conversation deferred to first send)
- Shows: cover photo, thumbnail strip, transactionType badge, status badge, viewCount, title, address, price/area/rooms stat cards, description, owner name
- **Message Owner button:** checks `conversations` cache for existing conversation with this listing; navigates to `/inbox/{existingId}` if found, otherwise `/inbox/new?listingId={id}`
- **Owner actions:** "Edit Listing" button (→ `/listings/{id}/edit`), "Delete Listing" button (with `confirm()` dialog)
- **Non-owner actions:** "Message Owner" button, `FavouriteButton` component
- Back button: `navigate(-1)`

### FavouritesPage (`pages/FavouritesPage.tsx`)
- **Query:** `useQuery(['favourites'], favouriteApi.getAll)`
- For each favourite → separate `useQuery(['listing', id], listingApi.getById)` to fetch listing details
- Shows favourite count and listing cards
- Empty state: Heart icon + text

### InboxPage (`pages/InboxPage.tsx`)
- **Queries:** `useQuery(['conversations'], chatApi.getConversations)` with `refetchInterval: 10_000`, `useQuery(['messages', activeId], chatApi.getMessages)` (disabled when `activeId === 'new'`), `useQuery(['user', otherPersonId], userApi.getPublicProfile)` for chat header name
- **Mutation (send):** if `activeId === 'new'` — calls `chatApi.startConversation(pendingListingId)` then `sendMessage`, then navigates to `/inbox/{convId}`; otherwise sends message in existing conversation
- **Badge fix:** `useEffect` invalidates `['conversations']` on `messagesQuery.isSuccess` (clears unread count immediately on open)
- **WebSocket:** STOMP over SockJS on `/ws`, subscribes to `/topic/conversation.{activeId}`, skipped when `activeId === 'new'`
- **Header:** shows `otherPerson.displayName` as title and `listingTitle` as subtitle; includes "View listing" link (`ExternalLink` icon → `/listings/{listingId}`)
- **Layout:** sidebar (280px) with conversation list + chat window
- Conversations show: avatar (first letter), title, unread badge
- Messages: bubble layout (own = accent color, theirs = elevated)
- Input: text input, placeholder "Type a message…", Enter to send, icon-only Send button
- Auto-scroll to latest message
- Pending state (`activeId === 'new'`): shows "Send a message to start the conversation" placeholder

### UserProfilePage (`pages/UserProfilePage.tsx`)
- **Query:** `useQuery(['me', userId], userApi.getMe)`
- **Mutation:** `userApi.updateMe({ displayName, phone })` → invalidates queries, updates auth store
- Sections: avatar card (initial, name, email, active badge, listings count), account details (email disabled, displayName, phone), statistics (active listings, member since)
- Toggle edit mode with "Edit" / "Save" buttons
- Success toast: "Profile updated successfully" (3s timeout)

## Components

### Layout
- **`AppLayout`** — Auth guard (redirects to `/login` if no userId). CSS Grid: topbar (full width, sticky), sidebar (left), content (right `<Outlet />`). Logo text: "EstateX", subtitle: "Property Marketplace"
- **`Sidebar`** — Shows "Signed in as {displayName}". Nav links (NavLink with active state): Browse (`/`), Search (`/search`), My Listings (`/my-listings`), Favourites (`/favourites`), Inbox (`/inbox`), Profile (`/profile`). Buttons: "New Listing" (`<button>` → `/listings/new`), "Sign out" (`clearUser()` → `/login`)

### Listing
- **`ListingCard`** — Props: `{ listing: Listing }`. Renders cover photo (from `photos.find(p => p.isCover)`), price in PLN (pl-PL locale), transactionType badge, title, city, area, rooms, viewCount. Entire card clickable → `/listings/{id}`. Contains `FavouriteButton` with click propagation stopped.

### Favourite
- **`FavouriteButton`** — Props: `{ listingId: string }`. Queries `['favourites']` via `favouriteApi.getAll` to find if listing is favourited. Mutations: `save(listingId)` / `remove(fav.id)`. Optimistic UI via `useState<boolean | null>`. Heart icon: filled + red if saved, outline if not.

- **`PublicUserProfilePage`** — Props: none (reads `userId` from route param). Fetches via `userApi.getPublicProfile(userId)`. Shows read-only avatar, name, email, phone (if set), member-since, active listings count. Includes a Back button (`navigate(-1)`).

## Frontend test infrastructure

### Unit tests (Vitest)

**Config** (`vite.config.ts`): globals, jsdom environment, setup file, excludes `e2e/**`

**Setup** (`src/test/setup.ts`): imports `@testing-library/jest-dom/vitest`, `afterEach` runs DOM cleanup + `localStorage.clear()`

**Utils** (`src/test/test-utils.tsx`): `createTestQueryClient()` (retry: false, gcTime: 0), `renderWithProviders(ui)` wraps with `QueryClientProvider` + `BrowserRouter`

**MSW mocks** (`src/test/mocks/`): `handlers.ts` with fixture data for all API endpoints, `server.ts` with `setupServer(...handlers)`

**Test pattern:** `beforeAll(server.listen)`, `afterEach(server.resetHandlers)`, `afterAll(server.close)`

### Test files (64 tests total)

| File | Tests | What it covers |
|---|---|---|
| `src/api/users.test.ts` | 5 | register, login, getMe, updateMe, publicProfile |
| `src/api/listings.test.ts` | 5 | search, page structure, getById, create, delete |
| `src/api/favourites.test.ts` | 3 | getAll, save, remove |
| `src/api/chat.test.ts` | 4 | getConversations, startConversation, getMessages, sendMessage |
| `src/store/auth.test.ts` | 3 | initial null state, setUser + localStorage, clearUser |
| `src/pages/LoginPage.test.tsx` | 5 | render form, switch mode, register flow, login flow, branding |
| `src/pages/BrowsePage.test.tsx` | 5 | heading, loading spinner, listing cards, filter inputs, select dropdowns |
| `src/pages/CreateListingPage.test.tsx` | 6 | form render, sections, fields, buttons, **photo required error**, form submit |
| `src/pages/EditListingPage.test.tsx` | 3 | form load, **photo required error**, **photo count update after upload** |
| `src/pages/FavouritesPage.test.tsx` | 3 | heading, count display, listing cards |
| `src/pages/UserProfilePage.test.tsx` | 2 | heading, display value |
| `src/components/listing/ListingCard.test.tsx` | 6 | title, price, city, area/rooms, viewCount, cover photo |

### E2E tests (Playwright)

**Config** (`playwright.config.ts`): testDir `./e2e`, timeout 60s, Chromium headless, `baseURL: http://localhost:4173`, webServer runs `npm run preview`

| File | Journey | Steps |
|---|---|---|
| `e2e/user-journey.spec.ts` | Register → Create → Upload Photo → Browse | Register user → navigate → fill create form → **upload required photo** → verify detail → browse and see listing |
| `e2e/buyer-journey.spec.ts` | Browse → Favorite → Chat → Message | Login → browse listings → click detail → message owner → navigate to inbox → send message |
| `e2e/seller-dashboard.spec.ts` | View listings → Analytics | Login as seller → verify browse shows listings → navigate to profile → verify profile data |
| `e2e/listing-management.spec.ts` | Create+Photo, Edit, Archive/Restore, Delete | Create listing with required photo; edit title/price; archive then restore from My Listings; delete from My Listings |
| `e2e/profile-search-favourites.spec.ts` | Profile update, Search filter, Favourites, Pagination | Update displayName/phone; filter by city with no-match case; add to favourites then verify on Favourites page; paginate through listing pages |

All E2E tests use `page.route()` to intercept API calls (no real backend needed).

---

# Infrastructure & deployment

## Docker Compose (`docker-compose.yml`)

| Service | Image | Ports | Purpose |
|---|---|---|---|
| `postgres` | `postgres:16-alpine` | 5432:5432 | Database. DB: `estateX`, User: `estateX`, Pass: `estateX_secret`. Volume: `postgres_data`. Healthcheck: `pg_isready` |
| `mailhog` | `mailhog/mailhog:latest` | 1025 (SMTP), 8025 (Web UI) | Email interceptor (reserved for future use) |
| `backend` | Built from `./backend/Dockerfile` | 8080:8080 | Spring Boot app. Profile: `prod`. Env: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `FILES_BASE_URL`, `FRONTEND_URL`. Volume: `uploads_data` |
| `frontend` | Built from `./frontend/Dockerfile` | 80:80 | Nginx serving SPA. Depends on backend |

## Backend Dockerfile

- **Build stage:** `gradle:8.10-jdk21` → `gradle :adapter-web:bootJar --no-daemon -q`
- **Run stage:** `eclipse-temurin:21-jre-alpine` → `java -jar app.jar`
- Exposes port 8080

## Frontend Dockerfile

- **Build stage:** `node:22-alpine` → `npm ci && npm run build`
- **Run stage:** `nginx:alpine` → copies `dist/` to nginx HTML root + custom `nginx.conf`
- Exposes port 80

## Nginx configuration (`frontend/nginx.conf`)

```
/ → try_files $uri $uri/ /index.html   (SPA fallback)
/api/ → proxy_pass http://backend:8080  (REST API reverse proxy)
/ws/ → proxy_pass http://backend:8080   (WebSocket upgrade proxy)
```

## Running locally

```bash
# Start all services (DB + Mailhog + Backend + Frontend)
docker compose up

# Or run individually for development:
# Terminal 1: Database
docker compose up postgres

# Terminal 2: Backend
cd backend && ./gradlew :adapter-web:bootRun

# Terminal 3: Frontend
cd frontend && npm run dev
```

---

# Development standards

## Build & quality gates

| Tool | Config | Threshold |
|---|---|---|
| JaCoCo | All subprojects | **100%** line + branch coverage required |
| PiTest | `:domain` and `:application` only | **100%** mutation threshold, **100%** test strength, 4 threads |
| Stryker | Frontend (`src/api/**`, `src/store/**`) | **100%** mutation score required. Run: `cd frontend && npm run test:mutation` |
| Compiler | All | `-parameters` flag enabled |
| Java toolchain | All | Java 21 |

## Testing standards

### Naming convention
Test methods MUST use `shouldDoSomethingWhenCondition` format:
- `shouldDoSomething` — expected behavior
- `WhenCondition` — specific condition

### Structure (required)
```java
@Test
public void shouldReturnUserWhenEmailExists() {
    /// given
    // setup preconditions

    /// when
    // execute tested code

    /// then
    // assert outcomes
}
```

The `/// given`, `/// when`, `/// then` comments are **mandatory**.

### Guidelines
- Test body should be as short as possible
- Long tests must be split into smaller tests + helper methods
- Non-conforming tests must be refactored

### Bug fix coverage requirement
**When an agent discovers and fixes a bug or inappropriate behavior:**
- The fix itself is not complete until test coverage is added to prevent regression
- Add a test that would have caught the bug before the fix
- Test should verify the correct behavior and will fail if the bug is reintroduced
- Update this knowledge base (AGENTS.md) if the bug reveals missing documentation or a new quirk

## Testing strategy by change type

| Changed path | Tests to run |
|---|---|
| `domain/src/main/**` | `:domain:test`, `:application:test` |
| `application/src/main/**` | `:application:test` |
| `adapter-persistence/src/main/**` | `:adapter-persistence:test`, `:e2e:test` |
| `adapter-web/src/main/**` | `:adapter-web:test` (+ `:e2e:test` for contract changes) |
| `e2e/**` or migrations | `:e2e:test` |
| `frontend/src/**` | `cd frontend && npm test` |
| `frontend/src/api/**` or `frontend/src/store/**` | `cd frontend && npm test` **and** `cd frontend && npm run test:mutation` |
| `frontend/e2e/**` | `cd frontend && npm run test:e2e` |
| Multiple modules / rename | `./gradlew test` |

---

# Known quirks & caveats

1. **`spring-boot-starter-validation` is NOT on the classpath.** Bean validation annotations (`@Valid`, `@NotBlank`, `@Email`, etc.) on controller DTOs are **not enforced at runtime**. Requests with invalid data will pass through to the service layer.

2. **Missing `X-User-Id` header returns 500, not 400.** When a required `X-User-Id` header is absent, Spring throws `MissingRequestHeaderException` which is caught by the generic `Exception` handler → 500 Internal Server Error.

3. ~~**Favourite API contract mismatch.** Backend `FavouriteController` expects `POST /api/favourites/{listingId}` (path parameter), but frontend `favouriteApi.save()` sent `POST /api/favourites` with `{ listingId }` in the request body.~~ Fixed: `favouriteApi.save(listingId)` now sends `POST /api/favourites/{listingId}` and `favouriteApi.remove(listingId)` correctly passes the listing ID (not favourite ID) in the path.

4. **Enum value sets differ between backend and frontend.** See [Data model reference → Enum value differences](#enum-value-differences) for full details. Frontend uses `COMMERCIAL`, `LAND` (not in backend); backend uses `ROOM`, `EXCHANGE`, `WANTED` (not in frontend). `ListingStatus` values are completely different.

5. **`reviews` and `reports` tables exist in DB but have no code.** Created in V4 migration, but no JPA entities, domain objects, services, or controllers exist for them. Reserved for future features.

6. **Sidebar has a `/search` link but no `/search` route.** It falls through to the `*` wildcard and redirects to `/`.

7. ~~**Frontend `ListingPage` uses `content` field, backend `ListingPage` uses `items` field.**~~ Fixed: `listingApi.search()` now maps `r.data.items → content` and `r.data.page → number` in the response transform. The `size` field was also removed from `ListingPage` as it is not returned by the backend and was unused.

8. **Frontend `Photo` type has `isCover` + `position`, backend `PhotoResult` has `cover` (no `position`).** Field name mapping needed at the API boundary.

9. **WebSocket connection:** Frontend connects directly to `http://localhost:8080/ws` in dev mode (hardcoded via Axios baseURL). In production (Docker), nginx proxies `/ws/` to backend.

10. ~~**`EditListingPage` photo upload silently dropped new files.** The `onChange` handler for the file input passed a lazy updater function that read `e.target.files` after `e.target.value = ''` had already cleared the FileList. Fixed: files are now captured into a local `const files = Array.from(e.target.files ?? [])` array before the state updater is scheduled.~~

11. ~~**No route existed for `/users/:userId` (public profile).** The `InboxPage` linked to `/users/${otherPersonId}`, but the router had no such route — the `*` wildcard redirected to `/`. Fixed: added `PublicUserProfilePage` component and a `/users/:userId` route.~~

12. **At least one photo is required to create or edit a listing.** `handleSubmit` in both `CreateListingPage` and `EditListingPage` validates `pendingPhotos.length > 0` (create) and `existingPhotos.length + pendingPhotos.length > 0` (edit) before calling the mutation.

13. **Search endpoint mock format in E2E tests must use `items`/`page` keys (backend wire format), not `content`/`number` (frontend format).** `listingApi.search()` maps `r.data.items → content` and `r.data.page → number`. Mocks returning `{ content: [...] }` will produce an empty listing grid since `r.data.items` would be `undefined`.

14. **Photo file input `onChange` must capture `FileList` into a `File[]` array eagerly before calling `e.target.value = ''`.** Both `CreateListingPage` and `EditListingPage` apply `const files = Array.from(e.target.files ?? [])` before resetting the input, preventing stale-closure issues in React 18 batched state updates.
