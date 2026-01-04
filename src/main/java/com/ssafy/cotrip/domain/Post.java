package com.ssafy.cotrip.domain;

import com.ssafy.cotrip.domain.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {
    private Long id;
    private String title;
    private String content;
    private Long userId;       // FK -> users.id
    private Long attractionId; // FK -> attractions.id
}
