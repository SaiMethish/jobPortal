package com.example.jobPortal.service;

import java.util.List;

/**
 * Service interface for performing private Supabase Storage bucket operations.
 */
public interface SupabaseStorageService {

    /**
     * Uploads file bytes to the configured private Supabase Storage bucket.
     *
     * @param objectPath Relative object path (e.g. candidate-resumes/{candidate-id}/{uuid}.pdf)
     * @param content Raw file byte content
     * @param contentType MIME type of the file (e.g. application/pdf)
     */
    void uploadFile(String objectPath, byte[] content, String contentType);

    /**
     * Downloads an object's raw binary data from private Supabase Storage.
     *
     * @param objectPath Relative object path in the bucket
     * @return Raw binary bytes of the object
     */
    byte[] downloadFile(String objectPath);

    /**
     * Deletes an object from private Supabase Storage.
     *
     * @param objectPath Relative object path in the bucket
     */
    void deleteFile(String objectPath);

    /**
     * Lists all object paths in the bucket matching the given prefix.
     *
     * @param prefix Path prefix (e.g. candidate-resumes)
     * @return List of full relative object paths
     */
    List<String> listObjects(String prefix);
}
