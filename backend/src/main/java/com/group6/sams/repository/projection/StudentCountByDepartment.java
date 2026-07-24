package com.group6.sams.repository.projection;

public interface StudentCountByDepartment {

    Long getDepartmentId();

    String getDepartmentName();

    String getDepartmentCode();

    long getStudentCount();
}
