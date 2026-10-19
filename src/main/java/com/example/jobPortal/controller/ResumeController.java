package com.example.jobPortal.controller;

import com.example.jobPortal.dto.request.CandidateProfileUpdateDTO;
import com.example.jobPortal.dto.response.ParsedResumeData;
import com.example.jobPortal.service.CandidateProfileService;
import com.example.jobPortal.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;

@Tag(name = "Resume Management APIs", description = "Candidate tools executing multipart File Uploads, Apache Tika Parsings, Document Previews, and Profile Overrides.")
@RestController
@RequestMapping("/candidate/profile")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final CandidateProfileService candidateProfileService;

    @Operation(summary = "Upload PDF/DOCX Resume", description = "Ingests multipart file blobs actively extracting Apache Tika raw text mapping onto user Profiles natively.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
               content = @Content(mediaType = "multipart/form-data")
        ))
    @PostMapping(value = "/resume", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ParsedResumeData> uploadResume(
            @Parameter(description = "Binary document file") @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) Principal principal) {
        
        String userEmail = principal.getName();
        ParsedResumeData parsedData = resumeService.uploadResume(file, userEmail);

        return ResponseEntity.ok(parsedData);
    }

    @Operation(summary = "Preview Parsed Extraction", description = "Dynamically fetches the locally saved Raw Text running standard Regex mappings predicting structured Data outputs.")
    @GetMapping("/resume/preview")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ParsedResumeData> getParsedResumePreview(@Parameter(hidden = true) Principal principal) {
        String userEmail = principal.getName();
        ParsedResumeData preview = resumeService.getParsedResumePreview(userEmail);
        return ResponseEntity.ok(preview);
    }

    @Operation(summary = "Apply Parse Data", description = "Allows Candidate to merge Structured fields exclusively mapped against existing Null definitions cleanly.")
    @PostMapping("/resume/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<String> applyParsedResumeData(
            @RequestBody CandidateProfileUpdateDTO updateDto,
            @Parameter(hidden = true) Principal principal) {
        
        String userEmail = principal.getName();
        candidateProfileService.applyParsedResumeData(updateDto, userEmail);
        
        return ResponseEntity.ok("Candidate profile updated successfully from parsed resume data");
    }

    @Operation(summary = "Download Candidate's Own Resume", description = "Streams the authenticated candidate's active resume file.")
    @GetMapping("/resume/download")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadMyResume(
            @Parameter(hidden = true) Principal principal) {
        org.springframework.core.io.Resource resource = resumeService.loadUserResumeAsResource(principal.getName());
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Download Resume by Filename", description = "Streams a resume file by its unique filename for recruiters and candidates.")
    @GetMapping("/resume/download/{filename:.+}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResumeByFilename(
            @PathVariable String filename) {
        org.springframework.core.io.Resource resource = resumeService.loadResumeAsResource(filename);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
