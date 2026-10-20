package com.example.jobPortal.service.Impl;

import com.example.jobPortal.exception.FileUploadException;
import com.example.jobPortal.exception.ResourceNotFoundException;
import com.example.jobPortal.service.SupabaseStorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of SupabaseStorageService utilizing Supabase Storage v1 REST API.
 * Includes graceful in-memory storage fallback for local development or testing when
 * Supabase credentials are not explicitly provided.
 */
@Service
@Slf4j
public class SupabaseStorageServiceImpl implements SupabaseStorageService {

    private final String supabaseUrl;
    private final String supabaseKey;
    private final String bucketName;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    // In-memory fallback map for offline tests or when unconfigured
    private final Map<String, byte[]> inMemoryStorage = new ConcurrentHashMap<>();

    public SupabaseStorageServiceImpl(
            @Value("${supabase.url:}") String supabaseUrl,
            @Value("${supabase.key:}") String supabaseKey,
            @Value("${supabase.bucket:resumes}") String bucketName,
            ObjectMapper objectMapper) {
        this.supabaseUrl = (supabaseUrl != null) ? supabaseUrl.trim() : "";
        this.supabaseKey = (supabaseKey != null) ? supabaseKey.trim() : "";
        this.bucketName = (bucketName != null && !bucketName.isBlank()) ? bucketName.trim() : "resumes";
        this.objectMapper = objectMapper;

        if (!isUnconfigured()) {
            this.restClient = RestClient.builder()
                    .baseUrl(this.supabaseUrl + "/storage/v1")
                    .defaultHeader("Authorization", "Bearer " + this.supabaseKey)
                    .defaultHeader("apikey", this.supabaseKey)
                    .build();
            log.info("Supabase Storage initialized for private bucket '{}' at {}", this.bucketName, this.supabaseUrl);
        } else {
            this.restClient = null;
            log.warn("Supabase Storage credentials unconfigured or using placeholders. In-memory storage enabled for tests/dev.");
        }
    }

    private boolean isUnconfigured() {
        return supabaseUrl.isBlank()
                || supabaseUrl.contains("xyzcompany.supabase.co")
                || supabaseKey.isBlank();
    }

    @Override
    public void uploadFile(String objectPath, byte[] content, String contentType) {
        if (isUnconfigured()) {
            inMemoryStorage.put(objectPath, content);
            log.info("[In-Memory Storage] Saved {} ({} bytes)", objectPath, content.length);
            return;
        }

        try {
            String path = sanitizePath(objectPath);
            restClient.post()
                    .uri("/object/{bucket}/{wildcard}", bucketName, path)
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .header("x-upsert", "true")
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully uploaded object to Supabase Storage: {}/{}", bucketName, path);
        } catch (Exception e) {
            log.error("Failed to upload object to Supabase Storage: {}", objectPath, e);
            throw new FileUploadException("Could not store file in Supabase Storage: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadFile(String objectPath) {
        if (isUnconfigured()) {
            byte[] data = inMemoryStorage.get(objectPath);
            if (data == null) {
                throw new ResourceNotFoundException("Resume file not found in storage: " + objectPath);
            }
            return data;
        }

        try {
            String path = sanitizePath(objectPath);
            // Retrieve private object bytes using /object/authenticated/ endpoint
            byte[] bytes = restClient.get()
                    .uri("/object/authenticated/{bucket}/{wildcard}", bucketName, path)
                    .retrieve()
                    .body(byte[].class);

            if (bytes == null || bytes.length == 0) {
                throw new ResourceNotFoundException("Resume file not found in storage: " + objectPath);
            }
            return bytes;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to download object from Supabase Storage: {}", objectPath, e);
            throw new ResourceNotFoundException("Could not retrieve resume from Supabase Storage: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String objectPath) {
        if (isUnconfigured()) {
            inMemoryStorage.remove(objectPath);
            log.info("[In-Memory Storage] Deleted {}", objectPath);
            return;
        }

        try {
            String path = sanitizePath(objectPath);
            restClient.delete()
                    .uri("/object/{bucket}/{wildcard}", bucketName, path)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully deleted object from Supabase Storage: {}/{}", bucketName, path);
        } catch (Exception e) {
            log.warn("Failed to delete object from Supabase Storage: {}", objectPath, e);
        }
    }

    @Override
    public List<String> listObjects(String prefix) {
        if (isUnconfigured()) {
            List<String> list = new ArrayList<>();
            for (String key : inMemoryStorage.keySet()) {
                if (prefix == null || prefix.isBlank() || key.startsWith(prefix)) {
                    list.add(key);
                }
            }
            return list;
        }

        try {
            List<String> results = new ArrayList<>();
            collectObjectsRecursively(prefix != null ? prefix : "", results);
            return results;
        } catch (Exception e) {
            log.error("Failed to list objects in Supabase Storage with prefix: {}", prefix, e);
            return Collections.emptyList();
        }
    }

    private void collectObjectsRecursively(String currentPrefix, List<String> accumulator) {
        Map<String, Object> requestBody = Map.of(
                "prefix", currentPrefix,
                "limit", 1000
        );

        String jsonResponse = restClient.post()
                .uri("/object/list/{bucket}", bucketName)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        if (jsonResponse == null || jsonResponse.isBlank()) {
            return;
        }

        try {
            JsonNode arrayNode = objectMapper.readTree(jsonResponse);
            if (arrayNode.isArray()) {
                for (JsonNode item : arrayNode) {
                    String name = item.path("name").asText();
                    boolean isFolder = item.path("id").isNull() || item.path("id").isMissingNode();
                    String fullPath = currentPrefix.isBlank() ? name : (currentPrefix + "/" + name);

                    if (isFolder) {
                        collectObjectsRecursively(fullPath, accumulator);
                    } else {
                        accumulator.add(fullPath);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing list objects response for prefix: {}", currentPrefix, e);
        }
    }

    private String sanitizePath(String path) {
        if (path == null) return "";
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
