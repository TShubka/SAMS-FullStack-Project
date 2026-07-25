# PHASE 0 — PROJECT ANALYSIS
**Group 8 — Student Academic Records & Attendance Management System**
Full-Stack Project (Spring Boot + ReactJS), 2025–2026
Status: **ANALYSIS ONLY — NO CODE WRITTEN.** Awaiting approval to enter Phase 1.

---

## 1. Project Overview

A web-based academic management system for a single institution. It replaces manual
registers and spreadsheets for three groups of users: administrators who manage the
institutional data, teachers who record attendance and marks for the courses assigned
to them, and students who view their own attendance, grades, GPA and transcript.

The system is a **single Spring Boot REST backend** + a **single ReactJS SPA frontend**
+ a **single PostgreSQL database**. Every number shown in a dashboard, report or
transcript is computed from real rows in PostgreSQL through the REST API. No hard-coded
or mocked statistics anywhere.

**Request flow (fixed by spec):**

```
React component → Axios service (JWT header)
    → Spring Boot @RestController  (HTTP concerns only)
        → @Service                 (business logic, @Transactional)
            → JpaRepository        (data access only)
                → PostgreSQL
```

**Deliberate non-goals:** microservices, Redis, Docker, WebSockets, AI features,
payments, mobile apps, cloud infrastructure. Section 13 of the spec forbids them.

---

## 2. Requirements

### 2.1 Functional Requirements

| ID | Requirement | Owner |
|---|---|---|
| FR-01 | User registration `POST /api/auth/register` | M1 |
| FR-02 | User login returning a JWT `POST /api/auth/login` | M1 |
| FR-03 | Passwords stored only as BCrypt hashes | M1 |
| FR-04 | All non-auth endpoints require a valid JWT | M1 |
| FR-05 | Role-based authorization (ADMIN / TEACHER / STUDENT) | M1 |
| FR-06 | Frontend protected routes + role-based navigation | M1 |
| FR-07 | Full CRUD: Departments | M2 |
| FR-08 | Full CRUD: Students (linked to a User + Department) | M2 |
| FR-09 | Full CRUD: Teachers (linked to a User + Department) | M2 |
| FR-10 | Full CRUD: Courses (belong to a Department, assigned a Teacher) | M2 |
| FR-11 | Enrollment of a student into a course for a semester | M2 |
| FR-12 | Duplicate enrollment prevented (DB unique constraint + service check) | M2 |
| FR-13 | Search + filter + pagination on list screens | M2 |
| FR-14 | Record attendance per student per course per date (PRESENT/ABSENT/LATE) | M3 |
| FR-15 | Update an existing attendance record | M3 |
| FR-16 | Attendance percentage per student per course | M3 |
| FR-17 | Attendance summary per course | M3 |
| FR-18 | Low-attendance alert list (below configurable threshold, default 75%) | M3 |
| FR-19 | A teacher may only touch attendance for courses assigned to them | M3 |
| FR-20 | A student may only read their own attendance | M3 |
| FR-21 | CRUD assessments per course (ASSIGNMENT/QUIZ/MIDTERM/FINAL, max marks, weight) | M3 |
| FR-22 | Enter/update marks per enrollment per assessment | M3 |
| FR-23 | Marks validated `0 <= obtained <= assessment.maxMarks` | M3 |
| FR-24 | Weighted total, percentage, letter grade, grade point per enrollment | M3 |
| FR-25 | Students cannot create or modify marks (403) | M3 |
| FR-26 | Semester GPA and CGPA per student (credit-weighted) | M4 |
| FR-27 | Transcript: student info, department, courses, credits, marks, grades, grade points, semester, GPA | M4 |
| FR-28 | Reports: students by department/year, academic performance, attendance by student/course, low attendance, course performance, grade distribution, pass/fail stats, department performance | M4 |
| FR-29 | Admin dashboard (counts + academic overview) | M4 |
| FR-30 | Teacher dashboard (assigned courses, students, attendance, marks, performance) | M4 |
| FR-31 | Student dashboard (profile, courses, attendance, grades, GPA, summary) | M4 |
| FR-32 | Centralized exception handling with correct HTTP status codes | M1 (shared) |

### 2.2 Non-Functional Requirements

