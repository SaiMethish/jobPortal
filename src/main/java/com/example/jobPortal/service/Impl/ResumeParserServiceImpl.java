package com.example.jobPortal.service.Impl;

import com.example.jobPortal.exception.ResumeParsingException;
import com.example.jobPortal.service.ResumeParserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
    public String extractText(byte[] fileBytes, String filename) {
        if (fileBytes == null || fileBytes.length == 0) {
            return "";
        }
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            return extractText(is, filename);
        } catch (Exception e) {
            log.error("Failed to parse resume bytes for file: {}", filename, e);
            throw new ResumeParsingException("Unable to read resume bytes: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractText(InputStream inputStream, String filename) {
        if (inputStream == null) {
            throw new ResumeParsingException("Invalid or missing resume stream provided for parsing.");
        }

        try {
            log.info("Starting text extraction for file: {}", filename != null ? filename : "unknown");
            String extractedText = tika.parseToString(inputStream);

            if (extractedText == null || extractedText.trim().isEmpty()) {
                return "";
            }

            extractedText = extractedText.trim();
            if (extractedText.length() > MAX_TEXT_LENGTH) {
                extractedText = extractedText.substring(0, MAX_TEXT_LENGTH);
            }

            log.info("Successfully extracted {} characters from {}", extractedText.length(), filename != null ? filename : "stream");
            return extractedText;

        } catch (Exception e) {
            log.error("Failed to parse resume stream for file: {}", filename, e);
            throw new ResumeParsingException("Unable to read resume: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractText(File resumeFile) {
        if (resumeFile == null || !resumeFile.exists()) {
            throw new ResumeParsingException("Invalid or missing resume file provided for parsing.");
        }

        try (InputStream is = new FileInputStream(resumeFile)) {
            return extractText(is, resumeFile.getName());
        } catch (Exception e) {
            log.error("Failed to parse resume file: {}", resumeFile.getName(), e);
            throw new ResumeParsingException("Unable to read resume: " + e.getMessage(), e);
        }
    }
}
