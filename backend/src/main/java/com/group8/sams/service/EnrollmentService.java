package com.group8.sams.service;

import com.group8.sams.dto.request.EnrollmentRequest;
import com.group8.sams.dto.response.EnrollmentResponse;
import com.group8.sams.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EnrollmentService {

    PageResponse<EnrollmentResponse> findAll(Pageable pageable);

    EnrollmentResponse findById(Long id);

    List<EnrollmentResponse> findByStudent(Long studentId);

    List<EnrollmentResponse> findByCourse(Long courseId);

    EnrollmentResponse create(EnrollmentRequest request);

    EnrollmentResponse update(Long id, EnrollmentRequest request);

    void delete(Long id);
}
