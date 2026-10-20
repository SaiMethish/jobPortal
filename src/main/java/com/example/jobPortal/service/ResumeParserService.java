package com.example.jobPortal.service;

import java.io.File;
import java.io.InputStream;

public interface ResumeParserService {
    /**
     * Extracts plain text directly from in-memory file bytes using Apache Tika.
     *
     * @param fileBytes The byte array of the uploaded resume.
     * @param filename  The original file name for logging and parser hints.
     * @return Extracted plain text up to 100,000 characters.
     */
    String extractText(byte[] fileBytes, String filename);

    /**
     * Extracts plain text from an InputStream using Apache Tika.
     *
     * @param inputStream The stream containing document data.
     * @param filename    The original file name for logging and parser hints.
     * @return Extracted plain text up to 100,000 characters.
     */
    String extractText(InputStream inputStream, String filename);

    /**
     * Extracts plain text from a resume file (PDF, DOC, DOCX, etc) using Apache Tika.
     *
     * @param resumeFile The local file containing the resume to parse.
     * @return Extracted plain text up to 100,000 characters.
     */
    String extractText(File resumeFile);
}
