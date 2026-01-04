package com.ssafy.cotrip.api.image.service;

import com.ssafy.cotrip.api.image.dto.request.UploadUrlRequest;
import com.ssafy.cotrip.api.image.dto.response.ReadUrlResponse;
import com.ssafy.cotrip.api.image.dto.response.UploadUrlResponse;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.S3Handler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.image-prefix}")
    private String imagePrefix;

    @Value("${cloud.aws.s3.presign.put-exp-min}")
    private long putExpMin;

    @Value("${cloud.aws.s3.presign.get-exp-min}")
    private long getExpMin;

    public UploadUrlResponse createPresignedUploadUrl(UploadUrlRequest request) {
        String key = buildKey(request.originalFileName(), request.contentType());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(request.contentType())
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(p -> p
                .signatureDuration(Duration.ofMinutes(putExpMin))
                .putObjectRequest(putObjectRequest)
        );

        long expiresInSec = calculateRemainingSeconds(presigned.expiration());
        String putUrl = presigned.url().toString();

        return UploadUrlResponse.builder()
                .key(key)
                .putUrl(putUrl)
                .expiresInSec(expiresInSec)
                .build();
    }

    public ReadUrlResponse createPresignedReadUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(p -> p
                .signatureDuration(Duration.ofMinutes(getExpMin))
                .getObjectRequest(getObjectRequest)
        );

        long expiresInSec = calculateRemainingSeconds(presigned.expiration());
        String getUrl = presigned.url().toString();

        return ReadUrlResponse.builder()
                .key(key)
                .getUrl(getUrl)
                .expiresInSec(expiresInSec)
                .build();
    }

    public void deleteImage(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            throw new S3Handler(ErrorStatus.FAIL_IMAGE_DELETE);
        }
    }

    private long calculateRemainingSeconds(Instant expiration) {
        long nowEpochSec = Instant.now().getEpochSecond();
        return expiration.getEpochSecond() - nowEpochSec;
    }

    private String buildKey(String originalFileName, String contentType) {
        String extension = switch (contentType) {
            case "image/jpg", "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new S3Handler(ErrorStatus.IMAGE_INVALID_EXTENSION);
        };

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = timestamp + "_" + UUID.randomUUID() + "." + extension;

        return imagePrefix + "/" + filename;
    }
}

