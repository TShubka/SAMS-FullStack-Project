# Member Study Guide — Easy Explanations with Code

**Group 8 — SAMS**

This guide explains each member's part in simple language with the key code, so you
can defend your work in the viva. Read your own section carefully; skim the others.

---

## Member 1 — Shuaib Osman Daud — Authentication & Security

### What your part does (in one sentence)
"I make sure only the right people can log in, and that each person can only do what
their role allows."

### The 3 things to explain

**1. Login gives you a token (JWT).**
When you log in, the server checks your password and gives you a "ticket" (a token).
You show this ticket on every future request instead of your password.

```java
// AuthServiceImpl.java — login
Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password)); // checks BCrypt password
String token = tokenProvider.generateToken(principal);                // makes the ticket
return JwtResponse.builder().token(token).roles(roles).build();
```

**2. Every request is checked by a filter.**
Before any request reaches the controller, a filter reads the token and confirms it
is real (correct signature, not expired).

```java
// JwtAuthenticationFilter.java
String token = extractToken(request);                    // read "Authorization: Bearer ..."
if (tokenProvider.validateToken(token)) {                // is the ticket valid?
    Long userId = tokenProvider.getUserIdFromToken(token);
    // put the user into the security context so controllers know who they are
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

**3. Passwords are never stored in plain text.**
We hash them with BCrypt (a one-way scrambler). Even we can't read them back.

```java
.password(passwordEncoder.encode(request.getPassword()))  // "Password123" -> "$2a$10$...."
```

### Likely questions
- *"What is a JWT?"* → A signed ticket with three parts (header.payload.signature). The
  signature makes it tamper-proof — if someone edits it, validation fails.
- *"Why can't a student change marks?"* → Two reasons: the mark endpoints don't allow
  the STUDENT role, and even a teacher is checked to be the course's own teacher.
- *"401 vs 403?"* → 401 = you didn't prove who you are (no/bad token). 403 = we know
  who you are, but you're not allowed.

### Your files
`security/` folder, `AuthController`, `AuthService`, `GlobalExceptionHandler`.
Your tables: `users`, `roles`, `user_roles`.

---

## Member 2 — Dahir Mohamed Shaie — Students, Departments & Courses

### What your part does (in one sentence)
"I handle the basic records — adding, viewing, editing and deleting students,
teachers, departments, courses and enrollments."

### The 3 things to explain

**1. CRUD = Create, Read, Update, Delete.**
Each resource has 5 standard actions. Example for a department:

```java
// DepartmentController.java
@PostMapping                  ...  create(...)   // Create -> 201
@GetMapping("/{id}")          ...  findById(...) // Read   -> 200
@PutMapping("/{id}")          ...  update(...)   // Update -> 200
@DeleteMapping("/{id}")       ...  delete(...)   // Delete -> 204
```

**2. Search, filter and pagination.**
Lists don't load everything — they show one page and can be filtered.

```java
// StudentRepository.java — one query handles all filter combinations
@Query("""
   SELECT s FROM Student s
   WHERE (:departmentId IS NULL OR s.department.id = :departmentId)
     AND (:search IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%')))
   """)
