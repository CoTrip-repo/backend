package com.ssafy.cotrip.api.image.controller;

import com.ssafy.cotrip.api.image.dto.request.UploadUrlRequest;
import com.ssafy.cotrip.api.image.dto.response.ReadUrlResponse;
import com.ssafy.cotrip.api.image.dto.response.UploadUrlResponse;
import com.ssafy.cotrip.api.image.service.S3Service;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/v1/images/upload-url")
    public ApiResponse<UploadUrlResponse> createUploadUrl(@Valid @RequestBody UploadUrlRequest request) {
        UploadUrlResponse response = s3Service.createPresignedUploadUrl(request);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/v1/images/read-url")
    public ApiResponse<ReadUrlResponse> createReadUrl(@RequestParam String key) {
        ReadUrlResponse response = s3Service.createPresignedReadUrl(key);
        return ApiResponse.onSuccess(response);
    }

    // @DeleteMapping("/v1/images")
    //     public ApiResponse<Void> delete(@RequestParam String key) {
    //         s3Service.deleteImage(key);
    //         return ApiResponse.onSuccess(null);
    // }
}

