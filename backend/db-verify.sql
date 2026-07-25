-- =====================================================================
-- PHASE 2 DATABASE VERIFICATION
-- Group 8 - Student Academic Records & Attendance Management System
--
-- Run AFTER starting the application once (Hibernate creates the schema
-- under the dev profile via ddl-auto: update).
--
--   psql -U postgres -h localhost -d sams_db -f db-verify.sql
--
-- Checks 1-9 correspond to the Phase 1 verification plan. Every check
-- prints its real result; nothing is asserted silently.
-- =====================================================================

\echo '=== CHECK 3: all 11 tables exist ==='
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

\echo ''
\echo '--- expected 11: assessments, attendance, courses, departments,'
\echo '--- enrollments, marks, roles, students, teachers, user_roles, users'

\echo ''
\echo '=== CHECK 4: column types and nullability ==='
SELECT table_name, column_name, data_type,
       character_maximum_length AS len,
       numeric_precision AS prec, numeric_scale AS scale,
       is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;

\echo ''
\echo '=== CHECK 5: unique constraints ==='
SELECT tc.table_name, tc.constraint_name,
       string_agg(kcu.column_name, ', ' ORDER BY kcu.ordinal_position) AS columns
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
     ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
WHERE tc.table_schema = 'public'
  AND tc.constraint_type = 'UNIQUE'
GROUP BY tc.table_name, tc.constraint_name
ORDER BY tc.table_name;

\echo ''
\echo '--- The three that matter most:'
\echo '--- uk_enrollment_student_course_term (student, course, semester, academic_year)'
\echo '--- uk_attendance_enrollment_date     (enrollment_id, attendance_date)'
\echo '--- uk_mark_enrollment_assessment     (enrollment_id, assessment_id)'

\echo ''
\echo '=== CHECK 6: indexes ==='
SELECT tablename, indexname
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

\echo ''
\echo '=== CHECK 7: foreign keys and their ON DELETE actions ==='
SELECT tc.table_name AS child_table,
       kcu.column_name AS child_column,
       ccu.table_name AS parent_table,
       rc.delete_rule AS on_delete
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
     ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu
     ON tc.constraint_name = ccu.constraint_name
JOIN information_schema.referential_constraints rc
     ON tc.constraint_name = rc.constraint_name
WHERE tc.table_schema = 'public'
  AND tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name, kcu.column_name;

\echo ''
\echo '=== CHECK 8: seed data row counts ==='
SELECT 'roles' AS table_name, COUNT(*) AS rows FROM roles
UNION ALL SELECT 'departments', COUNT(*) FROM departments
UNION ALL SELECT 'users',       COUNT(*) FROM users
UNION ALL SELECT 'students',    COUNT(*) FROM students
UNION ALL SELECT 'teachers',    COUNT(*) FROM teachers
UNION ALL SELECT 'courses',     COUNT(*) FROM courses
UNION ALL SELECT 'enrollments', COUNT(*) FROM enrollments
UNION ALL SELECT 'attendance',  COUNT(*) FROM attendance
UNION ALL SELECT 'assessments', COUNT(*) FROM assessments
UNION ALL SELECT 'marks',       COUNT(*) FROM marks
ORDER BY table_name;

\echo ''
\echo '--- Phase 2 expects roles = 4 and departments = 3.'
\echo '--- The other tables are seeded from Phase 3 onward and are legitimately 0 now.'

\echo ''
\echo '=== CHECK 5b: constraints actually REJECT duplicates ==='
\echo '--- Each block below must FAIL. A success here is a real defect.'

\echo ''
\echo '--- 5b-1: duplicate role name must be rejected'
SAVEPOINT sp1;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
ROLLBACK TO SAVEPOINT sp1;

\echo ''
\echo '--- 5b-2: duplicate department code must be rejected'
SAVEPOINT sp2;
INSERT INTO departments (name, code, created_at)
VALUES ('Duplicate CS Attempt', 'CS', NOW());
ROLLBACK TO SAVEPOINT sp2;

\echo ''
\echo '=== VERIFICATION SCRIPT COMPLETE ==='
\echo 'Review the output above against the Phase 1 design document.'