- **NFR-01 Layering** — no business logic in controllers, no data access in controllers,
  no HTTP types in services. Enforced in code review.
- **NFR-02 Injection** — constructor injection only; no field `@Autowired`.
- **NFR-03 Validation** — Bean Validation on every request DTO; `@Valid` on every
  `@RequestBody`.
- **NFR-04 Transactions** — `@Transactional` on write services, `readOnly = true` on reads.
- **NFR-05 Serialization** — entities never leave the API; response DTOs only. This is
  also how we avoid Jackson lazy-loading / infinite-recursion failures.
- **NFR-06 Security** — stateless JWT, BCrypt, `SessionCreationPolicy.STATELESS`, CSRF
  disabled (no cookies), CORS limited to the Vite dev origin.
- **NFR-07 Integrity** — FKs, NOT NULL and UNIQUE constraints declared in the schema,
  not only in Java.
- **NFR-08 Performance** — indexes on all FK columns and on the columns reports filter
  by; aggregate reports use JPQL/native aggregate queries, never in-memory loops over
  full tables.
- **NFR-09 Usability** — every screen has loading, empty, error and success states.
- **NFR-10 Responsive** — usable at desktop, tablet and mobile widths.
- **NFR-11 Portability** — `mvn spring-boot:run` + `npm run dev` on any machine with
  Java 17, Maven and PostgreSQL 14; documented in README.
- **NFR-12 Defensibility** — every member can explain their own files line by line.

---

## 3. Users and Roles

| Role | Spring authority | Who | Can do |
|---|---|---|---|
| ADMIN | `ROLE_ADMIN` | Registrar / office staff | Everything: manage departments, students, teachers, courses, enrollments, users; view all attendance, marks, transcripts and reports |
| TEACHER | `ROLE_TEACHER` | Faculty | Read their assigned courses and enrolled students; create/update attendance and marks for **those courses only**; view course performance |
| STUDENT | `ROLE_STUDENT` | Enrolled student | Read **their own** profile, courses, attendance, marks, grades, GPA, transcript. Read-only throughout |

Notes on the spec's "minimum ROLE_USER and ROLE_ADMIN": `ROLE_STUDENT` and
`ROLE_TEACHER` are our concrete domain roles; both are ordinary (non-admin) users, so
the minimum requirement is satisfied and exceeded. We will define a `ROLE_USER`
authority granted to every authenticated account so the spec's literal wording is met
and generic "any logged-in user" endpoints (e.g. `/api/users/me`) can use it.

**Ownership rules (the two rules the viva will probe):**
1. Teacher writes are authorized by `course.teacher.user.id == authenticatedUserId`,
   checked **in the service layer**, not only by URL.
2. Student reads are authorized by `student.user.id == authenticatedUserId`. A student
   requesting another student's id gets **403**, not 404.

---

## 4. Modules (complete list)

1. **User & Authentication** — register, login, JWT issue/validate, current user, user admin.
2. **Department Management** — CRUD, unique code, cannot delete while referenced.
3. **Student Management** — CRUD, roll number unique, belongs to a department, linked 1:1 to a user.
4. **Teacher Management** — CRUD, employee code unique, belongs to a department, linked 1:1 to a user.
5. **Course Management** — CRUD, course code unique, credits, semester, department, assigned teacher.
6. **Course Enrollment** — enroll/drop, unique (student, course, semester, academicYear).
7. **Attendance Management** — record/update, per-date status, percentages, summaries, low-attendance alerts.
8. **Assessments & Marks** — assessment definitions per course, mark entry with range validation.
9. **Grades & GPA** — weighted totals, letter grade, grade points, semester GPA, CGPA.
10. **Academic Transcript** — assembled per student across semesters.
11. **Department & Academic Reports** — the eight reports in FR-28.
12. **Role-Based Dashboards** — three dashboards, all fed by real aggregate queries.

---

## 5. Entity List

