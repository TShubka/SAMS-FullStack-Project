# Individual Contributions

**Group 8 — Student Academic Records & Attendance Management System**

The work was divided into four member areas, following the specification. Each
member owns their backend, frontend, database tables and documentation for that
area, and must be able to explain and defend their own code.

| Member | Name | Area |
|---|---|---|
| Member 1 | Shuaib Osman Daud | Authentication & Security |
| Member 2 | Dahir Mohamed Shaie | Students, Departments & Courses |
| Member 3 | Aisha Hassan Hersi | Attendance, Assessments & Marks |
| Member 4 | Idiris Abdi Mohamed | Dashboards, Transcripts & Reports |

---

## Member 1 — Shuaib Osman Daud — Authentication & Security

**Backend:** `security/` package (JwtTokenProvider, JwtAuthenticationFilter,
JwtAuthEntryPoint, JwtAccessDeniedHandler, CustomUserDetailsService, UserPrincipal,
SecurityConfig, OwnershipService), `AuthController`, `AuthService`, User & Role
entities, `GlobalExceptionHandler` and the exception package.

**Frontend:** Login and Register pages, `AuthContext`, `ProtectedRoute`,
`RoleRoute`, role-based navigation in `MainLayout`, `Profile` page, Axios instance
with the JWT interceptor.

**Database:** `users`, `roles`, `user_roles`.

**Key APIs:** `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`.

**Hardest problem:** ensuring `/api/auth/me` and other protected endpoints returned
401/403 (not 500) for anonymous or tampered requests — solved by restricting the
public matcher to register/login only and separating the 401 entry point from the
403 access-denied handler.

---

## Member 2 — Dahir Mohamed Shaie — Students, Departments & Courses

**Backend:** Department, Student, Teacher, Course, Enrollment entities, repositories,
services and controllers; DTOs and the `AcademicMapper`; search, filtering and
pagination; the service-layer referential rules (RESTRICT/CASCADE/SET NULL).

**Frontend:** Department, Student, Teacher, Course and Enrollment CRUD pages with
search bars, filters, pagination, modals and delete confirmation; the shared
`DataTable`, `Pagination`, `SearchBar`, `Modal`, `ConfirmDialog` components.

**Database:** `departments`, `students`, `teachers`, `courses`, `enrollments`.

**Key APIs:** the five REST resources with the standard verbs, plus
`/courses/my`, `/students/me`, `/enrollments/student/{id}`.

**Hardest problem:** implementing delete semantics without database `ON DELETE`
support — deleting a student cascades to their enrollments, attendance and marks,
while deleting a department or course with dependents is refused with a 409.

---

## Member 3 — Aisha Hassan Hersi — Attendance, Assessments & Marks

**Backend:** Attendance, Assessment, Mark entities, repositories, services and
controllers; `AttendanceUtil` and `GradeUtil`; attendance percentage, weighted
grading, GPA; the teacher-ownership and student-cannot-edit-marks rules; the three
integrity rules the schema cannot express.

**Frontend:** teacher attendance-marking screen (bulk, per-date), marks-entry grid
with inline assessment creation, attendance percentage and grade views; student
My Attendance and My Grades pages.

**Database:** `attendance`, `assessments`, `marks`.

**Key APIs:** `/attendance`, `/attendance/bulk`, `/attendance/percentage`,
`/attendance/low`, `/assessments`, `/marks`, `/grades/*`.

**Hardest problem:** getting the weighted-percentage and credit-weighted-GPA maths
right at the boundaries (49 vs 50, ungraded vs zero) — covered by unit tests and
cross-checked against independent SQL aggregates.

---

## Member 4 — Idiris Abdi Mohamed — Dashboards, Transcripts & Reports

**Backend:** TranscriptService, GradeService (GPA), ReportService (eight reports),
DashboardService and their controllers; reporting projection queries; `DataSeeder`.

**Frontend:** Admin, Teacher and Student dashboards; Transcript page; Reports
console; `SimpleBarChart` and `StatCard` components.

**Database:** reporting/aggregate queries and index tuning.

**Key APIs:** `/transcripts/*`, `/reports/*` (eight reports),
`/dashboard/{admin,teacher,student}`.

**Hardest problem:** keeping every dashboard and report figure consistent with the
marks page — solved by reusing `GradeService`/`GradeUtil` everywhere instead of
re-deriving grades, and aggregating simple counts in SQL.

---

## Shared by all members

Git & GitHub workflow, code reviews, integration, testing, debugging, database
verification, the README, this report, the ERD, the presentation and the viva.
