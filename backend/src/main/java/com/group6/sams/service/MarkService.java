package com.group6.sams.service;

import com.group6.sams.dto.request.MarkRequest;
import com.group6.sams.dto.response.MarkResponse;
import com.group6.sams.security.UserPrincipal;

import java.util.List;

public interface MarkService {

    MarkResponse create(MarkRequest request, UserPrincipal caller);

    MarkResponse update(Long id, MarkRequest request, UserPrincipal caller);

    void delete(Long id, UserPrincipal caller);

    List<MarkResponse> findByEnrollment(Long enrollmentId, UserPrincipal caller);

    List<MarkResponse> findByCourse(Long courseId, UserPrincipal caller);
}
