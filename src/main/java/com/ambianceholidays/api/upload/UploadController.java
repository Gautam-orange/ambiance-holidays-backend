package com.ambianceholidays.api.upload;

import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final Optional<StorageService> storageService;

    @PostMapping("/presign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> presign(
            @RequestParam(defaultValue = "image/jpeg") String contentType,
            @RequestParam(defaultValue = "media") String folder) {

        if (storageService.isEmpty()) {
            return ResponseEntity.status(503)
                    .body(ApiResponse.error("S3_UNAVAILABLE", "Storage not configured in this environment"));
        }

        StorageService.PresignResult result = storageService.get().presignUpload(contentType, folder);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "uploadUrl", result.uploadUrl(),
                "objectUrl", result.objectUrl(),
                "key", result.key()
        )));
    }
}
