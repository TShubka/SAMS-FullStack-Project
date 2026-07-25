# Student Academic Records & Attendance Management System

**Group 8** — Full-Stack Project (Spring Boot + ReactJS), 2025–2026

A web application for managing students, courses, attendance, marks, transcripts
and department reports, with three roles — **Administrator, Teacher, Student** —
each seeing a different, real, database-backed view of the data.

---

## Table of contents

1. [Technology stack](#technology-stack)
2. [Architecture](#architecture)
3. [Features](#features)
4. [Prerequisites](#prerequisites)
5. [Database setup](#database-setup)
6. [Running the backend](#running-the-backend)
7. [Running the frontend](#running-the-frontend)
8. [Demo accounts](#demo-accounts)
9. [Project structure](#project-structure)
10. [API overview](#api-overview)
11. [Testing](#testing)
12. [Documentation](#documentation)

---

## Technology stack

| Layer | Technology |
|---|---|
| Backend | Java 21 (LTS), Spring Boot 3.4.1, Maven |
| Web / API | Spring Web (REST) |
| Persistence | Spring Data JPA + Hibernate |
| Security | Spring Security + JWT (JJWT 0.12.6), BCrypt |
| Validation | Bean Validation (Hibernate Validator) |
| Database | PostgreSQL 18 |
| Frontend | ReactJS 18.3, Vite, Axios, React Router v6 |
| Styling | Plain CSS (responsive, no framework) |
| Version control | Git + GitHub |

> The specification asks for "Java 17+" and "Spring Boot 3.x". We use Java 21
> because it is the current LTS and the machine's default Java 26 is not yet
> supported by Spring Boot 3.x.

---

## Architecture

```
ReactJS (Vite)  ──HTTP/JSON──►  Spring Boot REST Controller
                                      │
                                      ▼
                                  Service  (business logic, @Transactional)
                                      │
                                      ▼
                                 Repository / JPA
                                      │
                                      ▼
                                  PostgreSQL
```

- **Controllers** handle HTTP only — validation via `@Valid`, no business logic.
- **Services** hold all business rules and transaction boundaries.
- **Repositories** are Spring Data JPA interfaces; reports use aggregate queries.
- **DTOs** cross every boundary — entities are never serialized, so the password
  hash is never exposed and there are no lazy-loading serialization failures.
- **Centralized exception handling** (`@RestControllerAdvice`) maps every error to
  a consistent JSON envelope with the correct HTTP status.

Authentication is **stateless JWT**: login returns a signed token, and every
subsequent request carries it in the `Authorization: Bearer` header. Authorization
is two-layered — coarse role checks with `@PreAuthorize`, plus fine-grained
ownership checks in the services (a teacher may only touch their own courses; a
student may only read their own records).

---

## Features

- **Authentication & authorization** — register, login, JWT, three roles, protected
  routes, role-based navigation
- **CRUD** — departments, students, teachers, courses, enrollments (with search,
  filtering, pagination)
- **Attendance** — single and bulk recording, percentages, per-course summaries,
  low-attendance alerts, teacher-owns-course enforcement, student self-view
- **Marks & grades** — assessments with weights, mark entry with range validation,
  weighted percentage, letter grade, grade points
- **GPA** — credit-weighted semester and cumulative GPA
- **Transcript** — assembled on demand, grouped by semester, with GPA
- **Reports** — students by department, student performance, attendance by course,
  low attendance, course performance, grade distribution, pass/fail, department
  performance
- **Dashboards** — Admin, Teacher and Student, every figure from a live query

---

## Prerequisites

- **JDK 21** (Eclipse Temurin recommended)
- **PostgreSQL 18** running on `localhost:5432`
- **Node.js 18+** and npm
- Maven is **not** required — the project ships the Maven Wrapper (`mvnw`)

---

## Database setup

1. Create the database:

   ```bash
   "C:\Program Files\PostgreSQL\18\bin\createdb.exe" -U postgres -h localhost sams_db
   ```

2. Provide the database password to the backend through an environment variable
   (never committed):

   ```bash
   setx DB_PASSWORD "your_postgres_password"
   ```

   Open a new terminal so the variable is picked up.

The schema is created automatically on first run (dev profile, Hibernate
`ddl-auto: update`), and the `DataSeeder` populates realistic demo data.

Other settings and their defaults (override via environment variables):

| Variable | Default |
|---|---|
| `DB_HOST` | localhost |
| `DB_PORT` | 5432 |
| `DB_NAME` | sams_db |
| `DB_USERNAME` | postgres |
| `DB_PASSWORD` | *(required)* |
| `JWT_SECRET` | dev placeholder (set your own in prod) |
| `CORS_ORIGINS` | http://localhost:5173 |

---

## Running the backend

From the `backend/` directory:

```bash
./mvnw spring-boot:run
```

On Windows with a specific JDK:

```bash
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot
mvnw.cmd -B spring-boot:run
```

The API starts on **http://localhost:8080**. Health check:
`GET http://localhost:8080/api/health`.

---

## Running the frontend

From the `frontend/` directory:

```bash
npm install     # first time only
npm run dev
```

The app starts on **http://localhost:5173** and proxies API calls to
`http://localhost:8080/api` (configurable via `VITE_API_BASE_URL` in `.env`).

> **Tip:** the project includes `.claude/launch.json` so both servers can be
> started by name from the Claude Code preview UI. If you cloned to a different
> path, update the two `cwd` values in that file.

---

## Demo accounts

All demo accounts use the password **`Password123`** and log in by **username**.

| Role | Username | Notes |
|---|---|---|
| Administrator | `admin` | full access |
| Teacher | `t.smith` | teaches CS301, CS302 |
| Teacher | `t.khan` | teaches EE201 |
| Student | `cs.student1` … `cs.student8` | CS students |
| Student | `ee.student1`, `ee.student2` | EE students |

`cs.student1` has a 4.00 GPA; `cs.student7` and `cs.student8` are deliberately
below the 75% attendance threshold so the low-attendance features have real data.

---

## Project structure

```
Student Attendent/
├── backend/                     Spring Boot application
│   └── src/main/java/com/group6/sams/
│       ├── config/  controller/  dto/  entity/  repository/
│       ├── service/ service/impl/ security/ exception/ mapper/ util/
├── frontend/                    React application
│   └── src/
│       ├── components/ pages/ services/ context/ hooks/
│       ├── layouts/ routes/ utils/
├── tests/                       integration suite + QA results
├── docs/                        ERD, project report, contributions, GenAI note
└── README.md
```

---

## API overview

Base path `/api`. All endpoints except `/auth/register` and `/auth/login` require a
Bearer token. Full list in [docs/API.md](docs/API.md).

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/register`, `POST /auth/login`, `GET /auth/me` |
| Core CRUD | `/departments`, `/students`, `/teachers`, `/courses`, `/enrollments` |
| Attendance | `/attendance`, `/attendance/bulk`, `/attendance/percentage`, `/attendance/low` |
| Marks & grades | `/assessments`, `/marks`, `/grades/*`, `/grades/gpa/*` |
| Transcript & reports | `/transcripts/*`, `/reports/*` |
| Dashboards | `/dashboard/{admin,teacher,student}` |

HTTP status codes: 200, 201, 204, 400, 401, 403, 404, 405, 409, 500.

---

## Testing

**Unit tests** (JWT, attendance percentage, grading/GPA):

```bash
cd backend
./mvnw test
```

23 tests, all passing.

**Integration suite** (52 end-to-end checks across all three roles):

```powershell
./tests/integration-test.ps1
```

See [tests/PHASE11_INTEGRATION_RESULTS.md](tests/PHASE11_INTEGRATION_RESULTS.md)
and [tests/PHASE12_QA_SECURITY.md](tests/PHASE12_QA_SECURITY.md).

---

## Documentation

- [docs/ERD.md](docs/ERD.md) — entity–relationship diagram and schema
- [docs/PROJECT_REPORT.md](docs/PROJECT_REPORT.md) — the 3–5 page project report
- [docs/CONTRIBUTIONS.md](docs/CONTRIBUTIONS.md) — individual member contributions
- [docs/GENAI_ACKNOWLEDGEMENT.md](docs/GENAI_ACKNOWLEDGEMENT.md) — GenAI usage
- [docs/API.md](docs/API.md) — full API reference
"# SAMS-FullStack-Project" 
