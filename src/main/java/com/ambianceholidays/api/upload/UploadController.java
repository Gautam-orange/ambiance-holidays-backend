package com.ambianceholidays.api.upload;

import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.exception.BusinessException;
import com.ambianceholidays.storage.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private static final long MAX_BYTES = 10 * 1024 * 1024L;   // 10 MB ceiling
    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml");

    private final Optional<StorageService> storageService;

    /** Where uploads land when no S3 is configured. Defaults to ./uploads-local relative to CWD. */
    @Value("${app.uploads.local-dir:./uploads-local}")
    private String localDir;

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

    /**
     * One-shot multipart upload. Always works — uses S3 when configured, otherwise drops the
     * file in a local directory and serves it back at GET /uploads/files/{name}. Returns the
     * URL the client should store on the entity.
     */
    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS','FLEET_MANAGER')")
    public ApiResponse<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "media") String folder,
            HttpServletRequest req) throws IOException {

        if (file.isEmpty()) {
            throw BusinessException.badRequest("EMPTY_FILE", "Upload is empty");
        }
        if (file.getSize() > MAX_BYTES) {
            throw BusinessException.badRequest("FILE_TOO_LARGE",
                    "Image must be ≤ " + (MAX_BYTES / 1024 / 1024) + "MB");
        }
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        if (!ALLOWED.contains(contentType.toLowerCase())) {
            throw BusinessException.badRequest("UNSUPPORTED_TYPE",
                    "Allowed types: " + ALLOWED + " (got " + contentType + ")");
        }

        String ext = guessExt(contentType, file.getOriginalFilename());
        String key = folder.replaceAll("[^a-zA-Z0-9_-]", "") + "/" + UUID.randomUUID() + ext;

        // Prefer S3 when available.
        if (storageService.isPresent()) {
            try {
                String objectUrl = storageService.get().uploadBytes(key, contentType, file.getBytes());
                return ApiResponse.ok(Map.of("url", objectUrl, "key", key));
            } catch (Exception e) {
                log.warn("S3 upload failed ({}), falling back to local filesystem", e.getMessage());
            }
        }

        // Local-FS fallback: write under {localDir}/{folder}/{uuid}.{ext}
        Path target = Paths.get(localDir).toAbsolutePath().normalize().resolve(key);
        Files.createDirectories(target.getParent());
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // Build a public URL the SPA can render. We return a relative /api/v1/uploads/files/...
        // path so it works equally on dev (vite proxy) and prod (Apache).
        String publicUrl = "/api/v1/uploads/files/" + key;
        log.info("Saved upload to {} ({} bytes) → {}", target, file.getSize(), publicUrl);
        return ApiResponse.ok(Map.of("url", publicUrl, "key", key));
    }

    /**
     * Static file server for uploads stored on the local filesystem (S3-less envs).
     * Read-open: car / tour images need to render for non-admin browsers too.
     */
    @GetMapping("/files/{folder}/{name:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String folder, @PathVariable String name) throws IOException {
        Path file = Paths.get(localDir).toAbsolutePath().normalize().resolve(folder).resolve(name);
        if (!file.startsWith(Paths.get(localDir).toAbsolutePath().normalize())) {
            // Defensive against ../ escapes.
            return ResponseEntity.notFound().build();
        }
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(file.toUri());
        MediaType mt = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mt)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .body(resource);
    }

    private static String guessExt(String contentType, String originalName) {
        // Prefer the original extension (preserves naming) but defensively normalise.
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0 && dot < originalName.length() - 1) {
                String ext = originalName.substring(dot).toLowerCase().replaceAll("[^a-z0-9.]", "");
                if (ext.length() <= 6) return ext;
            }
        }
        return switch (contentType.toLowerCase()) {
            case "image/png"     -> ".png";
            case "image/webp"    -> ".webp";
            case "image/gif"     -> ".gif";
            case "image/svg+xml" -> ".svg";
            default              -> ".jpg";
        };
    }
}
