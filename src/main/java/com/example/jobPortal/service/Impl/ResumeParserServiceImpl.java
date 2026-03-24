package com.example.jobPortal.service.Impl;

import com.example.jobPortal.exception.ResumeParsingException;
import com.example.jobPortal.service.ResumeParserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class ResumeParserServiceImpl implements ResumeParserService {

    private final Tika tika;
    private static final int MAX_TEXT_LENGTH = 100000;

    public ResumeParserServiceImpl() {
        this.tika = new Tika();
        // Sets maximum string length extracted to ~100k roughly to prevent DB bloat
        this.tika.setMaxStringLength(MAX_TEXT_LENGTH);
    }

    @Override
    public String extractText(File resumeFile) {
        if (resumeFile == null || !resumeFile.exists()) {
            throw new ResumeParsingException("Invalid or missing resume file provided for parsing.");
        }

        try {
            log.info("Starting text extraction for file: {}", resumeFile.getName());
            String extractedText = tika.parseToString(resumeFile);
            
            if (extractedText == null || extractedText.trim().isEmpty()) {
                return "";
            }

            // Additional safety trim
            extractedText = extractedText.trim();
            if (extractedText.length() > MAX_TEXT_LENGTH) {
                extractedText = extractedText.substring(0, MAX_TEXT_LENGTH);
            }

            log.info("Successfully extracted {} characters from {}", extractedText.length(), resumeFile.getName());
            return extractedText;

        } catch (Exception e) {
            log.error("Failed to parse resume file: {}", resumeFile.getName(), e);
            throw new ResumeParsingException("Unable to read resume: " + e.getMessage(), e);
        }
    }
}