| # | Entity | Purpose | Key fields |
|---|---|---|---|
| 1 | `User` | Login identity | id, username(U), email(U), password(hash), enabled, createdAt |
| 2 | `Role` | Authority | id, name(U) — ROLE_ADMIN / ROLE_TEACHER / ROLE_STUDENT / ROLE_USER |
| 3 | `user_roles` | Join table | user_id + role_id (composite PK) |
| 4 | `Department` | Academic dept | id, name(U), code(U) |
| 5 | `Student` | Student profile | id, user_id(U,FK), department_id(FK), rollNumber(U), firstName, lastName, admissionYear, currentSemester, phone |
| 6 | `Teacher` | Teacher profile | id, user_id(U,FK), department_id(FK), employeeCode(U), firstName, lastName, designation, phone |
| 7 | `Course` | Course offering | id, code(U), title, credits, semester, department_id(FK), teacher_id(FK, nullable) |
| 8 | `Enrollment` | Student↔Course link | id, student_id(FK), course_id(FK), semester, academicYear, enrolledOn, status — **U(student, course, semester, academicYear)** |
| 9 | `Attendance` | One class-day record | id, enrollment_id(FK), date, status(enum), remarks, recordedBy_teacher_id(FK) — **U(enrollment_id, date)** |
| 10 | `Assessment` | Graded item | id, course_id(FK), title, type(enum), maxMarks, weightPercent, assessedOn — **U(course_id, title)** |
| 11 | `Mark` | One score | id, enrollment_id(FK), assessment_id(FK), marksObtained, enteredBy_teacher_id(FK), enteredAt — **U(enrollment_id, assessment_id)** |

**Transcript is NOT a table.** It is derived on demand from Enrollment + Mark +
Assessment + Course. Storing it would duplicate data and risk it going stale — that is
the correct normalization answer and a likely viva question. If performance requires it
later we will add a read-only view, not a table.

Grade scale lives in a `GradeUtil` in `util/` (single source of truth, used by both the
marks module and the GPA/transcript module):

| % | Grade | Points |
|---|---|---|
| 90–100 | A+ | 4.0 |
| 80–89 | A | 3.7 |
| 70–79 | B | 3.3 |
| 60–69 | C | 2.7 |
| 50–59 | D | 2.0 |
| < 50 | F | 0.0 |

`GPA = Σ(gradePoints × credits) / Σ(credits)` over the semester's enrollments.
`CGPA` = same formula over all completed enrollments.

---

## 6. Database Relationships

| From | To | Cardinality | Notes |
|---|---|---|---|
| User | Role | M:N | via `user_roles` |
| User | Student | 1:1 | `students.user_id` UNIQUE, NOT NULL |
| User | Teacher | 1:1 | `teachers.user_id` UNIQUE, NOT NULL |
| Department | Student | 1:M | student must have a department |
| Department | Teacher | 1:M | |
| Department | Course | 1:M | |
| Teacher | Course | 1:M | a course has at most one teacher; nullable until assigned |
| Student | Enrollment | 1:M | |
| Course | Enrollment | 1:M | |
| Enrollment | Attendance | 1:M | attendance hangs off enrollment, not off (student, course) separately — this guarantees you can't mark attendance for a student not enrolled |
| Course | Assessment | 1:M | |
| Enrollment | Mark | 1:M | |
| Assessment | Mark | 1:M | |

**Why Enrollment exists:** Student↔Course is many-to-many, and the relationship itself
carries data (semester, academic year, status, and it owns attendance and marks). A
plain join table cannot hold that, so it is promoted to a first-class entity.

**Duplicate prevention:** `UNIQUE(student_id, course_id, semester, academic_year)` on
`enrollments`, `UNIQUE(enrollment_id, date)` on `attendance`, `UNIQUE(enrollment_id,
assessment_id)` on `marks`. Each is enforced in the DB *and* pre-checked in the service
so the user gets a clean **409 Conflict** instead of a raw constraint-violation 500.

**Indexes:** every FK column, plus `attendance(date)`, `enrollments(semester,
academic_year)`, `students(department_id)`, `courses(department_id)`.

**JPA choices:** all `@ManyToOne` are `FetchType.LAZY`; no `CascadeType.REMOVE` across
aggregate boundaries; bidirectional collections are avoided unless needed, and entities
are never serialized — DTOs only. That removes the classic Jackson recursion and
`LazyInitializationException` failures without needing `@JsonIgnore` patches everywhere.

ERD diagram is a **Phase 1** deliverable.

---

## 7. Backend Architecture

