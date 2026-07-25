# Project Report

## Student Academic Records & Attendance Management System

**Group 6** — Full-Stack Project (Spring Boot + ReactJS), 2025–2026

---

### 1. Introduction

This project is a web-based system for managing a college's academic records:
students, courses, attendance, marks, transcripts and reports. It replaces manual
registers and spreadsheets with a single source of truth backed by a relational
database, and gives three kinds of user — administrators, teachers and students —
a role-appropriate view of that data.

The system is built as a **Spring Boot REST API** consumed by a **ReactJS
single-page application**, with **PostgreSQL** for storage. It follows a strict
layered architecture and stateless JWT security.

---

### 2. Objectives

- Provide secure, role-based access for administrators, teachers and students.
- Manage the full academic lifecycle: departments, students, teachers, courses,
  enrollments, attendance, assessments, marks, grades, GPA and transcripts.
- Produce accurate reports and dashboards from live data — never mocked figures.
- Enforce academic integrity rules (no duplicate enrollment, no marks above the
  maximum, teachers restricted to their own courses, students unable to edit marks).

---

### 3. System architecture

```
ReactJS (Vite) ── HTTP/JSON ──► Spring Boot Controller ──► Service ──► Repository ──► PostgreSQL
```

**Why layered?** Each layer has one responsibility and can be changed or tested in
isolation. Controllers deal only with HTTP; services hold business logic and
transaction boundaries; repositories handle persistence. **DTOs** cross every
boundary so entities are never exposed — this hides the password hash and avoids
Jackson lazy-loading errors.

**Security.** Authentication is stateless: `POST /api/auth/login` validates
credentials against a BCrypt hash and returns a signed JWT. A servlet filter
validates the token on every request and populates the security context.
Authorization has two layers: `@PreAuthorize` for coarse role rules, and explicit
ownership checks in services for fine-grained rules ("is this the teacher assigned
to this course?", "is this the student's own record?").

---

### 4. Database design

The schema has **11 tables** in third normal form. The central design choice is
that **Enrollment** is a first-class entity resolving the many-to-many between
students and courses, because the relationship carries data (semester, academic
year, status) and owns the attendance and mark records. Attendance and marks hang
off the enrollment, which makes it impossible to record either for a student not
enrolled in the course.

**Transcripts and grades are never stored** — they are derived from marks on demand,
so a corrected mark immediately yields a corrected grade and GPA with no
reconciliation. Three integrity rules that a relational constraint cannot express
(a mark not exceeding its assessment maximum, assessment weights summing to at most
100%, and a mark's enrollment and assessment sharing a course) are enforced in the
service layer.

See [ERD.md](ERD.md) for the full diagram, constraints and indexes.

---

### 5. Key features and business logic

**Attendance.** Teachers record attendance for a whole class in one bulk,
transactional request. The percentage is `(present + late) / total × 100`; an
enrollment with no records has *no* percentage (not 0%), so new enrollments never
appear in the low-attendance alerts. A teacher can only touch attendance for courses
assigned to them.

**Marks and grades.** Each course has weighted assessments (quiz, midterm, final…).
A course percentage is the weighted sum of `(obtained / max) × weight` over the
recorded weight — so a partially-graded student is not unfairly penalised. Letter
grades and grade points come from a single `GradeUtil` used by both the marks module
and the transcript/reports, so they can never disagree.

**GPA** is credit-weighted: `Σ(gradePoints × credits) / Σ(credits)`. A heavier course
moves the GPA more than a lighter one; an ungraded course contributes neither points
nor credits, so it shows "in progress" rather than dragging the average down.

**Reports and dashboards.** Eight reports (students by department, student and course
performance, attendance, grade distribution, pass/fail, department performance) and
three role dashboards are all computed from live queries. Simple counts are
aggregated in SQL; grade bucketing is done by `GradeUtil` so the boundaries stay in
one place.

---

### 6. Security measures

- BCrypt password hashing (salted, adaptive).
- Stateless JWT signed with HMAC-SHA256; tampered or expired tokens rejected (401).
- Default-deny authorization — an unmapped endpoint fails closed.
- Self-registration as ADMIN is blocked (prevents privilege escalation).
- Students appear on no mark- or attendance-write endpoint.
- SQL injection prevented by JPA parameter binding (verified).
- No stack traces or password hashes ever leave the API.

---

### 7. Testing

- **23 unit tests** for the pure logic — JWT (tamper, forgery, expiry), attendance
  percentage (boundaries, null handling), grading and GPA (every grade boundary,
  credit weighting).
- **52 integration checks** across all three roles — authentication, the full
  authorization matrix, CRUD round-trips, search/filter/pagination, attendance,
  marks, grades, transcript, all eight reports, all three dashboards, and validation.
- **Security and QA sweep** — password-leak, stack-trace, SQL-injection, IDOR and
  HTTP-method checks; database integrity (zero orphaned or inconsistent rows).
- Key figures were cross-checked against independent SQL aggregates (attendance
  90.00%, GPA 4.00, pass/fail 6/2).

Results are recorded in `tests/PHASE11_INTEGRATION_RESULTS.md` and
`tests/PHASE12_QA_SECURITY.md`.

---

### 8. Challenges and solutions

| Challenge | Solution |
|---|---|
| Java 26 default not supported by Spring Boot 3.x | Used Java 21 LTS via `JAVA_HOME` |
| Hibernate does not emit `ON DELETE` clauses | Implemented CASCADE/RESTRICT/SET NULL in services |
| `lower(bytea)` error on null search parameter | Explicit `CAST(:search AS string)` in queries |
| Wrong HTTP verb returned 500 | Added handlers for method-not-supported (405) and no-resource (404) |
| Empty dashboards at demo time | Idempotent `DataSeeder` with realistic demo data |
| Keeping grade logic consistent across modules | Single `GradeUtil` consumed everywhere |

---

### 9. Conclusion

The system meets the specification end-to-end: secure role-based authentication and
authorization, full CRUD with search and pagination, attendance with percentages and
alerts, marks with validated grading and credit-weighted GPA, on-demand transcripts,
eight reports, and three live dashboards — all backed by a normalized PostgreSQL
schema and verified by unit, integration and security tests. The layered
architecture and DTO boundaries keep the codebase explainable and maintainable, and
every member can defend their part of it.

---

### 10. Future enhancements

- Refresh tokens and token revocation.
- CSV/PDF export of transcripts and reports.
- Pagination on the very largest report queries.
- An audit log of administrative changes.
