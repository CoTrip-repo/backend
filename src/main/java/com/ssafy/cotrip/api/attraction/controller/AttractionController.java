package com.ssafy.cotrip.api.attraction.controller;

import com.ssafy.cotrip.api.attraction.dto.response.AttractionDto;
import com.ssafy.cotrip.api.attraction.dto.response.AttractionOptionListResponse;
import com.ssafy.cotrip.api.attraction.service.AttractionService;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import com.ssafy.cotrip.global.util.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @GetMapping("/v1/attractions")
    public ApiResponse<SliceResponse<AttractionDto, Long>> searchAttractions(
            @RequestParam(required = false) Long contentTypeId,
            @RequestParam(required = false) Long sidoId,
            @RequestParam(required = false) Long gugunId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Integer size
    ) {
        SliceResponse<AttractionDto, Long> response = attractionService.searchAttractions(contentTypeId, sidoId, gugunId, keyword, cursorId, size);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/v1/attractions/search")
    public ApiResponse<SliceResponse<AttractionDto, Long>> searchAttractionsByName(
            @RequestParam String name,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) Integer size
    ) {
        SliceResponse<AttractionDto, Long> response = attractionService.findAttractionByName(name, cursorId, size);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/v1/attractions/content-type")
    public ApiResponse<AttractionOptionListResponse> getContentTypeList() {
        AttractionOptionListResponse response = attractionService.getContentTypeList();
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/v1/attractions/sido")
    public ApiResponse<AttractionOptionListResponse> getRegionList() {
        AttractionOptionListResponse response = attractionService.getSidoList();
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/v1/attractions/gugun")
    public ApiResponse<AttractionOptionListResponse> getGugunList(@RequestParam Long sidoId) {
        AttractionOptionListResponse response = attractionService.getGugunList(sidoId);
        return ApiResponse.onSuccess(response);
    }

}
