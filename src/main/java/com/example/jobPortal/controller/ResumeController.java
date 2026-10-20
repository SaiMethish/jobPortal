package com.example.jobPortal.controller;

import com.example.jobPortal.dto.request.CandidateProfileUpdateDTO;
import com.example.jobPortal.dto.response.ParsedResumeData;
import com.example.jobPortal.service.CandidateProfileService;
import com.example.jobPortal.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.security.Principal;

@Tag(name = "Resume Management APIs", description = "Candidate tools executing multipart File Uploads to Supabase Storage, Apache Tika Parsings, Document Previews, and Profile Overrides.")
@RestController
@RequestMapping("/candidate/profile")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final CandidateProfileService candidateProfileService;

    @Operation(summary = "Upload PDF/DOCX Resume", description = "Ingests multipart file blobs directly into private Supabase Storage, actively extracting Apache Tika raw text mapping onto user Profiles natively.",
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

    @Operation(summary = "Download Candidate's Own Resume", description = "Streams the authenticated candidate's active resume file from private Supabase Storage.")
    @GetMapping("/resume/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadMyResume(
            @RequestParam(value = "path", required = false) String path,
            @Parameter(hidden = true) Principal principal) {
        Resource resource;
        if (path != null && !path.isBlank()) {
            resource = resumeService.loadResumeAsResource(path);
        } else {
            resource = resumeService.loadUserResumeAsResource(principal.getName());
        }
        return buildDownloadResponse(resource);
    }

    @Operation(summary = "Download Resume by Filename or Storage Path", description = "Streams a resume file by its unique filename or cloud storage object path for recruiters and candidates.")
    @GetMapping("/resume/download/**")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadResumeByPath(HttpServletRequest request) {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String filenameOrPath = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, fullPath);

        Resource resource = resumeService.loadResumeAsResource(filenameOrPath);
        return buildDownloadResponse(resource);
    }

    private ResponseEntity<Resource> buildDownloadResponse(Resource resource) {
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