```
src/main/java/com/group6/sams/
├── config/          CorsConfig, JpaAuditingConfig, DataSeeder
├── controller/      AuthController, UserController, DepartmentController,
│                    StudentController, TeacherController, CourseController,
│                    EnrollmentController, AttendanceController,
│                    AssessmentController, MarkController,
│                    TranscriptController, ReportController, DashboardController
├── dto/             request/  + response/  (one package each)
├── entity/          User, Role, Department, Student, Teacher, Course,
│                    Enrollment, Attendance, Assessment, Mark + enums
├── repository/      one JpaRepository per aggregate + report projections
├── service/         interfaces
├── service/impl/    implementations (all @Transactional business logic)
├── security/        JwtTokenProvider, JwtAuthenticationFilter,
│                    CustomUserDetailsService, SecurityConfig,
│                    JwtAuthEntryPoint, OwnershipGuard
├── exception/       GlobalExceptionHandler(@RestControllerAdvice),
│                    ResourceNotFoundException, DuplicateResourceException,
│                    BusinessRuleException, UnauthorizedActionException,
│                    ErrorResponse
├── mapper/          entity ↔ DTO mappers (plain Java, no extra dependency)
└── util/            GradeUtil, AttendanceUtil, PageResponse
```

**Cross-cutting decisions:**
- Constructor injection everywhere; no field injection.
- Method security via `@PreAuthorize` on controllers for coarse role checks, plus
  explicit ownership checks in services for fine-grained rules (FR-19, FR-20, FR-25).
- `GlobalExceptionHandler` maps: `ResourceNotFoundException`→404,
  `DuplicateResourceException`→409, `MethodArgumentNotValidException`→400 (with a
  field→message map), `BusinessRuleException`→400, `AccessDeniedException`→403,
  `AuthenticationException`→401, anything else→500 with a generic message (never a
  stack trace to the client).
- Profiles: `application.yml` (shared) + `application-dev.yml`
  (`ddl-auto: update`, SQL logging) + `application-prod.yml` (`ddl-auto: validate`).
  Credentials come from environment variables, never committed.

---

## 8. Frontend Architecture

```
src/
├── components/   common/ (Table, Pagination, SearchBar, Modal, Spinner,
│                 EmptyState, Alert, ConfirmDialog, StatCard, SimpleBarChart)
│                 + feature components
├── pages/        auth/  admin/  teacher/  student/  shared/
├── services/     api.js (Axios instance + JWT interceptor),
│                 authService, studentService, departmentService,
│                 teacherService, courseService, enrollmentService,
│                 attendanceService, assessmentService, markService,
│                 transcriptService, reportService, dashboardService
├── context/      AuthContext.jsx  (token, user, roles, login, logout)
├── hooks/        useAuth, useFetch, usePagination, useDebounce
├── layouts/      MainLayout (sidebar + navbar), AuthLayout
├── routes/       AppRoutes.jsx, ProtectedRoute.jsx, RoleRoute.jsx
├── utils/        formatters, validators, constants (roles, grade colors)
├── App.jsx
└── main.jsx
```

- JWT stored in `localStorage`; a request interceptor attaches
  `Authorization: Bearer <token>`; a response interceptor catches 401 → clear session →
  redirect to `/login`, and 403 → "not authorized" message.
- `AuthContext` holds the decoded user + roles and is the single source of truth for
  what the navigation renders.
- `ProtectedRoute` gates on authentication; `RoleRoute` gates on allowed roles. Both are
  **UX**, not security — the backend re-checks every request. (Expected viva question.)
- Charts: lightweight CSS/SVG bar and distribution components written by us. No charting
  library is required by the spec; we keep the dependency list to Axios + React Router
  + Vite (+ Tailwind if we choose it in Phase 8).

---

## 9. API Plan

Conventions: base `/api`, plural nouns, `ResponseEntity` everywhere,
200 read / 201 create (+ `Location`) / 204 delete / 400 validation / 401 unauthenticated /
403 wrong role or not owner / 404 missing / 409 duplicate / 500 unexpected.
List endpoints accept `?page=&size=&sort=&search=` and return a paged envelope.