Page<Student> findByFilters(Long departmentId, Integer year, String search, Pageable p);
```

**3. Smart delete rules.**
You can't delete a department that still has students (it would break their records).

```java
// DepartmentServiceImpl.java — delete
if (students > 0 || teachers > 0 || courses > 0) {
    throw new ResourceInUseException("Cannot delete department: it still has ...");  // 409
}
```
But deleting a student DOES remove their enrollments, attendance and marks (cascade).

### Likely questions
- *"Why is Enrollment a separate table?"* → A student takes many courses and a course
  has many students (many-to-many). The enrollment also stores the semester and year,
  so it must be its own table, not just a link.
- *"How do you prevent duplicate enrollment?"* → A unique rule on
  (student, course, semester, year) in the database, plus a check that returns 409.
- *"What is pagination?"* → Showing results one page at a time (e.g. 10 per page)
  instead of all at once.

### Your files
Department/Student/Teacher/Course/Enrollment entity, repository, service, controller.
Your tables: `departments`, `students`, `teachers`, `courses`, `enrollments`.

---

## Member 3 — Aisha Hassan Hersi — Attendance, Assessments & Marks

### What your part does (in one sentence)
"I record attendance and marks, calculate attendance percentages and grades, and make
sure only the right teacher can enter them."

### The 3 things to explain

**1. Attendance percentage.**
Present + Late count as attended; the percentage is attended ÷ total.

```java
// AttendanceUtil.java
public static boolean countsAsPresent(AttendanceStatus s) {
    return s == PRESENT || s == LATE;              // late still means they showed up
}
// percentage = attended * 100 / total   (e.g. 18 of 20 = 90%)
```
Important: a student with **no records** has *no* percentage (not 0%), so new students
don't wrongly appear in the low-attendance list.

**2. Marks are validated.**
A mark can't be higher than the assessment's maximum.

```java
// MarkServiceImpl.java
if (request.getMarksObtained().compareTo(assessment.getMaxMarks()) > 0) {
    throw new BusinessRuleException("Marks cannot exceed the maximum");  // 400
}
```

**3. Grade calculation (weighted).**
Each assessment has a weight. Final % = sum of (score/max × weight).

```java
// GradeUtil.java
if (p >= 90) return "A+";   // 4.0 points
if (p >= 80) return "A";    // 3.7
if (p >= 70) return "B";    // 3.3
...
if (p <  50) return "F";    // 0.0
```

### Likely questions
- *"How do you calculate attendance?"* → attended (present+late) ÷ total × 100.
- *"Who can enter marks?"* → Only the teacher assigned to that course, or an admin.
  Students cannot at all.
- *"How do you stop teacher A editing teacher B's course?"* → `OwnershipService`
  compares the course's teacher with the logged-in user; if different → 403.
- *"Why is a mark of 8/10 worth 80%?"* → 8 ÷ 10 × 100 = 80.

### Your files
Attendance/Assessment/Mark entity, service, controller; `AttendanceUtil`, `GradeUtil`.
Your tables: `attendance`, `assessments`, `marks`.

---

## Member 4 — Idiris Abdi Mohamed — Dashboards, Transcripts & Reports

### What your part does (in one sentence)
"I turn the raw data into useful summaries — the transcript, the eight reports, and
the three dashboards — all from real database numbers."

### The 3 things to explain

**1. GPA is credit-weighted.**
A 4-credit course affects your GPA more than a 1-credit course.

```java
// GradeUtil.java
// GPA = sum(gradePoints × credits) / sum(credits)
// Example: A+ (4.0) on 3 credits, F (0.0) on 3 credits
//          = (4.0×3 + 0.0×3) / 6 = 12/6 = 2.00
```

**2. The transcript is built on demand, not stored.**
Every time you open it, it is calculated fresh from the marks — so it is always
correct, even after a grade change.

```java
// TranscriptServiceImpl.java
List<CourseGradeResponse> grades = gradeService.gradesForStudent(studentId, caller);
// group by semester, add up GPA per semester and overall
```

**3. Dashboards show real counts.**
No fake numbers — every figure is a live database query.

```java
// DashboardServiceImpl.java — admin
.totalStudents(studentRepository.count())      // real count
.totalCourses(courseRepository.count())        // real count
.lowAttendanceCount(attendanceService.lowAttendance(null, caller).size());
```

### Likely questions
- *"Why don't you store the transcript?"* → Because if a mark changes, a stored
  transcript would be wrong. Calculating it fresh keeps it always correct.
- *"How is a report different from a dashboard?"* → A report answers one question
  (e.g. grade distribution for a course); a dashboard is the overview for a role.
- *"Where do the dashboard numbers come from?"* → Real `COUNT` queries on the database,
  never hard-coded.

### Your files
Transcript/Report/Dashboard service + controller; `SimpleBarChart`, dashboards.
Your area: reporting queries.

---

## Shared answers (everyone should know)

- **Architecture:** React (screen) → Controller (receives request) → Service (thinks) →
  Repository (talks to database) → PostgreSQL (stores data).
- **Why layers?** Each layer has one job, so it's easy to find and fix things.
- **Why DTOs?** So we never send the password or internal data to the browser.
- **How does React talk to Spring Boot?** Axios sends JSON over HTTP with the token.
- **How did we test?** 23 unit tests + 52 integration tests, all passing.
