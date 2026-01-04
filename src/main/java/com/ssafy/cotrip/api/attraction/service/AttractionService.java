package com.ssafy.cotrip.api.attraction.service;

import com.ssafy.cotrip.api.ai.service.AiService;
import com.ssafy.cotrip.api.attraction.dto.AttractionCondition;
import com.ssafy.cotrip.api.attraction.dto.response.AttractionDto;
import com.ssafy.cotrip.api.attraction.dto.response.AttractionOptionListResponse;
import com.ssafy.cotrip.api.attraction.repository.AttractionMapper;
import com.ssafy.cotrip.api.plan.dto.request.AddAttractionRequestDto;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.AttractionHandler;
import com.ssafy.cotrip.domain.Attraction;
import com.ssafy.cotrip.domain.ContentType;
import com.ssafy.cotrip.domain.Gugun;
import com.ssafy.cotrip.domain.Sido;
import com.ssafy.cotrip.global.util.SliceResponse;
import com.ssafy.cotrip.global.util.SliceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttractionService {

    private static final int DEFAULT_PAGE = 20;

    private final AttractionMapper attractionMapper;
    private final SliceService sliceService;
    private final AiService aiService;

    @Transactional(readOnly = true)
    public SliceResponse<AttractionDto, Long> searchAttractions(
            Long contentTypeId,
            Long sidoId,
            Long gugunId,
            String keyword,
            Long cursorId,
            Integer size) {
        if (sidoId == null && gugunId != null) {
            throw new AttractionHandler(
                    ErrorStatus._BAD_REQUEST,
                    "시/도 선택없이 구/군만 선택할 수 없습니다");
        }

        if (cursorId != null && cursorId <= 0) {
            throw new AttractionHandler(
                    ErrorStatus._BAD_REQUEST,
                    "cursorId는 0보다 큰 값이어야 합니다");
        }

        int pageSize = Objects.requireNonNullElse(size, DEFAULT_PAGE);
        int fetchSize = pageSize + 1;

        List<AttractionDto> attractions = getAttractions(
                contentTypeId, sidoId, gugunId, keyword, cursorId, fetchSize);

        // 공통 SliceService 사용
        return sliceService.toSliceResponse(attractions, pageSize, AttractionDto::id);
    }

    private List<AttractionDto> getAttractions(
            Long contentTypeId,
            Long sidoId,
            Long gugunId,
            String keyword,
            Long cursorId,
            Integer size) {
        AttractionCondition condition = AttractionCondition.builder()
                .contentTypeId(contentTypeId)
                .sidoId(sidoId)
                .gugunId(gugunId)
                .keyword(keyword)
                .cursorId(cursorId)
                .size(size)
                .build();

        return attractionMapper.searchAttractions(condition);
    }

    @Cacheable(value = "contentTypes")
    @Transactional(readOnly = true)
    public AttractionOptionListResponse getContentTypeList() {
        log.info("DB에서 ContentType 목록 조회 (캐시 미스)");
        List<ContentType> contentTypes = attractionMapper.findAllContentTypes();

        AttractionOptionListResponse response = AttractionOptionListResponse.builder().build();
        for (ContentType contentType : contentTypes) {
            response.addContentType(contentType);
            System.out.println(contentType.getContentTypeName());
        }

        return response;
    }

    @Cacheable(value = "sidos")
    @Transactional(readOnly = true)
    public AttractionOptionListResponse getSidoList() {
        log.info("DB에서 시/도 목록 조회 (캐시 미스)");
        List<Sido> sidos = attractionMapper.findAllSidos();

        AttractionOptionListResponse response = AttractionOptionListResponse.builder().build();
        for (Sido sido : sidos) {
            response.addSido(sido);
        }

        return response;
    }

    @Cacheable(value = "guguns", key = "#sidoId")
    @Transactional(readOnly = true)
    public AttractionOptionListResponse getGugunList(Long sidoId) {
        log.info("DB에서 구/군 목록 조회 - sidoId: {} (캐시 미스)", sidoId);
        List<Gugun> guguns = attractionMapper.findAllGugunsBySidoId(sidoId);

        AttractionOptionListResponse response = AttractionOptionListResponse.builder().build();
        for (Gugun gugun : guguns) {
            response.addGugun(gugun);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public SliceResponse<AttractionDto, Long> findAttractionByName(String name, Long cursorId, Integer size) {
        if (cursorId != null && cursorId <= 0) {
            throw new AttractionHandler(
                    ErrorStatus._BAD_REQUEST,
                    "cursorId는 0보다 큰 값이어야 합니다");
        }

        int pageSize = Objects.requireNonNullElse(size, DEFAULT_PAGE);
        int fetchSize = pageSize + 1;

        List<AttractionDto> attractions = attractionMapper.findAllByName(name, cursorId, fetchSize);

        return sliceService.toSliceResponse(attractions, pageSize, AttractionDto::id);
    }

    /**
     * 카카오 장소 데이터로 Attraction 찾기 또는 생성
     * - kakaoId로 DB 조회
     * - 있으면 기존 ID 반환
     * - 없으면 새로 저장 후 ID 반환
     */
    @Transactional
    public Long findOrCreateAttraction(AddAttractionRequestDto.KakaoPlaceData kakaoPlaceData) {
        // 1. 카카오 ID로 기존 장소 조회
        Attraction existing = attractionMapper.findByKakaoId(kakaoPlaceData.id());

        if (existing != null) {
            log.info("기존 Kakao ID로 찾음: attractionId={}", existing.getId());
            return existing.getId();
        }

        // 2. 도로명 주소로 기존 장소 조회 (중복 방지)
        // 주소와 장소명이 완전히 일치해야 동일 장소로 판단
        if (kakaoPlaceData.roadAddressName() != null && !kakaoPlaceData.roadAddressName().isEmpty()) {
            Attraction existingByAddress = attractionMapper.findByRoadAddress(kakaoPlaceData.roadAddressName());
            if (existingByAddress != null) {
                String existingName = existingByAddress.getTitle();
                String newName = kakaoPlaceData.placeName();

                // 이름이 완전히 일치하면 같은 장소로 판단
                if (existingName.equals(newName)) {
                    log.info("기존 도로명 주소 + 이름 완전 일치로 찾음: attractionId={}, roadAddress={}, name={}",
                            existingByAddress.getId(), kakaoPlaceData.roadAddressName(), existingName);
                    return existingByAddress.getId();
                } else {
                    log.info("도로명 주소 같지만 이름 불일치: existing='{}', new='{}'",
                            existingName, newName);
                    // 다른 장소로 간주하고 새로 생성
                }
            }
        }

        // 3. 없으면 새로 생성
        log.info("새로운 Attraction 생성: kakaoId={}, placeName={}",
                kakaoPlaceData.id(), kakaoPlaceData.placeName());
        Double latitude = null;
        Double longitude = null;

        // 좌표 변환
        if (kakaoPlaceData.y() != null && kakaoPlaceData.x() != null) {
            try {
                latitude = Double.parseDouble(kakaoPlaceData.y());
                longitude = Double.parseDouble(kakaoPlaceData.x());
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinates for Kakao place: {}", kakaoPlaceData.id());
            }
        }

        // 주소 파싱하여 시도/구군 코드 매핑
        Long sidoId = 1L; // 기본값
        Long gugunId = 1L; // 기본값

        if (kakaoPlaceData.addressName() != null && !kakaoPlaceData.addressName().isEmpty()) {
            String[] tokens = kakaoPlaceData.addressName().split(" ");
            if (tokens.length >= 1) {
                String sidoName = tokens[0];
                // 현재 Mapper XML이 #{name}% LIKE 검색이므로 "경기" -> "경기도" 매칭됨
                Integer foundSido = attractionMapper.findSidoCodeByName(sidoName);

                if (foundSido != null) {
                    sidoId = Long.valueOf(foundSido);

                    if (tokens.length >= 2) {
                        String gugunName = tokens[1];
                        Integer foundGugun = attractionMapper.findGugunCodeByName(foundSido, gugunName);
                        if (foundGugun != null) {
                            gugunId = Long.valueOf(foundGugun);
                        }
                    }
                }
            }
        }

        Attraction newAttraction = Attraction.builder()
                .kakaoId(kakaoPlaceData.id())
                .title(kakaoPlaceData.placeName())
                .tel(kakaoPlaceData.phone())
                .addr1(kakaoPlaceData.roadAddressName()) // 도로명 주소를 addr1에 저장
                .addr2(kakaoPlaceData.addressName()) // 지번 주소를 addr2에 저장
                .latitude(latitude)
                .longitude(longitude)
                .sidoId(sidoId)
                .gugunId(gugunId)
                .contentTypeId(12L) // 기본: 관광지 (AI 분류 대기 중)
                .build();

        attractionMapper.insertKakaoAttraction(newAttraction);

        // 새 Attraction 처리: Vector DB 임베딩 + 카테고리 분류 (비동기, 통합 처리)
        aiService.processNewAttraction(newAttraction.getId());

        return newAttraction.getId();
    }
}
