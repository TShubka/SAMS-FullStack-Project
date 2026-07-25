package com.group8.sams.service;

import com.group8.sams.dto.response.TranscriptResponse;
import com.group8.sams.security.UserPrincipal;

public interface TranscriptService {

    TranscriptResponse forStudent(Long studentId, UserPrincipal caller);
}
