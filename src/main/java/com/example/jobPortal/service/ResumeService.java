package com.example.jobPortal.service;

import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {
    String uploadResume(MultipartFile file, String userEmail);
}
