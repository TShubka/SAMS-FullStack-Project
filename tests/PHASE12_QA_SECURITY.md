# PHASE 12 — QA & SECURITY RESULTS

**Group 6 — Student Academic Records & Attendance Management System**

## Security checks

| # | Check | Result |
|---|---|---|
| 1 | Password hash never appears in any response (`/users`, `/auth/me`) | PASS — no `password`, no `$2a$` anywhere |
| 2 | Internal errors hide stack traces | PASS — no `Exception`, `at com.`, or `.java:` in bodies |
| 3 | SQL injection in search (`' OR '1'='1`) | PASS — treated as a literal, 0 rows, 200 not 500 (JPA parameter binding) |
| 4 | Empty / malformed / tampered JWT | PASS — all 401 |
| 5 | Admin token on a student-only route (`/grades/my`) | PASS — 403 |
| 6 | IDOR: student reads `/users/1`, PUTs `/students/1`, DELETEs `/courses/1` | PASS — all 403 |
| 7 | CORS restricted to the configured Vite origin, not `*` | PASS — origins from config |
| 8 | Wrong HTTP verb on a valid path | **FIXED** — was 500, now 405 |
| 9 | Unknown path | **FIXED** — was 500, now 404 |

## Bug found and fixed

`GlobalExceptionHandler` had no handler for `HttpRequestMethodNotSupportedException`
or `NoResourceFoundException`, so a wrong verb (e.g. `GET /api/auth/login`) or an
unmapped path fell through to the 500 catch-all. Added both handlers → 405 and 404
respectively. The full integration suite (52/52) still passes after the change.

## Error-envelope consistency

Every failure returns the same `ErrorResponse` shape
(`timestamp, status, error, message, path`, plus `fieldErrors` on validation):

| Status | Verified |
|---|---|
| 400 | validation with a field→message map |
| 401 | 166-byte JSON envelope (Authentication required) |
| 403 | forbidden envelope |
| 404 | not found |
| 405 | method not allowed |
| 409 | duplicate |

## Database integrity

All zero — the referential and business rules held through every test:

| Check | Count |
|---|---|
| Attendance without enrollment | 0 |
| Marks without enrollment | 0 |
| Marks without assessment | 0 |
| Students without user | 0 |
| Enrollments without student | 0 |
| Marks exceeding assessment max | 0 |
| Duplicate enrollments | 0 |
| Duplicate attendance rows | 0 |

## Responsive UI

Verified in earlier phases at 375px (mobile): no horizontal overflow; sidebar
collapses to a toggle; stat cards reflow to two columns; forms stack to one column.

## Test totals

- Backend unit tests: **23 passing** (JWT 7, attendance 7, grading 9)
- Integration suite: **52 passing**
