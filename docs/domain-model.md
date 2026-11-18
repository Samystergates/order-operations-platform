# Domain Model

## Core entities

- `Order` — sales-order metadata, customer and product details, lifecycle flags, department statuses, and parent/child relationships.
- `OrderDepartment` — a department’s status and previous status for an order line.
- `MonSubOrders` — MON-related sub-order lines.
- `User`, `Role`, and `Department` — authentication, authorization, and department membership.
- Transport entities — route, truck, trailer, driver, and transport-line data.

## Workflow

Department progression is represented by status values such as `R` (ready), `Y` (in progress), and `G` (complete). Workflow updates are coordinated by the main API and propagated to order data and subscribed dashboard clients.

## Application responsibilities

- The main API owns users, orders, workflow transitions, archive operations, transport planning, and application persistence.
- The ERP adapter owns provider-specific queries and normalization of ERP/CRM data.
- The React dashboard presents operational workflows and sends commands to the main API.
