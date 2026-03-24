package com.example.jobPortal.service;

import java.io.File;

public interface ResumeParserService {
    /**
     * Extracts plain text from a resume file (PDF, DOC, DOCX, etc) using Apache Tika.
     * @param resumeFile The local file containing the resume to parse.
     * @return Extracted plain text up to 100,000 characters.
     */
    String extractText(File resumeFile);
}
