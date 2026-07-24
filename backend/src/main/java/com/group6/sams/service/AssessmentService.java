package com.group6.sams.service;

import com.group6.sams.dto.request.AssessmentRequest;
import com.group6.sams.dto.response.AssessmentResponse;
import com.group6.sams.security.UserPrincipal;

import java.util.List;

public interface AssessmentService {

    List<AssessmentResponse> findByCourse(Long courseId, UserPrincipal caller);

    AssessmentResponse findById(Long id, UserPrincipal caller);

    AssessmentResponse create(AssessmentRequest request, UserPrincipal caller);

    AssessmentResponse update(Long id, AssessmentRequest request, UserPrincipal caller);

    void delete(Long id, UserPrincipal caller);
}
