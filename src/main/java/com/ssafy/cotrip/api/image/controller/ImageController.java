package com.ssafy.cotrip.api.image.controller;

import com.ssafy.cotrip.api.image.service.ImageService;
import com.ssafy.cotrip.api.image.dto.response.LocalImageUploadResponse;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/v1/uploads/images")
    public ApiResponse<List<LocalImageUploadResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        List<LocalImageUploadResponse> result = imageService.uploadImages(files);
        return ApiResponse.onSuccess(result);
    }
}