**Auth & Users (M1)**
```
POST   /api/auth/register            public
POST   /api/auth/login               public → { token, type, username, roles }
GET    /api/users/me                 authenticated
GET    /api/users                    ADMIN
GET    /api/users/{id}               ADMIN
PUT    /api/users/{id}               ADMIN
DELETE /api/users/{id}               ADMIN
```

**Core CRUD (M2)** — each of the five resources gets the standard five verbs:
```
GET/POST       /api/departments      GET/PUT/DELETE /api/departments/{id}
GET/POST       /api/students         GET/PUT/DELETE /api/students/{id}
GET/POST       /api/teachers         GET/PUT/DELETE /api/teachers/{id}
GET/POST       /api/courses          GET/PUT/DELETE /api/courses/{id}
GET/POST       /api/enrollments      GET/PUT/DELETE /api/enrollments/{id}
```
Writes: ADMIN. Reads: ADMIN + TEACHER (+ STUDENT for own records).
Extra filtered reads:
```
GET /api/students?departmentId=&admissionYear=&search=
GET /api/courses?departmentId=&semester=&teacherId=
GET /api/courses/my                       TEACHER — assigned courses
GET /api/enrollments/course/{courseId}    ADMIN, owning TEACHER
GET /api/enrollments/student/{studentId}  ADMIN, owner STUDENT
```

**Attendance (M3)**
```
POST   /api/attendance                      TEACHER(owner)/ADMIN  — single record
POST   /api/attendance/bulk                 TEACHER(owner)/ADMIN  — whole class, one date
PUT    /api/attendance/{id}                 TEACHER(owner)/ADMIN
GET    /api/attendance/course/{courseId}?date=
GET    /api/attendance/student/{studentId}  ADMIN / owner STUDENT
GET    /api/attendance/percentage?studentId=&courseId=
GET    /api/attendance/summary/course/{courseId}
GET    /api/attendance/low?threshold=75     ADMIN/TEACHER
```

**Assessments & Marks (M3)**
```
GET/POST       /api/assessments?courseId=   TEACHER(owner)/ADMIN
PUT/DELETE     /api/assessments/{id}
POST   /api/marks                           TEACHER(owner)/ADMIN
PUT    /api/marks/{id}                      TEACHER(owner)/ADMIN
GET    /api/marks/enrollment/{enrollmentId}
GET    /api/marks/course/{courseId}
GET    /api/grades/student/{studentId}      ADMIN / owner STUDENT
GET    /api/grades/gpa/{studentId}?semester=
```

**Transcript, Reports, Dashboards (M4)**
```
GET /api/transcripts/student/{studentId}          ADMIN / owner STUDENT
GET /api/reports/students-by-department
GET /api/reports/student-performance/{studentId}
GET /api/reports/attendance/course/{courseId}
GET /api/reports/low-attendance?threshold=
GET /api/reports/course-performance/{courseId}
GET /api/reports/grade-distribution?courseId=
GET /api/reports/pass-fail?courseId=
GET /api/reports/department-performance/{departmentId}
GET /api/dashboard/admin        ADMIN
GET /api/dashboard/teacher      TEACHER
GET /api/dashboard/student      STUDENT
```

---

## 10. Group-of-4 Responsibility Matrix

| | **M1 — Auth & Security** | **M2 — Students, Depts & Courses** | **M3 — Attendance, Assessments & Marks** | **M4 — Dashboards, Transcripts & Reports** |
|---|---|---|---|---|
| **Branch** | `feature/auth-security` | `feature/student-course` | `feature/attendance-marks` | `feature/dashboard-reports` |
| **Tables** | users, roles, user_roles | departments, students, teachers, courses, enrollments | attendance, assessments, marks | reporting queries + indexes |
| **Backend** | SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService, AuthController, UserController, auth exceptions, GlobalExceptionHandler | 5 entities, 5 repos, 5 services, 5 controllers, DTOs, mappers, search/filter/pagination | Attendance/Assessment/Mark entities + repos + services + controllers, attendance %, grade calc, teacher-ownership authorization, business rules | TranscriptService, GpaService, ReportService, DashboardService + their controllers and aggregate repository queries |
| **Frontend** | Login, Register, AuthContext, ProtectedRoute, RoleRoute, role navigation, Profile, User Management | Department/Student/Teacher/Course/Enrollment list+form+detail pages, SearchBar, filters, Pagination | Attendance marking screen, attendance % views, assessment setup, marks entry grid, teacher course management | Admin/Teacher/Student dashboards, Transcript page, Reports pages, StatCard + chart components |
| **Docs** | Security architecture, auth API reference | ERD + CRUD/API reference | Attendance workflow, grading logic, business rules | Dashboard/report/transcript workflow, final integration notes |

