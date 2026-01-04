package com.ssafy.cotrip.api.hotplace.service;

import com.ssafy.cotrip.api.attraction.service.AttractionService;
import com.ssafy.cotrip.api.hotplace.dto.request.PostRequest;
import com.ssafy.cotrip.api.hotplace.dto.response.PostDto;
import com.ssafy.cotrip.api.hotplace.repository.HotplaceMapper;
import com.ssafy.cotrip.api.plan.dto.request.AddAttractionRequestDto;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.HotplaceHandler;
import com.ssafy.cotrip.domain.Post;
import com.ssafy.cotrip.global.util.SliceResponse;
import com.ssafy.cotrip.global.util.SliceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotplaceService {

    private final HotplaceMapper hotplaceMapper;
    private final SliceService sliceService;
    private final AttractionService attractionService;

    @Transactional
    public void createPost(Long userId, PostRequest request) {
        // attractionId와 kakaoPlaceData 중 하나는 반드시 있어야 함
        if (request.attractionId() == null && request.kakaoPlaceData() == null) {
            throw new HotplaceHandler(ErrorStatus._BAD_REQUEST, "attractionId 또는 kakaoPlaceData 중 하나는 필수입니다.");
        }

        Long finalAttractionId = request.attractionId();

        // 카카오 장소 데이터가 있으면 DB에 저장하거나 기존 ID 가져오기
        if (request.kakaoPlaceData() != null) {
            log.info("🔍 핫플레이스 등록: 카카오 장소 데이터로 Attraction 찾기/생성 시작");
            // PostRequest.KakaoPlaceData를 AddAttractionRequestDto.KakaoPlaceData로 변환
            PostRequest.KakaoPlaceData kakaoData = request.kakaoPlaceData();
            AddAttractionRequestDto.KakaoPlaceData attractionKakaoData = new AddAttractionRequestDto.KakaoPlaceData(
                    kakaoData.id(),
                    kakaoData.placeName(),
                    kakaoData.categoryName(),
                    kakaoData.phone(),
                    kakaoData.addressName(),
                    kakaoData.roadAddressName(),
                    kakaoData.x(),
                    kakaoData.y(),
                    kakaoData.placeUrl());
            finalAttractionId = attractionService.findOrCreateAttraction(attractionKakaoData);
        }

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .userId(userId)
                .attractionId(finalAttractionId)
                .build();

        hotplaceMapper.save(post);

        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            hotplaceMapper.saveImages(post.getId(), request.imageUrls());
        }
    }

    public SliceResponse<PostDto, Long> getMyPosts(Long userId, Long cursorId, int size) {
        List<PostDto> posts = hotplaceMapper.findByUserId(userId, cursorId, size + 1);
        return sliceService.toSliceResponse(posts, size, PostDto::id);
    }

    public SliceResponse<PostDto, Long> getRecentPosts(Long cursorId, int size) {
        List<PostDto> posts = hotplaceMapper.findRecentPosts(cursorId, size + 1);
        return sliceService.toSliceResponse(posts, size, PostDto::id);
    }

    public PostDto getPostDetail(Long postId) {
        PostDto postDto = hotplaceMapper.findPostById(postId);
        if (postDto == null) {
            throw new HotplaceHandler(ErrorStatus.POST_NOT_FOUND);
        }
        return postDto;
    }

    @Transactional
    public PostDto updatePost(Long userId, Long postId, PostRequest request) {
        PostDto postDto = hotplaceMapper.findPostById(postId);
        if (postDto == null) {
            throw new HotplaceHandler(ErrorStatus.POST_NOT_FOUND);
        }
        if (!Objects.equals(postDto.userId(), userId)) {
            throw new HotplaceHandler(ErrorStatus.POST_AUTHORIZATION);
        }

        Long finalAttractionId = request.attractionId();

        // 카카오 장소 데이터가 있으면 DB에 저장하거나 기존 ID 가져오기
        if (request.kakaoPlaceData() != null) {
            log.info("🔍 핫플레이스 수정: 카카오 장소 데이터로 Attraction 찾기/생성 시작");
            // PostRequest.KakaoPlaceData를 AddAttractionRequestDto.KakaoPlaceData로 변환
            PostRequest.KakaoPlaceData kakaoData = request.kakaoPlaceData();
            AddAttractionRequestDto.KakaoPlaceData attractionKakaoData = new AddAttractionRequestDto.KakaoPlaceData(
                    kakaoData.id(),
                    kakaoData.placeName(),
                    kakaoData.categoryName(),
                    kakaoData.phone(),
                    kakaoData.addressName(),
                    kakaoData.roadAddressName(),
                    kakaoData.x(),
                    kakaoData.y(),
                    kakaoData.placeUrl());
            finalAttractionId = attractionService.findOrCreateAttraction(attractionKakaoData);
        }

        Post post = Post.builder()
                .id(postId)
                .title(request.title())
                .content(request.content())
                .userId(userId)
                .attractionId(finalAttractionId)
                .build();

        // 게시글 내용 수정
        hotplaceMapper.update(post);

        // 이미지 교체
        hotplaceMapper.updateImages(postId, request.imageUrls());

        return getPostDetail(postId);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        int deleted = hotplaceMapper.delete(userId, postId);
        if (deleted == 0) {
            // 존재하지 않거나, 작성자가 아님
            throw new HotplaceHandler(ErrorStatus.POST_AUTHORIZATION);
        }

        // 이미지 soft delete
        hotplaceMapper.softDeleteImages(postId);
    }
}
