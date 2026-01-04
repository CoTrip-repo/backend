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
public class Attraction extends BaseEntity {
    private Long id;
    private Long contentId;
    private String title;
    private String image1;
    private String image2;
    private Integer mapLevel;
    private Double latitude;
    private Double longitude;
    private String tel;
    private String addr1;
    private String addr2;
    private String homepage;
    private String overview;
    private String kakaoId;

    private Long contentTypeId; // FK
    private Long gugunId; // FK
    private Long sidoId; // FK
}
