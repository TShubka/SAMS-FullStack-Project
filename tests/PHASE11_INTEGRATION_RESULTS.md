# PHASE 11 — FULL INTEGRATION TEST RESULTS

**Group 8 — Student Academic Records & Attendance Management System**

Run against the live backend (`http://localhost:8080`) with the seeded demo
dataset. Reproduce with:

```powershell
./tests/integration-test.ps1
```

## Result

```
RESULT:  PASS = 52   FAIL = 0
```

Plus the backend unit suite: **23 tests, 0 failures**
(`JwtTokenProviderTest` 7, `AttendanceUtilTest` 7, `GradeUtilTest` 9).

## Coverage

| Group | Checks | What it proves |
|---|---|---|
| A. Authentication | 6 | all three roles log in; bad password, missing token and tampered token all 401 |
| B. Authorization matrix | 7 | student cannot write marks or attendance (403); student cannot read another student's records or transcript (403); a teacher cannot touch a course they do not teach (403) |
| C. CRUD round-trip | 6 | create→read→update→delete of a department; duplicate code 409; read-after-delete 404 |
| D. Search / filter / pagination | 5 | page size honoured; search finds the expected row; combined department+semester filter works |
| E. Attendance | 5 | percentage 90.00% cross-checked; course summary; exactly 2 students flagged low |
| F. Marks & grades | 3 | grades and GPA endpoints; credit-weighted GPA = 4.00 |
| G. Transcript | 2 | own transcript with cumulative GPA 4.00 |
| H. All 8 reports | 9 | every report returns 200; pass/fail shows 6 passed, matching the marks |
| I. Dashboards | 5 | all three role dashboards return 200; cross-role access 403 |
| J. Validation & errors | 4 | 400 with a `fieldErrors` map; 404 for a missing resource; self-registering as ADMIN blocked with 400 |

## The critical authorization rules (verified 403)

- Student → `POST /api/marks` → **403** (students cannot modify marks)
- Student → `POST /api/attendance` → **403**
- Student → another student's attendance / transcript → **403**
- Teacher B → Teacher A's course marks / attendance → **403**
- Any role → a dashboard for a different role → **403**

## Business figures cross-checked against the database

| Figure | Value | Independently confirmed |
|---|---|---|
| Student 1 attendance % | 90.00% | SQL aggregate |
| Student 1 GPA | 4.00 | manual credit-weighted calc |
| CS301 pass/fail | 6 passed, 2 failed, 75% | SQL aggregate |
| Low-attendance students | 2 | the two seeded below 75% |

No leftover test data: the CRUD round-trip deletes its own department, and the
UI-created artifacts were removed, leaving the seeded state (3 departments) intact.
