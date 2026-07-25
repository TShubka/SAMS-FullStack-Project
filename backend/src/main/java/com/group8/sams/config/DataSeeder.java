package com.group8.sams.config;

import com.group8.sams.entity.*;
import com.group8.sams.entity.enums.*;
import com.group8.sams.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Development seed data. Owner: Member 4. Active only under the dev profile.
 *
 * Idempotent: it seeds nothing beyond roles unless the database has no users yet,
 * so restarting the app never duplicates the demo set. A single guard flag keeps
 * that decision in one place.
 *
 * Everything it creates is real rows queried through the same API as production -
 * the dashboards and reports show genuine figures, never mocked numbers. This is
 * the mitigation for the "empty dashboards at demo time" risk (R10). The two
 * students seeded below 75% attendance exist so the low-attendance report has real
 * output to display.
 *
 * The default password for every seeded account is "Password123".
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "Password123";

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssessmentRepository assessmentRepository;
    private final MarkRepository markRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      DepartmentRepository departmentRepository,
                      UserRepository userRepository,
                      StudentRepository studentRepository,
                      TeacherRepository teacherRepository,
                      CourseRepository courseRepository,
                      EnrollmentRepository enrollmentRepository,
                      AttendanceRepository attendanceRepository,
                      AssessmentRepository assessmentRepository,
                      MarkRepository markRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.assessmentRepository = assessmentRepository;
        this.markRepository = markRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedDepartments();

        // Any existing users mean a seeded (or hand-built) dataset is already present.
        // Seeding on top would duplicate roll numbers and codes, so we stop here.
        if (userRepository.count() > 0) {
            log.info("Users already present - skipping demo data seed");
            return;
        }
        seedDemoData();
    }

    private void seedRoles() {
        for (RoleName name : RoleName.values()) {
            if (!roleRepository.existsByName(name)) {
                roleRepository.save(new Role(name));
                log.info("Seeded role {}", name);
            }
        }
    }

    private void seedDepartments() {
        record Dept(String name, String code) {}
        List.of(new Dept("Computer Science", "CS"),
                new Dept("Electrical Engineering", "EE"),
                new Dept("Mechanical Engineering", "ME"))
            .forEach(d -> {
                if (!departmentRepository.existsByCode(d.code())) {
                    departmentRepository.save(
                            Department.builder().name(d.name()).code(d.code()).build());
                    log.info("Seeded department {}", d.code());
                }
            });
    }

    private void seedDemoData() {
        Role admin = role(RoleName.ROLE_ADMIN);
        Role teacherRole = role(RoleName.ROLE_TEACHER);
        Role studentRole = role(RoleName.ROLE_STUDENT);
        Role userRole = role(RoleName.ROLE_USER);

        Department cs = departmentRepository.findByCode("CS").orElseThrow();
        Department ee = departmentRepository.findByCode("EE").orElseThrow();

        // Admin
        createUser("admin", "admin@sams.edu", Set.of(admin, userRole));

        // Teachers
        Teacher tSmith = createTeacher("t.smith", "smith@sams.edu", teacherRole, userRole,
                cs, "EMP1001", "Sarah", "Smith", "Associate Professor");
        Teacher tKhan = createTeacher("t.khan", "khan@sams.edu", teacherRole, userRole,
                ee, "EMP1002", "Imran", "Khan", "Lecturer");

        // Courses (Smith teaches two CS, Khan teaches one EE)
        Course db = createCourse(cs, tSmith, "CS301", "Database Systems", 3, 3);
        Course algo = createCourse(cs, tSmith, "CS302", "Algorithms", 4, 3);
        Course circuits = createCourse(ee, tKhan, "EE201", "Circuit Analysis", 3, 2);

        // Students: 8 in CS, 2 in EE
        List<Student> csStudents = new ArrayList<>();
        String[][] csNames = {
                {"Amina", "Yusuf"}, {"Bilal", "Omar"}, {"Chloe", "Reed"}, {"Daniel", "Park"},
                {"Esraa", "Nur"}, {"Farid", "Aziz"}, {"Grace", "Lee"}, {"Hassan", "Ali"}
        };
        for (int i = 0; i < csNames.length; i++) {
            csStudents.add(createStudent("cs.student" + (i + 1),
                    "cs" + (i + 1) + "@sams.edu", studentRole, userRole, cs,
                    "CS2023%03d".formatted(i + 1), csNames[i][0], csNames[i][1], 2023, 3));
        }
        Student ee1 = createStudent("ee.student1", "ee1@sams.edu", studentRole, userRole, ee,
                "EE2024001", "Karim", "Saleh", 2024, 2);
        Student ee2 = createStudent("ee.student2", "ee2@sams.edu", studentRole, userRole, ee,
                "EE2024002", "Layla", "Hadi", 2024, 2);

        // Enrollments: all CS students into both CS courses, EE students into circuits
        List<Enrollment> dbEnrollments = new ArrayList<>();
        for (Student s : csStudents) {
            dbEnrollments.add(enroll(s, db));
            enroll(s, algo);
        }
        enroll(ee1, circuits);
        enroll(ee2, circuits);

        // Assessments for Database Systems: Quiz 20%, Midterm 30%, Final 50%
        Assessment quiz = assessment(db, "Quiz 1", AssessmentType.QUIZ, "10", "20");
        Assessment midterm = assessment(db, "Midterm", AssessmentType.MIDTERM, "50", "30");
        Assessment finalExam = assessment(db, "Final", AssessmentType.FINAL, "100", "50");

        // Marks and attendance across a spread so grades and the low-attendance report
        // both have realistic, non-empty output. The last two students are kept below
        // 75% attendance on purpose.
        int[] quizScores    = {9, 8, 7, 6, 5, 8, 4, 3};   // of 10
        int[] midtermScores = {45, 40, 38, 30, 25, 42, 20, 15}; // of 50
        int[] finalScores   = {92, 85, 78, 65, 55, 88, 45, 30}; // of 100

        for (int i = 0; i < dbEnrollments.size(); i++) {
            Enrollment e = dbEnrollments.get(i);
            mark(e, quiz, tSmith, quizScores[i]);
            mark(e, midterm, tSmith, midtermScores[i]);
            mark(e, finalExam, tSmith, finalScores[i]);
            // 20 class days; the last two students attend far fewer to fall below 75%.
            int present = (i < 6) ? 18 : 12;
            seedAttendance(e, tSmith, present, 20);
        }

        log.info("Seeded demo data: 1 admin, 2 teachers, 10 students, 3 courses, "
                 + "{} enrollments, 3 assessments, marks and 20 days of attendance",
                 enrollmentRepository.count());
    }

    // ---- helpers -------------------------------------------------------------

    private Role role(RoleName name) {
        return roleRepository.findByName(name).orElseThrow();
    }

    private User createUser(String username, String email, Set<Role> roles) {
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .enabled(true)
                .roles(new HashSet<>(roles))
                .build());
    }

    private Teacher createTeacher(String username, String email, Role teacherRole, Role userRole,
                                  Department dept, String code, String first, String last,
                                  String designation) {
        User user = createUser(username, email, Set.of(teacherRole, userRole));
        return teacherRepository.save(Teacher.builder()
                .user(user).department(dept).employeeCode(code)
                .firstName(first).lastName(last).designation(designation)
                .build());
    }

    private Student createStudent(String username, String email, Role studentRole, Role userRole,
                                  Department dept, String roll, String first, String last,
                                  int admissionYear, int semester) {
        User user = createUser(username, email, Set.of(studentRole, userRole));
        return studentRepository.save(Student.builder()
                .user(user).department(dept).rollNumber(roll)
                .firstName(first).lastName(last)
                .admissionYear(admissionYear).currentSemester(semester)
                .build());
    }

    private Course createCourse(Department dept, Teacher teacher, String code, String title,
                                int credits, int semester) {
        return courseRepository.save(Course.builder()
                .department(dept).teacher(teacher).code(code).title(title)
                .credits(credits).semester(semester)
                .build());
    }

    private Enrollment enroll(Student student, Course course) {
        return enrollmentRepository.save(Enrollment.builder()
                .student(student).course(course)
                .semester(course.getSemester()).academicYear("2025-2026")
                .status(EnrollmentStatus.ACTIVE).enrolledOn(LocalDate.now())
                .build());
    }

    private Assessment assessment(Course course, String title, AssessmentType type,
                                  String maxMarks, String weight) {
        return assessmentRepository.save(Assessment.builder()
                .course(course).title(title).type(type)
                .maxMarks(new BigDecimal(maxMarks)).weightPercent(new BigDecimal(weight))
                .assessedOn(LocalDate.now().minusDays(7))
                .build());
    }

    private void mark(Enrollment enrollment, Assessment assessment, Teacher teacher, int score) {
        markRepository.save(Mark.builder()
                .enrollment(enrollment).assessment(assessment).enteredBy(teacher)
                .marksObtained(new BigDecimal(score))
                .build());
    }

    private void seedAttendance(Enrollment enrollment, Teacher teacher, int present, int total) {
        LocalDate start = LocalDate.now().minusDays(total + 5L);
        for (int day = 0; day < total; day++) {
            AttendanceStatus status = (day < present)
                    ? AttendanceStatus.PRESENT
                    : AttendanceStatus.ABSENT;
            attendanceRepository.save(Attendance.builder()
                    .enrollment(enrollment).recordedBy(teacher)
                    .attendanceDate(start.plusDays(day))
                    .status(status)
                    .build());
        }
    }
}
