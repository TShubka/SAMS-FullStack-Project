package com.group6.sams.service;

import com.group6.sams.dto.request.StudentRequest;
import com.group6.sams.dto.response.PageResponse;
import com.group6.sams.dto.response.StudentResponse;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    PageResponse<StudentResponse> findAll(Long departmentId, Integer admissionYear,
                                          String search, Pageable pageable);

    StudentResponse findById(Long id);

    StudentResponse findByUserId(Long userId);

    StudentResponse create(StudentRequest request);

    StudentResponse update(Long id, StudentRequest request);

    void delete(Long id);
}
