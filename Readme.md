# UniqueHire EMS — Employee Management System

> Full-stack Employee Management System built for **UniqueHire** (uniquehire.co.in)  
> React 18 + Vite frontend · Spring Boot 3.2 backend · PostgreSQL · Redis · Kafka

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Backend Setup](#backend-setup)
- [Frontend Setup](#frontend-setup)
- [Default Credentials](#default-credentials)
- [API Reference](#api-reference)
- [Salary Calculation](#salary-calculation)
- [QR Attendance Flow](#qr-attendance-flow)
- [Kafka Events](#kafka-events)
- [Database Schema](#database-schema)
- [Environment Variables](#environment-variables)
- [Troubleshooting](#troubleshooting)

---

## Tech Stack

| Layer       | Technology                                      |
|-------------|-------------------------------------------------|
| Frontend    | React 18, Vite 5, CSS-in-JS                     |
| Backend     | Spring Boot 3.2, Maven, Java 17                 |
| ORM         | Hibernate + Spring Data JPA                     |
| Database    | PostgreSQL 16                                   |
| Cache       | Redis 7 (QR token storage)                      |
| Messaging   | Apache Kafka 3.6 (event-driven notifications)   |
| Security    | Spring Security + JWT (HS256)                   |
| QR Code     | Google ZXing 3.5                                |
| Container   | Docker + Docker Compose                         |

---

## Project Structure

```
UniqueHire-EMS/
│
├── ems-final/                          ← Spring Boot Backend
│   ├── pom.xml
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── src/main/java/com/uniquehire/ems/
│       ├── UniqueHireEmsApplication.java
│       ├── config/
│       │   ├── JwtUtil.java
│       │   ├── JwtAuthFilter.java
│       │   ├── CustomUserDetailsService.java
│       │   ├── SecurityConfig.java
│       │   ├── KafkaConfig.java
│       │   └── RedisConfig.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── EmployeeController.java
│       │   ├── AttendanceController.java
│       │   ├── LeaveController.java
│       │   ├── PayrollController.java
│       │   ├── PerformanceController.java
│       │   └── DashboardController.java
│       ├── service/
│       │   ├── interfaces/             ← 7 service contracts
│       │   └── impl/                   ← 8 implementations
│       ├── repository/                 ← 9 Spring Data JPA repos
│       ├── entity/                     ← 10 JPA entities
│       ├── dto/                        ← 27 request/response DTOs
│       ├── kafka/                      ← Events + Producer + Consumer
│       ├── exception/                  ← Custom exceptions + GlobalHandler
│       └── util/
│           └── DateUtil.java
│
└── ems-integrated/                     ← React Frontend (integrated)
    ├── package.json
    ├── vite.config.js
    ├── index.html
    ├── .env
    └── src/
        ├── App.jsx                     ← Root: auth check + state + routing
        ├── main.jsx
        ├── data/
        │   ├── constants.js            ← Brand tokens, color maps
        │   └── mockData.js             ← Fallback/seed data
        ├── styles/
        │   └── globalStyles.js         ← All CSS
        ├── components/
        │   ├── Sidebar.jsx
        │   ├── Topbar.jsx
        │   ├── QRCode.jsx
        │   └── AddEmployeeModal.jsx
        ├── pages/
        │   ├── Login.jsx
        │   ├── Dashboard.jsx
        │   ├── Employees.jsx
        │   ├── Attendance.jsx
        │   ├── Payroll.jsx
        │   ├── Performance.jsx
        │   └── Leave.jsx
        └── utils/
            ├── api.js                  ← All REST API calls
            └── helpers.js              ← Utility functions
```

---

## Prerequisites

Make sure these are installed before starting:

| Tool          | Version  | Check Command          |
|---------------|----------|------------------------|
| Java          | 17+      | `java -version`        |
| Maven         | 3.9+     | `mvn -version`         |
| Node.js       | 18+      | `node --version`       |
| npm           | 9+       | `npm --version`        |
| Docker        | 24+      | `docker --version`     |
| Docker Compose| 2+       | `docker compose version` |

---

## Quick Start

### 4-command startup

```bash
# 1 — Start infrastructure
cd ems-final
docker compose up -d postgres redis zookeeper kafka kafka-ui

# 2 — Start backend (wait ~30s for Kafka to be ready)
mvn clean install -DskipTests
mvn spring-boot:run

# 3 — Start frontend (new terminal)
cd ems-integrated
npm install
npm run dev

# 4 — Open browser
# Frontend  →  http://localhost:5173
# API       →  http://localhost:8080
# Kafka UI  →  http://localhost:8090
```

---

## Backend Setup

### Step 1 — Start Docker services

```bash
cd ems-final
docker compose up -d postgres redis zookeeper kafka kafka-ui
```

Wait ~30 seconds, then verify:

```bash
docker compose ps
# All services should show "healthy"
```

### Step 2 — Schema is auto-applied

The `schema.sql` is mounted as an init script in Docker.  
It creates all 10 tables, indexes, triggers, and seed data automatically.

To run manually if needed:

```bash
docker exec -i ems-postgres psql -U ems_user -d uniquehire_ems \
  < src/main/resources/schema.sql
```

### Step 3 — Build and run

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

App starts at **http://localhost:8080**

Health check:

```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

---

## Frontend Setup

```bash
cd ems-integrated
npm install
npm run dev
```

Frontend runs at **http://localhost:5173**

### Environment variable

The `.env` file is already configured:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Change the URL if your backend runs on a different host or port.

---

## Default Credentials

| Username | Password   | Role  |
|----------|------------|-------|
| `admin`  | `Admin@123`| ADMIN |

Change password after first login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"oldPassword":"Admin@123","newPassword":"NewPass@456"}'
```

---

## API Reference

### Authentication

| Method | Endpoint                        | Description              | Auth |
|--------|---------------------------------|--------------------------|------|
| POST   | `/api/v1/auth/login`            | Login, get JWT token     | No   |
| POST   | `/api/v1/auth/refresh`          | Refresh access token     | No   |
| POST   | `/api/v1/auth/change-password`  | Change password          | Yes  |

**Login example:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

---

### Employees

| Method | Endpoint                                       | Description                     |
|--------|------------------------------------------------|---------------------------------|
| GET    | `/api/v1/employees`                            | List all (page, size, search)   |
| GET    | `/api/v1/employees/{id}`                       | Get by ID                       |
| POST   | `/api/v1/employees`                            | Create new employee             |
| PUT    | `/api/v1/employees/{id}`                       | Update employee                 |
| DELETE | `/api/v1/employees/{id}`                       | Deactivate employee             |
| PATCH  | `/api/v1/employees/{id}/status?status=ACTIVE`  | Change status                   |
| POST   | `/api/v1/employees/{id}/photo`                 | Upload profile photo            |
| GET    | `/api/v1/employees/{id}/reports`               | Get direct reports              |
| GET    | `/api/v1/employees/{id}/attendance-summary`    | Monthly attendance summary      |

**Create employee example:**

```bash
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Arjun",
    "lastName": "Sharma",
    "email": "arjun@uniquehire.co.in",
    "designation": "Senior Engineer",
    "departmentId": 1,
    "dateOfJoining": "2026-03-15",
    "annualSalary": 1200000
  }'
```

---

### Attendance

| Method | Endpoint                                       | Description                      |
|--------|------------------------------------------------|----------------------------------|
| GET    | `/api/v1/attendance/qr-code`                   | Live QR PNG (no auth needed)     |
| POST   | `/api/v1/attendance/checkin/qr`                | QR scan check-in                 |
| POST   | `/api/v1/attendance/checkin/wifi`              | WiFi auto check-in               |
| POST   | `/api/v1/attendance/checkin/manual`            | Manual HR override               |
| POST   | `/api/v1/attendance/checkout`                  | Record checkout + work hours     |
| GET    | `/api/v1/attendance/today`                     | Today's attendance log           |
| GET    | `/api/v1/attendance/today/stats`               | Stat counts (office/WFH/absent)  |
| GET    | `/api/v1/attendance/weekly?weekStart=2026-03-10` | Weekly chart data              |
| GET    | `/api/v1/attendance/summary/{empId}?month=2026-03` | Monthly summary             |

**QR check-in example:**

```bash
curl -X POST http://localhost:8080/api/v1/attendance/checkin/qr \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"employeeId": 1, "token": "SCANNED_TOKEN_HERE"}'
```

---

### Leave

| Method | Endpoint                              | Description                    |
|--------|---------------------------------------|--------------------------------|
| POST   | `/api/v1/leaves`                      | Apply for leave                |
| GET    | `/api/v1/leaves`                      | All leaves (filter by status)  |
| PUT    | `/api/v1/leaves/{id}/approve`         | Approve leave                  |
| PUT    | `/api/v1/leaves/{id}/reject`          | Reject leave                   |
| PUT    | `/api/v1/leaves/{id}/cancel`          | Cancel leave                   |
| GET    | `/api/v1/leaves/{empId}/balance`      | Leave balance breakdown        |
| GET    | `/api/v1/leaves/types`                | All leave types                |
| GET    | `/api/v1/leaves/pending/manager/{id}` | Manager's inbox                |

---

### Payroll

| Method | Endpoint                                       | Description                    |
|--------|------------------------------------------------|--------------------------------|
| POST   | `/api/v1/payroll/process`                      | Bulk process all employees     |
| POST   | `/api/v1/payroll/process/{empId}?month=2026-03`| Process single employee        |
| GET    | `/api/v1/payroll?month=2026-03`                | All payroll for month          |
| GET    | `/api/v1/payroll/summary?month=2026-03`        | Summary + 6-month trend        |
| GET    | `/api/v1/payroll/employee/{empId}`             | Employee payroll history       |
| PUT    | `/api/v1/payroll/{id}/mark-paid`               | Mark as paid                   |
| GET    | `/api/v1/payroll/tax-preview?salary=1200000`   | Tax breakdown preview          |

**Run payroll example:**

```bash
curl -X POST http://localhost:8080/api/v1/payroll/process \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"payrollMonth": "2026-03-01", "processedById": 1}'
```

---

### Performance

| Method | Endpoint                                        | Description              |
|--------|-------------------------------------------------|--------------------------|
| POST   | `/api/v1/performance`                           | Create review (DRAFT)    |
| PUT    | `/api/v1/performance/{id}`                      | Update draft review      |
| PUT    | `/api/v1/performance/{id}/submit`               | Submit review            |
| PUT    | `/api/v1/performance/{id}/acknowledge`          | Employee acknowledges    |
| GET    | `/api/v1/performance?period=Q1-2026`            | Reviews for a period     |
| GET    | `/api/v1/performance/employee/{empId}/history`  | Employee's review history|
| GET    | `/api/v1/performance/top-performers?period=Q1-2026&limit=5` | Leaderboard |

---

### Dashboard

| Method | Endpoint                  | Description                        |
|--------|---------------------------|------------------------------------|
| GET    | `/api/v1/dashboard/stats` | All stats in a single call         |

---

## Salary Calculation

```
Monthly Basic     =  Annual Salary ÷ 12
HRA               =  Basic × 20%
Special Allowance =  Basic × 10%
─────────────────────────────────────
Gross Salary      =  Basic + HRA + Special Allowance

PF Deduction      =  Basic × 12%
Professional Tax  =  ₹200/month (flat)
Income Tax        =  Annual Tax (new regime) ÷ 12
─────────────────────────────────────
Net Salary        =  Gross − PF − PT − Income Tax − Leave Deduction
```

### Indian Income Tax Slabs — New Regime FY 2025-26

| Slab              | Rate |
|-------------------|------|
| ₹0 – ₹4L          | 0%   |
| ₹4L – ₹8L         | 5%   |
| ₹8L – ₹12L        | 10%  |
| ₹12L – ₹16L       | 15%  |
| ₹16L – ₹20L       | 20%  |
| ₹20L – ₹24L       | 25%  |
| Above ₹24L        | 30%  |

Plus **4% Health & Education Cess** on total tax.  
Minus **₹75,000 Standard Deduction**.

---

## QR Attendance Flow

```
1.  Admin opens dashboard
         ↓
2.  GET /api/v1/attendance/qr-code → live PNG image served
         ↓
3.  QR token rotates every 30 seconds (stored in Redis)
         ↓
4.  Employee opens mobile app → scans QR code at office gate
         ↓
5.  App extracts token from payload:
    uniquehire://attendance/checkin?token=<TOKEN>&ts=<TIMESTAMP>
         ↓
6.  POST /api/v1/attendance/checkin/qr  { employeeId, token }
         ↓
7.  Backend validates token in Redis
    → marks PRESENT, location = OFFICE
    → invalidates token (one-time use)
         ↓
8.  Kafka event published → confirmation notification sent
```

### WiFi Auto-Detection Flow

```
1.  Employee opens mobile app
2.  App detects connected WiFi SSID
3.  POST /api/v1/attendance/checkin/wifi  { employeeId, ssid }
4.  SSID matches company list → OFFICE
    SSID doesn't match       → WFH
5.  Attendance marked automatically — no QR scan needed for WFH
```

Company WiFi SSIDs configured in `application.properties`:

```properties
app.qr.company-wifi-ssids=Corp-WiFi-5G,UniqueHire-Office,UH-Internal
```

---

## Kafka Events

| Topic                      | Trigger                  | Consumer Action                    |
|----------------------------|--------------------------|------------------------------------|
| `ems.attendance.marked`    | QR / WiFi check-in       | Send attendance confirmation email |
| `ems.leave.requested`      | Employee applies leave   | Notify manager                     |
| `ems.leave.approved`       | Leave approved           | Notify employee                    |
| `ems.leave.rejected`       | Leave rejected           | Notify employee                    |
| `ems.payroll.processed`    | Payroll run              | Send payslip email                 |
| `ems.employee.onboarded`   | New employee created     | Send welcome email                 |

Monitor all topics live at **http://localhost:8090** (Kafka UI).

---

## Database Schema

10 tables are created automatically by `schema.sql`:

| Table                  | Description                             |
|------------------------|-----------------------------------------|
| `departments`          | 7 departments (Engineering, HR, etc.)   |
| `employees`            | Core employee records                   |
| `users`                | Authentication accounts                 |
| `attendance`           | Daily check-in/check-out records        |
| `qr_tokens`            | QR token audit trail (live = Redis)     |
| `leave_types`          | 7 leave types (Annual, Sick, etc.)      |
| `leave_requests`       | Leave applications and approvals        |
| `payroll`              | Monthly payroll records                 |
| `performance_reviews`  | Quarterly review records                |
| `audit_logs`           | All create/update/delete actions        |

---

## Environment Variables

### Backend — `application.properties`

| Property                          | Default                       | Description                      |
|-----------------------------------|-------------------------------|----------------------------------|
| `spring.datasource.url`           | `jdbc:postgresql://localhost:5432/uniquehire_ems` | DB URL |
| `spring.datasource.username`      | `ems_user`                    | DB username                      |
| `spring.datasource.password`      | `ems_password`                | DB password                      |
| `spring.data.redis.host`          | `localhost`                   | Redis host                       |
| `spring.kafka.bootstrap-servers`  | `localhost:9092`              | Kafka broker                     |
| `app.jwt.secret`                  | (long string)                 | JWT signing key (change in prod) |
| `app.jwt.expiration-ms`           | `86400000`                    | Token TTL (24 hours)             |
| `app.qr.token-ttl-seconds`        | `30`                          | QR token rotation interval       |
| `app.qr.company-wifi-ssids`       | `Corp-WiFi-5G,...`            | Comma-separated office SSIDs     |

### Frontend — `.env`

| Variable               | Default                          | Description         |
|------------------------|----------------------------------|---------------------|
| `VITE_API_BASE_URL`    | `http://localhost:8080/api/v1`   | Backend API base URL|

---

## Port Summary

| Service      | Port  | URL                          |
|--------------|-------|------------------------------|
| Frontend     | 5173  | http://localhost:5173        |
| Backend API  | 8080  | http://localhost:8080        |
| PostgreSQL   | 5432  | localhost:5432               |
| Redis        | 6379  | localhost:6379               |
| Kafka Broker | 9092  | localhost:9092               |
| Kafka UI     | 8090  | http://localhost:8090        |

---

## Troubleshooting

| Error                            | Fix                                                                 |
|----------------------------------|---------------------------------------------------------------------|
| `Port 5432 already in use`       | Stop local PostgreSQL: `sudo service postgresql stop`               |
| `Port 6379 already in use`       | Stop local Redis: `sudo service redis stop`                         |
| `Kafka connection refused`       | Wait 30s after `docker compose up`, then run `mvn spring-boot:run` |
| `401 Unauthorized`               | Add header: `Authorization: Bearer <token>`                        |
| `CORS error in browser`          | Frontend must run on `:3000` or `:5173` — both are whitelisted     |
| `QR code returns 500`            | Redis must be running — check `docker compose ps`                   |
| `BUILD FAILURE`                  | Run `java -version` — must be Java 17+                             |
| `Missing script: dev`            | Make sure `package.json` is at root, not in a subfolder            |
| `npm audit vulnerabilities`      | Run `npm audit fix` — these are dev-only, not security critical    |
| `SchemaValidationException`      | Re-run `schema.sql` via `docker exec` command above               |
| `Token not found in Redis`       | QR token expired. Refresh the QR image — it auto-rotates every 30s |

### Stop everything

```bash
# Stop all containers
docker compose down

# Stop and wipe all data (fresh start)
docker compose down -v
```

### Useful debug commands

```bash
# Live backend logs
mvn spring-boot:run

# View Kafka topics
docker exec -it ems-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Connect to PostgreSQL
docker exec -it ems-postgres psql -U ems_user -d uniquehire_ems

# Connect to Redis CLI
docker exec -it ems-redis redis-cli

# Check current QR token in Redis
docker exec -it ems-redis redis-cli GET qr:current_token
```

---

## License

Built for UniqueHire — uniquehire.co.in  
© 2026 UniqueHire. All rights reserved.
