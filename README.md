# Shriven

A simple, distributed URL shortener built with Kotlin and Angular. It uses Snowflake IDs and Base62 for generating links. The focus is on speed: Redis handles the redirects, while Kafka processes analytics asynchronously.

## Tech Stack

- **Backend:** Kotlin + Spring Boot 3 + JetBrains Exposed
- **Database:** PostgreSQL (primary) + Redis (cache)
- **Messaging:** Apache Kafka (KRaft mode)
- **Frontend:** Angular 19 with SSR
- **DevOps:** Docker Compose

## Architecture Highlights

- **Snowflake IDs** for distributed, collision-free ID generation
- **Base62 encoding** for compact short codes
- **Redis cache-aside** pattern for ultra-low latency redirects (P99 < 50ms)
- **Event-driven analytics** via Kafka (no DB writes on redirect path)
- **Horizontal scalability** with stateless design

## Features

### Core Capabilities
- **Distributed ID Generation:** Utilizes Snowflake algorithm for unique, sortable IDs without coordination.
- **Base62 Encoding:** Converts numeric IDs into compact, URL-safe alphanumeric strings.
- **Data Persistence:** reliably stores all link data and metadata in PostgreSQL.

### Performance & Caching
- **Redis Integration:** Implements Cache-Aside and Write-Through patterns for instant data access.
- **TTL Support:** Automatically handles time-to-live for temporary cache keys.
- **Non-Blocking Redirects:** Serves redirects immediately while offloading analytics to background threads.

### Infrastructure & Messaging
- **Asynchronous Event Publishing:** Publishes `ClickEvents` to Kafka topics during the redirect phase.
- **Dockerized Environment:** Fully containerized setup for PostgreSQL, Redis, and Kafka (KRaft).

## Planned Features

### Analytics Engine (In Progress)
- **Kafka Consumer:** Implement background processing to consume and aggregate click events.
- **Batch Processing:** Buffer events in memory for efficient database writes.
- **Data Visualization:** Expose endpoints for daily/weekly click statistics.

### User Management
- **Authentication:** JWT-based user registration and login system.
- **Link Ownership:** Associate created links with user accounts for private management.
- **User Dashboard:** Dedicated interface for users to view and manage their history.

### Customization & Control
- **Custom Aliases:** Allow users to define their own vanity URLs (e.g., `/my-link`).
- **Expiration Dates:** Support temporary links that automatically expire after a set time.
- **Tags & Categories:** Organize links using custom tags for better filtering.

### Advanced Metrics
- **GeoIP Integration:** Resolve IP addresses to approximate country and city locations.
- **Device Tracking:** Parse User-Agent strings to identify browser and device types.
- **Referrer Analysis:** Track traffic sources to understand where clicks come from.

### Enterprise Features
- **Rate Limiting:** Protect the API against abuse using IP-based throttling.
- **QR Codes:** Generate downloadable QR codes for every short link.
- **Bulk Operations:** Support CSV import/export for managing large volumes of links.
- **Security:** Add password protection to specific short links.

### AI & Smart Features
- **Auto-Categorization:** Use ML models to analyze target URLs and suggest tags automatically.
- **Semantic Search:** Implement vector-based search to find links by content.

## Getting Started

```bash
# Start infrastructure
docker compose up -d

# Run backend (local development)
cd shriven-backend
./gradlew bootRun

# Run frontend
cd shriven-frontend
npm install
ng serve