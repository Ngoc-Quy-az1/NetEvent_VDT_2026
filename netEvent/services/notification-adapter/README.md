# Notification Adapter (`notification-adapter`)

Standalone Spring Boot 2.7.18 / Java 11 microservice simulating the **Business Service (LHSK)** PostgreSQL database (`lhsk_db`) to produce network coverage KPI telemetry and test the **Notification Service** (ingestion, routing, approval, dispatch).

---

## 🚀 Key Responsibilities

- Operates completely isolated on database `lhsk_db` (Port `8088`).
- Never mutates or connects to Notification Service databases (`ingest_db`, `routing_db`, `audit_db`).
- Uses the shared database schema managed outside individual services.
- All database tables, column names, and constraint values are standardized to pure English.
- Provides REST endpoints to trigger 6 controlled test scenarios simulating network quality alarms.

---

## 🛠 Tech Stack

- **Java**: 11 (inherited from `netevent-parent`)
- **Framework**: Spring Boot 2.7.18 (Web, Data JPA)
- **Database**: PostgreSQL (`lhsk_db`)
- **Schema management**: shared deployment schema
- **Testing**: Testcontainers PostgreSQL

---

## 📊 Standardized Schema Tables

1. `event` (Master event registry)
2. `event_session` (Session timeframes)
3. `cell_session` (Cell mapping within a session)
4. `cell_event` (Cell role assignment - FK decoupled in V2)
5. `user_account` (Users)
6. `profile` (Notification recipient profiles)
7. `business_rule` (Business rule master configuration - V2)
8. `profile_business_rule_mapping` (Profile to Business Rule N-N mapping - V2)
9. `profile_event_session` (Profile to Event Session N-N mapping - V2)
10. `cell_overshoot` (Overshoot report - FK decoupled in V2)
11. `cell_azimuth_mismatch_and_swap_feeder` (Azimuth mismatch & feeder swap report - FK decoupled in V2)
12. `cell_twinbeam_swap` (Twinbeam swap report - FK decoupled in V2)
13. `blocked_cell` (Blocked cell report - FK decoupled in V2)
14. `azimuth_station_less_than_20_degrees` (Station azimuth < 20 deg report - FK decoupled in V2)
15. `coverage_area_by_cell` (Coverage area & Low CQI report - FK decoupled in V2)

---

## 📋 Pre-configured Scenarios

| Scenario Code | Description | Target Domain Table(s) |
| :--- | :--- | :--- |
| `SINGLE_DOMAIN_MINOR` | Minor overshoot rate warning | `cell_overshoot` |
| `SINGLE_DOMAIN_CRITICAL` | Severe throughput drop & PRB overload | `blocked_cell` |
| `MULTI_DOMAIN_PARALLEL` | Concurrent alarms across all 6 report tables | All 6 report tables |
| `INCOMPLETE_ON_PURPOSE` | Missing optional fields for DLQ/validation testing | `coverage_area_by_cell` |
| `DUPLICATE_BATCH` | Identical records pushed twice to test de-duplication | `cell_overshoot` |
| `INVALID_RECIPIENT` | Malformed recipient email/phone for dispatch failure testing | `user_account`, `profile`, `cell_overshoot` |

---

## 📡 REST API Endpoints

### 1. Run Simulation Batch
- **URL**: `POST /api/v1/simulate/batch`
- **Body**:
  ```json
  {
    "scenarioCode": "MULTI_DOMAIN_PARALLEL",
    "eventCode": "EVT-2026-FESTIVAL",
    "sessionCode": "SES-DAY1-MORNING",
    "recordCount": 5,
    "cycle": "15MIN"
  }
  ```

### 2. List Available Scenarios
- **URL**: `GET /api/v1/scenarios`

### 3. Get Session Data Summary
- **URL**: `GET /api/v1/simulate/batch/{sessionId}`

### 4. Reset Simulation Data
- **URL**: `DELETE /api/v1/simulate/reset`

---

## ⚙ Environment Variables

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8088` | HTTP Server Port |
| `DB_HOST` | `localhost` | PostgreSQL Host |
| `DB_PORT` | `5432` | PostgreSQL Port |
| `DB_NAME` | `lhsk_db` | Database Name |
| `DB_USER` | `postgres` | Database Username |
| `DB_PASSWORD` | `12345` | Database Password |
