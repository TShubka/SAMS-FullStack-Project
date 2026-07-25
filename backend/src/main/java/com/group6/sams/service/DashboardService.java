package com.group6.sams.service;

import com.group6.sams.dto.response.dashboard.AdminDashboardResponse;
import com.group6.sams.dto.response.dashboard.StudentDashboardResponse;
import com.group6.sams.dto.response.dashboard.TeacherDashboardResponse;
import com.group6.sams.security.UserPrincipal;

public interface DashboardService {

    AdminDashboardResponse admin(UserPrincipal caller);

    TeacherDashboardResponse teacher(UserPrincipal caller);

    StudentDashboardResponse student(UserPrincipal caller);
}
