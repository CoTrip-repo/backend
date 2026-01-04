package com.ssafy.cotrip.api.hotplace.controller;

import com.ssafy.cotrip.api.hotplace.dto.request.PostRequest;
import com.ssafy.cotrip.api.hotplace.dto.response.PostDto;
import com.ssafy.cotrip.api.hotplace.service.HotplaceService;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import com.ssafy.cotrip.global.util.SliceResponse;
import com.ssafy.cotrip.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HotplaceController {

    private final HotplaceService hotplaceService;

    @PostMapping("/v1/posts")
    public ApiResponse<Void> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostRequest request
    ) {
        Long userId = userDetails.getId();
        hotplaceService.createPost(userId, request);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/v1/posts/me")
    public ApiResponse<SliceResponse<PostDto, Long>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size
    ) {
        SliceResponse<PostDto, Long> response = hotplaceService.getMyPosts(userDetails.getId(), cursorId, size);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/v1/posts/recent")
    public ApiResponse<SliceResponse<PostDto, Long>> recentPosts(
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", required = false, defaultValue = "3") int size
    ) {
        SliceResponse<PostDto, Long> response = hotplaceService.getRecentPosts(cursorId, size);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/v1/posts/{postId}")
    public ApiResponse<PostDto> getPostDetail(
            @PathVariable Long postId
    ) {
        PostDto response = hotplaceService.getPostDetail(postId);
        return ApiResponse.onSuccess(response);
    }

    @PutMapping("/v1/posts/{postId}")
    public ApiResponse<PostDto> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody PostRequest request
    ) {
        Long userId = userDetails.getId();
        PostDto response = hotplaceService.updatePost(userId, postId, request);
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/v1/posts/{postId}")
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        Long userId = userDetails.getId();
        hotplaceService.deletePost(userId, postId);
        return ApiResponse.onSuccess(null);
    }
}
