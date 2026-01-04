package com.ssafy.cotrip.global.util;

import lombok.Builder;

import java.util.List;

@Builder
public record SliceResponse<T, C>(
        List<T> content,
        boolean hasNext,
        C nextCursor
) {
}
