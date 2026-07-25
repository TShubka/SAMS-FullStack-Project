# API Reference

**Group 6 — Student Academic Records & Attendance Management System**

Base URL: `http://localhost:8080/api`
All endpoints except `POST /auth/register` and `POST /auth/login` require
`Authorization: Bearer <jwt>`.

List endpoints accept `?page=&size=&sort=&search=` and return a paged envelope.
Errors use a uniform shape:

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request",
  "message": "Validation failed", "path": "/api/students",
  "fieldErrors": { "rollNumber": "must not be blank" } }
```

Status codes: 200 OK · 201 Created · 204 No Content · 400 Bad Request ·
401 Unauthorized · 403 Forbidden · 404 Not Found · 405 Method Not Allowed ·
409 Conflict · 500 Server Error.

---

## Authentication & Users

| Method | Path | Roles | Success |
|---|---|---|---|
| POST | `/auth/register` | public | 201 |
| POST | `/auth/login` | public | 200 (JWT) |
| GET | `/auth/me` | any | 200 |
| GET | `/users` | ADMIN | 200 |
| GET | `/users/{id}` | ADMIN | 200 |
| PUT | `/users/{id}` | ADMIN | 200 |
| DELETE | `/users/{id}` | ADMIN | 204 |

## Departments / Students / Teachers / Courses / Enrollments

Standard CRUD (writes are ADMIN; reads ADMIN/TEACHER, plus STUDENT for own records):

| Method | Path | Roles |
|---|---|---|
| GET | `/departments` `/students` `/teachers` `/courses` `/enrollments` | ADMIN, TEACHER |
| GET | `/{resource}/{id}` | ADMIN, TEACHER |
| POST | `/{resource}` | ADMIN |
| PUT | `/{resource}/{id}` | ADMIN |
| DELETE | `/{resource}/{id}` | ADMIN |

Filtered reads:

| Method | Path | Roles |
|---|---|---|
| GET | `/students?departmentId=&admissionYear=&search=` | ADMIN, TEACHER |
| GET | `/students/me` | STUDENT |
| GET | `/courses?departmentId=&semester=&teacherId=&search=` | ADMIN, TEACHER, STUDENT |
| GET | `/courses/my` | TEACHER |
| GET | `/enrollments/my` | STUDENT |
| GET | `/enrollments/student/{id}` | ADMIN, TEACHER |
| GET | `/enrollments/course/{id}` | ADMIN, TEACHER |

## Attendance

| Method | Path | Roles |
|---|---|---|
| POST | `/attendance` | TEACHER(owner), ADMIN |
| POST | `/attendance/bulk` | TEACHER(owner), ADMIN |
| PUT | `/attendance/{id}` | TEACHER(owner), ADMIN |
| DELETE | `/attendance/{id}` | TEACHER(owner), ADMIN |
| GET | `/attendance/course/{courseId}?date=` | ADMIN, TEACHER(owner) |
| GET | `/attendance/student/{studentId}` | ADMIN, TEACHER, STUDENT(own) |
| GET | `/attendance/percentage?studentId=&courseId=` | ADMIN, TEACHER, STUDENT(own) |
| GET | `/attendance/summary/course/{courseId}` | ADMIN, TEACHER(owner) |
| GET | `/attendance/low?threshold=` | ADMIN, TEACHER |

## Assessments, Marks & Grades

| Method | Path | Roles |
|---|---|---|
| GET | `/assessments?courseId=` | ADMIN, TEACHER, STUDENT |
| POST/PUT/DELETE | `/assessments[/{id}]` | TEACHER(owner), ADMIN |
| POST | `/marks` | TEACHER(owner), ADMIN |
| PUT/DELETE | `/marks/{id}` | TEACHER(owner), ADMIN |
| GET | `/marks/enrollment/{id}` | ADMIN, TEACHER, STUDENT(own) |
| GET | `/marks/course/{courseId}` | ADMIN, TEACHER(owner) |
| GET | `/grades/student/{studentId}` | ADMIN, TEACHER, STUDENT(own) |
| GET | `/grades/my` | STUDENT |
| GET | `/grades/enrollment/{id}` | ADMIN, TEACHER, STUDENT(own) |
| GET | `/grades/gpa/{studentId}?semester=&academicYear=` | ADMIN, TEACHER, STUDENT(own) |
| GET | `/grades/gpa/my` | STUDENT |

## Transcript, Reports, Dashboards

| Method | Path | Roles |
|---|---|---|
| GET | `/transcripts/student/{studentId}` | ADMIN, TEACHER, STUDENT(own) |
| GET | `/transcripts/my` | STUDENT |
| GET | `/reports/students-by-department` | ADMIN |
| GET | `/reports/student-performance/{studentId}` | ADMIN, TEACHER, STUDENT(own) |
| GET | `/reports/attendance/course/{courseId}` | ADMIN, TEACHER(owner) |
| GET | `/reports/low-attendance?threshold=` | ADMIN, TEACHER |
| GET | `/reports/course-performance/{courseId}` | ADMIN, TEACHER(owner) |
| GET | `/reports/grade-distribution?courseId=` | ADMIN, TEACHER(owner) |
| GET | `/reports/pass-fail?courseId=` | ADMIN, TEACHER(owner) |
| GET | `/reports/department-performance/{departmentId}` | ADMIN |
| GET | `/dashboard/admin` | ADMIN |
| GET | `/dashboard/teacher` | TEACHER |
| GET | `/dashboard/student` | STUDENT |

---

## Example: login

```http
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "Password123" }
```

```json
{ "token": "eyJ...", "type": "Bearer", "userId": 1,
  "username": "admin", "email": "admin@sams.edu",
  "roles": ["ROLE_ADMIN", "ROLE_USER"], "expiresInMs": 86400000 }
```
