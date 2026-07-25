package com.group8.sams.service;

import com.group8.sams.dto.response.dashboard.AdminDashboardResponse;
import com.group8.sams.dto.response.dashboard.StudentDashboardResponse;
import com.group8.sams.dto.response.dashboard.TeacherDashboardResponse;
import com.group8.sams.security.UserPrincipal;

public interface DashboardService {

    AdminDashboardResponse admin(UserPrincipal caller);

    TeacherDashboardResponse teacher(UserPrincipal caller);

    StudentDashboardResponse student(UserPrincipal caller);
}
