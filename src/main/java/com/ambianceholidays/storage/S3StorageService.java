package com.ambianceholidays.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.s3.enabled", havingValue = "true")
public class S3StorageService implements StorageService {

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.region:eu-west-1}")
    private String region;

    @Value("${app.s3.endpoint:}")
    private String endpoint;

    @Override
    public PresignResult presignUpload(String contentType, String folder) {
        String key = (folder != null ? folder + "/" : "") + UUID.randomUUID();
        String ext = extensionFor(contentType);
        String fullKey = key + ext;

        S3Presigner.Builder builder = S3Presigner.builder().region(Region.of(region));
        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        try (S3Presigner presigner = builder.build()) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fullKey)
                    .contentType(contentType)
                    .build();

            PresignedPutObjectRequest presigned = presigner.presignPutObject(
                    PutObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(15))
                            .putObjectRequest(putRequest)
                            .build());

            String objectUrl = endpoint.isBlank()
                    ? "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fullKey
                    : endpoint + "/" + bucket + "/" + fullKey;

            return new PresignResult(presigned.url().toString(), objectUrl, fullKey);
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }
}
