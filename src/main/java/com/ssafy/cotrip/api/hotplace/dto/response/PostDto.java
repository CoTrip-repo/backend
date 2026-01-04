package com.ssafy.cotrip.api.hotplace.dto.response;

import java.time.LocalDate;
import java.util.List;

public record PostDto(
        Long id,
        LocalDate createdAt,
        String title,
        String content,
        Long attractionId,
        String attractionName,
        String attractionAddress,
        Long userId,
        String userName,
        List<String> imageUrls
) {
}
