package com.ambianceholidays.storage;

public interface StorageService {
    PresignResult presignUpload(String contentType, String folder);

    /**
     * Server-side upload (one-shot). Returns the public objectUrl to store on the entity.
     * Default impl throws — only S3-backed services need to implement this.
     */
    default String uploadBytes(String key, String contentType, byte[] bytes) {
        throw new UnsupportedOperationException("uploadBytes not implemented by " + getClass().getSimpleName());
    }

    record PresignResult(String uploadUrl, String objectUrl, String key) {}
}