**Shared by all four:** Git & GitHub, code reviews, integration, testing, debugging,
database verification, README, the 3–5 page report, ERD, slides, and viva. Each member
must be able to defend their own code independently.

**Dependency order between members:** M1 must land Phase 3 before M3/M4 can enforce
ownership rules; M2 must land Phase 4 before M3 has enrollments to attach attendance to;
M3 must land Phases 5–6 before M4 has marks to aggregate. Frontend work (Phases 8–10) is
parallel once the APIs exist. This is exactly why the phase order is what it is.

---

## 11. 25-Day Roadmap

| Days | Work | Phase gate |
|---|---|---|
| 1–2 | Requirements, scope, team assignment, GitHub repo, `main`/`develop`, branch protection | Phase 0 |
| 3–4 | ERD, entity design, constraints, package structure, API plan | Phase 1 |
| 5–7 | Spring Boot + Maven + PostgreSQL + JPA + validation + exception handling; app starts, schema created | Phase 2 |
| 8–10 | Register, login, BCrypt, JWT, roles, filters, protected endpoints | Phase 3 |
| 11–14 | Department/Student/Teacher/Course/Enrollment CRUD + search/filter/pagination | Phase 4 |
| 15–17 | Attendance (15–16), Assessments/Marks/Grades (16–17) | Phases 5–6 |
| 18–19 | Transcripts, GPA, reports, dashboard APIs | Phase 7 |
| 20 | Presentation preparation (slides drafted while features are fresh) | — |
| 21–22 | React foundation + all frontend modules + dashboards wired to real APIs; validation, responsive UI | Phases 8–10 |
| 23–24 | Full integration testing, security testing, bug fixing, DB verification | Phases 11–12 |
| 25 | README, 3–5 page report, ERD export, Git cleanup, demo rehearsal | Phases 13–14 |

Days 21–22 are the tightest window in the plan. Mitigation: each member builds their own
frontend pages incrementally from Day 11 onward against their already-finished APIs
rather than waiting for Day 21, per the spec's "integrate continuously" instruction.

---

## 12. Risks

| # | Risk | Impact | Likelihood | Mitigation |
|---|---|---|---|---|
| R1 | Frontend compressed into 2 days | High | High | Start each module's UI as soon as its API passes Phase gate; Phase 8 skeleton can be built during Phase 4 |
| R2 | JWT/Security misconfiguration blocking everyone | High | Medium | Phase 3 is a hard gate; M1 delivers a Postman collection proving 200/401/403 before Phase 4 opens |
| R3 | Jackson infinite recursion / LazyInitializationException | Medium | High | DTO-only responses, LAZY associations, mapping inside the transaction — decided now, not after it breaks |
| R4 | Duplicate enrollment/attendance/marks slipping through | Medium | Medium | DB UNIQUE constraints + service pre-check returning 409 |
| R5 | Merge conflicts in `SecurityConfig`, `application.yml`, `AppRoutes.jsx` | Medium | High | Those files are owned by M1; others request changes rather than editing; pull `develop` before every push |
| R6 | Slow/incorrect report queries written as Java loops | Medium | Medium | Aggregates in JPQL/native queries with projections; indexes defined in Phase 1 |
| R7 | Uneven contribution / a member unable to defend their code | High | Medium | Per-member branches, per-member commits, code review by a second member, individual contribution log kept from Day 1 |
| R8 | GPA/grade logic duplicated in two modules and diverging | Medium | Medium | Single `GradeUtil`; M4 consumes M3's service, never re-implements |
| R9 | PostgreSQL setup differs per machine | Low | High | `application-dev.yml` + env vars + README setup section + seed data script |
| R10 | Empty dashboards at demo time | Medium | Medium | `DataSeeder` (dev profile only) creating realistic demo data — real rows in PostgreSQL, not fake numbers |
| R11 | Scope creep beyond the specification | Medium | Medium | Section 13 checked at every phase gate; anything not in the spec is rejected |
| R12 | Last-day documentation crunch | Medium | High | README and report written incrementally at each phase gate |

