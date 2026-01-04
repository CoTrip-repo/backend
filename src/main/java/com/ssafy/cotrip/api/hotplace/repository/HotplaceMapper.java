package com.ssafy.cotrip.api.hotplace.repository;

import com.ssafy.cotrip.api.hotplace.dto.response.PostDto;
import com.ssafy.cotrip.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HotplaceMapper {

    // =============================
    // 조회
    // =============================

    // 최신 게시글 조회 (cursor 기반)
    List<PostDto> findRecentPosts(@Param("cursorId") Long cursorId,
                                  @Param("size") int size);

    // 게시글 단건 조회
    PostDto findPostById(@Param("postId") Long postId);

    // 나의 게시글 조회
    List<PostDto> findByUserId(@Param("userId") Long userId,
                         @Param("cursorId") Long cursorId,
                         @Param("size") int size);


    // =============================
    // 저장
    // =============================

    // 게시글 저장
    void save(Post post);

    // 게시글 이미지 저장
    void saveImages(@Param("postId") Long postId,
                    @Param("imageUrls") List<String> imageUrls);


    // =============================
    // 수정
    // =============================

    // 게시글 수정
    void update(@Param("post") Post post);

    // 기존 이미지 전체 *hard* delete (update 시 사용)
    void hardDeleteImages(@Param("postId") Long postId);

    default void updateImages(Long postId, List<String> imageUrls) {
        hardDeleteImages(postId);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            saveImages(postId, imageUrls);
        }
    }


    // =============================
    // 삭제 (soft delete)
    // =============================

    /**
     * 게시글 soft delete (작성자 본인만)
     * @return 변경된 row count (0이면 삭제 실패 → 권한 없음 or 없음)
     */
    int delete(@Param("userId") Long userId,
               @Param("postId") Long postId);


    // 기존 이미지 전체 *soft* delete (post 삭제 시 사용)
    void softDeleteImages(@Param("postId") Long postId);

}
