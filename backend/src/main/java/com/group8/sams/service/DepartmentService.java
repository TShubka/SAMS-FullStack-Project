package com.group8.sams.service;

import com.group8.sams.dto.request.DepartmentRequest;
import com.group8.sams.dto.response.DepartmentResponse;
import com.group8.sams.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {

    PageResponse<DepartmentResponse> findAll(String search, Pageable pageable);

    DepartmentResponse findById(Long id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentRequest request);

    void delete(Long id);
}
