package com.ssafy.cotrip.api.attraction.dto.response;

import com.ssafy.cotrip.domain.ContentType;
import com.ssafy.cotrip.domain.Gugun;
import com.ssafy.cotrip.domain.Sido;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public record AttractionOptionListResponse(
        List<RegionDto> data
) {
    public AttractionOptionListResponse {
        if (data == null) {
            data = new ArrayList<>();
        }
    }

    public void addContentType(ContentType contentType) {
        data.add(
                RegionDto.builder()
                        .id(contentType.getId())
                        .name(contentType.getContentTypeName())
                        .build()
        );
    }

    public void addSido(Sido sido) {
        data.add(
                RegionDto.builder()
                        .id(sido.getId())
                        .name(sido.getSidoName())
                        .build()
        );
    }

    public void addGugun(Gugun gugun) {
        data.add(
                RegionDto.builder()
                        .id(gugun.getId())
                        .name(gugun.getGugunName())
                        .build()
        );
    }

    @Builder
    public record RegionDto(
            Long id,
            String name
    ) { }

}

