package com.group6.sams.service;

import com.group6.sams.dto.response.TranscriptResponse;
import com.group6.sams.security.UserPrincipal;

public interface TranscriptService {

    TranscriptResponse forStudent(Long studentId, UserPrincipal caller);
}
