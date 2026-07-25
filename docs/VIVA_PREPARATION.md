# Viva Preparation & Demo Guide

**Group 6 — Student Academic Records & Attendance Management System**

This is the defense guide: a demo script, and prepared answers to the questions in
the specification. Every member should be fluent in their own area and comfortable
with the shared architecture answers.

---

## Part A — Demo script (10 minutes)

**Setup before you start:** PostgreSQL running; backend on 8080; frontend on 5173.
Have three browser tabs or use one and log out between roles.

1. **Login & security (1 min).** Log in as `admin` / `Password123`. Point out that
   login returns a JWT; open dev-tools → Application → Local Storage to show the
   token. Try a wrong password to show the 401.

2. **Admin CRUD (2 min).** Departments → create one → show it appears. Students →
   filter by department, search by name, page through. Show a delete being blocked
   (409) when the record is still referenced.

3. **Admin dashboard & reports (2 min).** Dashboard shows live counts and the
   students-by-department chart. Reports → pick CS301 → Grade Distribution and
   Pass/Fail; note the figures are computed, not stored.

4. **Teacher (2 min).** Log out, log in as `t.smith`. Note the menu changed. Open a
   course → record attendance for the class → save. Enter marks for an assessment.
   Try to open a course you don't teach — show it's not available.

5. **Student (2 min).** Log in as `cs.student7`. Show the low-attendance warning,
   My Grades (an F and an in-progress course), and the Transcript with GPA.

6. **Authorization proof (1 min).** As the student, in the URL bar go to
   `/departments` — show the 403 page. Mention the backend also returns 403 on the
   API, so it's real security, not just a hidden menu.

---

## Part B — Prepared answers

### Architecture

**Why layered architecture?** Separation of concerns — each layer has one job and
can be changed or tested independently. Controllers handle HTTP, services hold
business logic and transactions, repositories handle persistence.

**Why Controller / Service / Repository?** It keeps HTTP concerns out of business
logic and business logic out of data access, so the same service can be called from
different controllers and tested without a web server or a database.

**Why DTOs?** They decouple the API contract from the database schema, prevent
over-exposing fields (the password hash never leaves the server), and avoid Jackson
lazy-loading and infinite-recursion errors that happen when entities are serialized.

**How does React communicate with Spring Boot?** Over stateless JSON REST calls made
by Axios; each request carries the JWT in the `Authorization: Bearer` header.

### Spring Boot

**Dependency injection?** The framework supplies a class's dependencies instead of
the class constructing them — this decouples code and makes it testable with mocks.

**`@Service`, `@Repository`, JPA, Hibernate?** `@Service` marks business components;
`@Repository` marks data-access components and translates persistence exceptions;
Spring Data JPA generates repository implementations; Hibernate is the JPA provider
doing the object-relational mapping.

**Why constructor injection?** Dependencies become `final` and mandatory, the object
is never half-built, and the class can be unit-tested without a Spring container.

### Security

**Authentication vs authorization?** Authentication is "who are you" (login →
token); authorization is "what may you do" (roles + ownership checks).

**How does JWT work / how is it generated & validated?** On login we verify the
password against the BCrypt hash and issue a token containing the username, user id,
roles and expiry, signed with HMAC-SHA256. On each request a filter validates the
signature and expiry and populates the security context. The signature makes the
token tamper-evident — changing the payload invalidates it because the attacker
can't recompute the signature without the secret.

**How are endpoints protected?** `SecurityFilterChain` rules plus `@PreAuthorize` on
controllers for roles, and ownership checks in services for fine-grained rules.
Default is deny.

**Why can't students modify marks?** `ROLE_STUDENT` is on no mark-write endpoint, and
the service additionally checks the caller is the owning teacher. It's enforced
server-side, so hiding the button in React is convenience, not the control.

### Database

**Explain the ERD.** (Walk through [ERD.md](ERD.md).) 11 tables; Enrollment resolves
the student–course many-to-many.

**Student–Course relationship? Why Enrollment?** Many-to-many, and the relationship
carries data (semester, year, status) and owns attendance and marks — so it is a
real entity, not a join table.

**How prevent duplicate enrollment?** A composite unique constraint on
(student, course, semester, academic_year), plus a service pre-check returning 409.

**Why PostgreSQL?** A mature open-source RDBMS with strong constraint, transaction
and aggregate-query support, which this reporting-heavy domain needs.

### Attendance & Marks

**How calculate attendance?** (present + late) / total × 100 for the enrollment; an
enrollment with no records has no percentage, not 0%.

**Who can modify attendance / how prevent unauthorized teacher access?** Only the
assigned teacher or an admin. The service compares the course's teacher's user id
with the authenticated principal and returns 403 otherwise.

**How calculate grades / GPA? Where is the logic?** Weighted percentage → letter →
grade points; GPA is credit-weighted. All of it is in `GradeUtil` and the services,
never in the controller or React.

### React

**Why Axios?** A configured instance with interceptors and clean promise-based error
handling.

**How does the JWT interceptor work?** A request interceptor attaches the token to
every call; a response interceptor logs out on 401.

**What is Context API?** A way to share state (here, the auth session) without prop
drilling.

**How are protected routes / role-based navigation implemented?**
`ProtectedRoute` gates on authentication, `RoleRoute` on roles; the menu is rendered
from the roles held in `AuthContext`. Both are UX — the backend re-checks every call.

### Testing

**How did you test APIs / auth / authorization / validation / error handling?**
23 unit tests for pure logic; a 52-check integration suite across all three roles
covering the authorization matrix, CRUD, search, attendance, marks, grades,
transcripts, reports and dashboards; and a security/QA sweep. Key figures were
cross-checked against independent SQL queries. Results are in `tests/`.

---

## Part C — Individual contribution (each member prepares)

Be ready to state, for your area: your features, your files, your APIs, your
frontend pages, your database tables, your hardest problem and how you solved it.
See [CONTRIBUTIONS.md](CONTRIBUTIONS.md).

---

## Part D — Final submission checklist

- [x] Backend source code (layered, compiles, runs)
- [x] Frontend source code (builds, runs)
- [x] Git repository with meaningful history and feature branches
- [x] PostgreSQL schema / ERD
- [x] README with setup and run instructions
- [x] 3–5 page project report
- [x] Individual contributions
- [x] GenAI acknowledgement
- [ ] Presentation slides *(prepare from this guide)*
- [x] Authentication & authorization
- [x] CRUD, search, filtering, pagination
- [x] Dashboards (real data)
- [x] Attendance, marks, grades, transcripts, reports
- [x] Validation & centralized exception handling
- [x] Responsive UI
- [x] No broken APIs, no console errors, no fake dashboard data
- [ ] All members ready for viva *(rehearse Parts A & B)*
