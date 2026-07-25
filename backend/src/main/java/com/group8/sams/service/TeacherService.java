package com.group8.sams.service;

import com.group8.sams.dto.request.TeacherRequest;
import com.group8.sams.dto.response.PageResponse;
import com.group8.sams.dto.response.TeacherResponse;
import org.springframework.data.domain.Pageable;

public interface TeacherService {

    PageResponse<TeacherResponse> findAll(Long departmentId, String search, Pageable pageable);

    TeacherResponse findById(Long id);

    TeacherResponse findByUserId(Long userId);

    TeacherResponse create(TeacherRequest request);

    TeacherResponse update(Long id, TeacherRequest request);

    void delete(Long id);
}
