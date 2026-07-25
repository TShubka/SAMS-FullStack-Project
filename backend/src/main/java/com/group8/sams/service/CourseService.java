package com.group8.sams.service;

import com.group8.sams.dto.request.CourseRequest;
import com.group8.sams.dto.response.CourseResponse;
import com.group8.sams.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {

    PageResponse<CourseResponse> findAll(Long departmentId, Integer semester,
                                         Long teacherId, String search, Pageable pageable);

    CourseResponse findById(Long id);

    /** Courses assigned to the teacher profile linked to this user account. */
    List<CourseResponse> findMyCourses(Long userId);

    CourseResponse create(CourseRequest request);

    CourseResponse update(Long id, CourseRequest request);

    void delete(Long id);
}
