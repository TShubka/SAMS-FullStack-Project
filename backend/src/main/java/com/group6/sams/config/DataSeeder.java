package com.group6.sams.config;

import com.group6.sams.entity.Department;
import com.group6.sams.entity.Role;
import com.group6.sams.entity.enums.RoleName;
import com.group6.sams.repository.DepartmentRepository;
import com.group6.sams.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Development seed data. Owner: Member 4.
 *
 * Idempotent - every insert is guarded by an existence check, so restarting the
 * application does not duplicate rows. Active only under the dev profile; it never
 * runs in prod.
 *
 * PHASE 2 SCOPE: roles and departments only. Users, students, teachers, courses,
 * enrollments, attendance, assessments and marks are seeded from Phase 3 onward,
 * once the password encoder and the domain services they depend on exist.
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    public DataSeeder(RoleRepository roleRepository,
                      DepartmentRepository departmentRepository) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedDepartments();
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
        List<Department> departments = List.of(
                Department.builder().name("Computer Science").code("CS").build(),
                Department.builder().name("Electrical Engineering").code("EE").build(),
                Department.builder().name("Mechanical Engineering").code("ME").build()
        );
        for (Department department : departments) {
            if (!departmentRepository.existsByCode(department.getCode())) {
                departmentRepository.save(department);
                log.info("Seeded department {}", department.getCode());
            }
        }
    }
}