---

## 13. Testing Strategy

**Per phase, before any approval request:**

1. **Build** — `mvn clean package` and `npm run build` must succeed with no errors.
2. **Startup** — application starts, Hibernate creates/validates the schema, no stack traces.
3. **Database verification** — `psql` inspection of tables, constraints, indexes and
   actual rows after each operation. A feature is not done until the row is confirmed in
   PostgreSQL.
4. **API testing** — Postman/curl collection per module. Every endpoint tested for:
   happy path, validation failure (400), unauthenticated (401), wrong role (403),
   not-owner (403), missing id (404), duplicate (409).
5. **Authorization matrix testing** — the critical set:
   - Student attempts `POST /api/marks` → **403**
   - Student attempts `PUT /api/attendance/{id}` → **403**
   - Teacher A attempts attendance on Teacher B's course → **403**
   - Student A requests Student B's transcript → **403**
   - Any endpoint with no token → **401**; with a tampered/expired token → **401**
6. **Business-rule testing** — attendance % against a hand-computed figure; marks above
   `maxMarks` rejected; grade boundary values (49/50, 89/90); GPA against a manual
   credit-weighted calculation; duplicate enrollment rejected.
7. **Frontend testing** — manual per page: loading, empty, error and success states;
   client validation; protected route redirects; role-based menu contents; responsive at
   1440 / 768 / 375 px; browser console clean.
8. **Integration testing (Phase 11)** — full end-to-end walkthrough per role.
9. **Regression** — after every merge into `develop`, the smoke suite (login for all
   three roles + one read per module) is re-run.

Unit tests with JUnit/Mockito for `GradeUtil`, GPA calculation and attendance percentage
— the pure-logic pieces where a test is cheap and the viva will ask about correctness.

**We report actual results, including failures.** No phase is declared passing without
evidence.

---

## 14. Teacher Viva Questions — prepared answer outline

**Architecture** — Layered architecture separates concerns so each layer changes
independently and is testable in isolation; Controller handles HTTP only, Service holds
business rules and transaction boundaries, Repository handles persistence. DTOs decouple
the API contract from the database schema, prevent over-exposing fields such as the
password hash, and avoid Jackson lazy-loading problems. React talks to Spring Boot over
stateless JSON REST calls made by Axios, carrying a JWT in the `Authorization` header.

**Spring Boot** — DI means the framework supplies dependencies rather than classes
constructing them, which decouples code and enables testing with mocks. `@Service` marks
business components, `@Repository` marks data-access components and translates
persistence exceptions, Spring Data JPA generates implementations, Hibernate is the JPA
provider doing the ORM. Constructor injection is preferred because dependencies become
final and mandatory, the object is never in a half-built state, and the class is
testable without a Spring container.

**Security** — Authentication answers "who are you", authorization answers "what may you
do". A JWT is a signed header.payload.signature token; on login we validate credentials
against the BCrypt hash and sign a token containing the username, roles and expiry. Every
subsequent request passes through `JwtAuthenticationFilter`, which validates the
signature and expiry and populates the `SecurityContext`. Endpoints are protected by
`SecurityFilterChain` rules plus `@PreAuthorize`. Students cannot modify marks because
`ROLE_STUDENT` is not permitted on mark-write endpoints and the service additionally
verifies the caller is the owning teacher — a rule enforced server-side, so hiding the
button in React is convenience, not security.

**Database** — ERD walkthrough; Student↔Course is many-to-many resolved through
`Enrollment`, which is a real entity because it carries semester, academic year and
status and owns attendance and marks. Duplicates are prevented by a composite UNIQUE
constraint plus a service-level check returning 409. PostgreSQL: mature open-source
RDBMS with strong constraint, transaction and aggregate-query support, which this
reporting-heavy domain needs.

**Attendance & Marks** — Attendance % = present (and late, per our documented rule)
sessions ÷ total recorded sessions × 100 for that enrollment. Only the assigned teacher
or an admin may modify it; ownership is checked in the service by comparing the course's
teacher's user id with the authenticated principal. Grades come from weighted marks →
percentage → letter → grade points; GPA is credit-weighted. All of it lives in the
service layer plus `GradeUtil`, never in the controller and never in React.

