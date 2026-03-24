package com.example.jobPortal.service;

import com.example.jobPortal.dto.response.ParsedResumeData;

public interface ResumeParsingService {
    /**
     * Parses raw unstructured text extracted from a resume into structured fields using RegEx and Dictionaries.
     * @param resumeText The raw string content of the resume
     * @return Organized data including email, phone, skills, experience, and education
     */
    ParsedResumeData parseResume(String resumeText);
}
