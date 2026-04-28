package com.ambianceholidays.storage;

public interface StorageService {
    PresignResult presignUpload(String contentType, String folder);

    record PresignResult(String uploadUrl, String objectUrl, String key) {}
}
