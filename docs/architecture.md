# Architecture

## System context

The platform supports internal order operations, from order intake and department workflows through transport planning, archiving, and printing.

```text
React dashboard ── REST / SockJS-STOMP ──> Order Platform API ── Feign / HTTP ──> ERP Adapter
                                                  │                                    │
                                                  v                                    v
                                           MySQL application data             ERP data over ODBC
```

## Applications

### Order Platform API

`backend/appts` is the primary Spring Boot application. It owns authentication, order persistence, workflow coordination, transport, archive, and maintenance operations. Controllers define HTTP boundaries, services coordinate use cases, repositories encapsulate JPA access, and WebSocket topics publish updates to the dashboard.

### ERP Adapter

`backend/appts2` isolates provider-specific ERP/CRM access. It queries the ERP data source through the configured JDBC/ODBC driver, maps provider rows to application DTOs, and exposes the integration endpoints used by the main API. The adapter also uses its own MySQL database for application data.

### React dashboard

`frontend` is a React single-page application. It uses REST for commands and queries, and SockJS/STOMP for live workflow updates. API clients centralize request configuration and error handling.

## Request and data flow

1. The dashboard calls the main API for user, order, archive, transport, and workflow operations.
2. The main API validates and coordinates application use cases, persists changes, and publishes relevant WebSocket updates.
3. For ERP/CRM refresh and maintenance operations, the main API calls the adapter through its Feign client.
4. The adapter queries the configured ODBC source, normalizes returned values, and responds to the main API.
5. The main API applies the returned data to its order model and reports updated state to connected dashboard clients.

## ERP connection configuration

The ERP source is accessed using the driver class, connection URL or DSN, username, and password supplied through `APP_ERP_ODBC_DRIVER_CLASS_NAME`, `APP_ERP_ODBC_URL`, `APP_ERP_ODBC_USERNAME`, and `APP_ERP_ODBC_PASSWORD`. The vendor driver is supplied by the ERP environment and is not included in the repository. It must be available to the adapter at runtime.

## Persistence and migrations

The main API and adapter use separate MySQL configurations. Hibernate schema update is the current default. Flyway migrations are included for the main API and disabled by default; deployments should select and validate a migration strategy for their database before applying changes to persistent data.

## Architectural style

The system uses a modular monolith for order operations and a separate adapter boundary for ERP connectivity. This keeps strongly related order workflows in one application while isolating provider-specific data access behind an HTTP contract.
