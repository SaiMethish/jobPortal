package com.example.jobPortal.service;

import com.example.jobPortal.dto.response.ParsedResumeData;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {
    ParsedResumeData uploadResume(MultipartFile file, String userEmail);
    ParsedResumeData getParsedResumePreview(String userEmail);
    Resource loadResumeAsResource(String filename);
    Resource loadUserResumeAsResource(String userEmail);
}