**React** — Axios gives us a configured instance with interceptors and clean
promise-based error handling. The request interceptor attaches the JWT to every call;
the response interceptor logs out on 401. Context API shares auth state without prop
drilling. `ProtectedRoute` wraps routes and redirects unauthenticated users;
`RoleRoute` additionally checks roles. Navigation is rendered from the roles in
`AuthContext`.

**Testing** — as Section 13, with the Postman collection and the authorization matrix as
the concrete evidence to show.

**Individual contribution** — each member presents their features, files, APIs, pages,
tables, hardest problem and solution, using their own commits as evidence.

---

## 15. Final Submission Checklist

- [ ] Backend source code (Spring Boot, layered, compiles)
- [ ] Frontend source code (React + Vite, builds)
- [ ] Git repository with `main`, `develop`, four feature branches, meaningful history from all 4 members
- [ ] PostgreSQL schema + ERD diagram
- [ ] README with setup and run instructions
- [ ] 3–5 page project report
- [ ] Individual contributions document
- [ ] GenAI acknowledgement
- [ ] Presentation slides
- [ ] Authentication working (register, login, JWT)
- [ ] Authorization working (all three roles, ownership rules)
- [ ] CRUD for all core entities
- [ ] Search / filtering / pagination
- [ ] Role-based dashboards
- [ ] Attendance module
- [ ] Marks & grades module
- [ ] Transcripts
- [ ] Reports
- [ ] Validation on all inputs
- [ ] Centralized exception handling with correct status codes
- [ ] Responsive UI (desktop / tablet / mobile)
- [ ] No broken APIs
- [ ] No console errors
- [ ] No fake dashboard data — every figure traced to a PostgreSQL query
- [ ] All four members ready for viva

---

## Specification Conformance Statement

| Spec section | Conformance |
|---|---|
| 1 Technology | Java 17, Spring Boot 3.x, Maven, Web, Data JPA/Hibernate, Security, JWT, PostgreSQL 14+, Bean Validation, Lombok(optional); React 18, Vite, Axios, React Router v6, CSS/Tailwind; Git+GitHub. **No additions.** |
| 2 Features | All 12 modules and every listed feature mapped in §4 and §9 |
| 3 Database | 11 entities, normalized, PKs/FKs/UNIQUE/nullability/indexes/duplicate prevention defined |
| 4 Backend structure | Exact package list from the spec adopted verbatim |
| 5 Frontend structure | Exact folder list from the spec adopted verbatim |
| 6 Group of 4 | Responsibility matrix in §10 matches the spec's member definitions |
| 7 Git workflow | main/develop + the four named feature branches |
| 8 Roadmap | 25-day plan in §11 follows the spec's day allocations |
| 9 Process | ANALYZE→PLAN→IMPLEMENT→BUILD→TEST→VERIFY→REPORT→APPROVE per phase |
| 10 Phase order | Phases 0–14 respected; no phase skipped |
| 11 Viva | §14 |
| 12 Submission | §15 |
| 13 Scope control | No microservices, Redis, Docker, WebSockets, AI, payments, mobile, cloud |

**Open item requiring your input:** the official specification document itself was not
present in the project folder. This analysis is built from the restatement in the task
brief. If the original PDF/DOCX exists, add it to the repository and I will reconcile
this document against it before Phase 1 begins.

---

## PHASE 0 COMPLETE — STOPPING FOR APPROVAL

No code has been written. No files other than this analysis have been created.

**Decisions I need from you before Phase 1:**
1. Approve the 11-entity model, or request changes.
2. Confirm the grade scale and the 75% attendance threshold, or supply your institution's.
3. Confirm LATE counts as present for the attendance percentage (our default), or specify otherwise.
4. Confirm Tailwind CSS vs plain CSS for the frontend.
5. Confirm the base Java package `com.group6.sams`.

On approval, Phase 1 delivers: the ERD, the full DDL-level table design with every
constraint and index, the JPA entity relationship map, the finalized package structure,
and the complete endpoint-by-endpoint API contract.
