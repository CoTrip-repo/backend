package com.ssafy.cotrip.api.plan.service;

import com.ssafy.cotrip.api.attraction.service.AttractionService;
import com.ssafy.cotrip.api.plan.dto.request.AddAttractionRequestDto;
import com.ssafy.cotrip.api.plan.dto.request.UpdateAttractionContentRequestDto;
import com.ssafy.cotrip.api.plan.dto.request.UpdateAttractionTimeRequestDto;
import com.ssafy.cotrip.api.plan.dto.response.PlanDayAttractionResponseDto;
import com.ssafy.cotrip.api.plan.repository.PlanDayAttractionMapper;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.PlanHandler;
import com.ssafy.cotrip.domain.PlanDayAttraction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanDayAttractionService {
    private final PlanDayAttractionMapper planDayAttractionMapper;
    private final AttractionService attractionService;

    @Transactional
    public PlanDayAttractionResponseDto addAttraction(AddAttractionRequestDto request) {
        // 일차별 최대 30개 제한 확인
        int currentCount = planDayAttractionMapper.countByPlanDayId(request.planDayId());
        if (currentCount >= 30) {
            throw new PlanHandler(ErrorStatus._BAD_REQUEST, "하루에 최대 30개의 관광지만 등록할 수 있습니다.");
        }

        Long finalAttractionId = request.attractionId();

        // 카카오 장소 데이터가 있으면 DB에 저장하거나 기존 ID 가져오기
        if (request.kakaoPlaceData() != null) {
            finalAttractionId = attractionService.findOrCreateAttraction(request.kakaoPlaceData());
        }

        PlanDayAttraction planDayAttraction = PlanDayAttraction.builder()
                .plandayId(request.planDayId())
                .attractionId(finalAttractionId)
                .time(request.time())
                .content(request.content())
                .build();

        planDayAttractionMapper.insert(planDayAttraction);

        PlanDayAttraction savedAttraction = planDayAttractionMapper.findById(planDayAttraction.getId());
        return convertToDto(savedAttraction);
    }

    @Transactional
    public PlanDayAttractionResponseDto updateTime(UpdateAttractionTimeRequestDto request) {
        PlanDayAttraction planDayAttraction = planDayAttractionMapper.findById(request.id());
        if (planDayAttraction == null) {
            throw new PlanHandler(ErrorStatus.POST_NOT_FOUND, "일정 항목을 찾을 수 없습니다.");
        }

        PlanDayAttraction updated = PlanDayAttraction.builder()
                .id(planDayAttraction.getId())
                .plandayId(planDayAttraction.getPlandayId())
                .attractionId(planDayAttraction.getAttractionId())
                .time(request.newTime())
                .content(planDayAttraction.getContent())
                .build();

        planDayAttractionMapper.update(updated);

        return convertToDto(planDayAttractionMapper.findById(request.id()));
    }

    @Transactional
    public PlanDayAttractionResponseDto updateContent(UpdateAttractionContentRequestDto request) {
        PlanDayAttraction planDayAttraction = planDayAttractionMapper.findById(request.id());
        if (planDayAttraction == null) {
            throw new PlanHandler(ErrorStatus.POST_NOT_FOUND, "일정 항목을 찾을 수 없습니다.");
        }

        PlanDayAttraction updated = PlanDayAttraction.builder()
                .id(planDayAttraction.getId())
                .plandayId(planDayAttraction.getPlandayId())
                .attractionId(planDayAttraction.getAttractionId())
                .time(planDayAttraction.getTime())
                .content(request.content())
                .build();

        planDayAttractionMapper.update(updated);

        return convertToDto(planDayAttractionMapper.findById(request.id()));
    }

    @Transactional
    public PlanDayAttractionResponseDto changeLocation(Long attractionId, Long newAttractionId,
            Map<String, Object> kakaoPlaceData) {

        PlanDayAttraction planDayAttraction = planDayAttractionMapper.findById(attractionId);
        if (planDayAttraction == null) {
            throw new PlanHandler(ErrorStatus.POST_NOT_FOUND, "일정 항목을 찾을 수 없습니다.");
        }

        // 카카오 장소 데이터가 있으면 DB에 저장하거나 기존 ID 가져오기
        Long finalAttractionId = newAttractionId;
        if (kakaoPlaceData != null) {
            // Map을 KakaoPlaceData DTO로 변환
            AddAttractionRequestDto.KakaoPlaceData kakaoData = new AddAttractionRequestDto.KakaoPlaceData(
                    (String) kakaoPlaceData.get("id"),
                    (String) kakaoPlaceData.get("placeName"),
                    (String) kakaoPlaceData.get("categoryName"),
                    (String) kakaoPlaceData.get("phone"),
                    (String) kakaoPlaceData.get("addressName"),
                    (String) kakaoPlaceData.get("roadAddressName"),
                    (String) kakaoPlaceData.get("x"),
                    (String) kakaoPlaceData.get("y"),
                    (String) kakaoPlaceData.get("placeUrl"));
            finalAttractionId = attractionService.findOrCreateAttraction(kakaoData);
        }

        PlanDayAttraction updated = PlanDayAttraction.builder()
                .id(planDayAttraction.getId())
                .plandayId(planDayAttraction.getPlandayId())
                .attractionId(finalAttractionId)
                .time(planDayAttraction.getTime())
                .content(planDayAttraction.getContent())
                .build();

        planDayAttractionMapper.update(updated);

        PlanDayAttractionResponseDto result = convertToDto(planDayAttractionMapper.findById(attractionId));

        return result;
    }

    @Transactional
    public void deleteAttraction(Long id) {
        PlanDayAttraction planDayAttraction = planDayAttractionMapper.findById(id);
        if (planDayAttraction == null) {
            throw new PlanHandler(ErrorStatus.POST_NOT_FOUND, "일정 항목을 찾을 수 없습니다.");
        }

        planDayAttractionMapper.delete(id);
    }

    public List<PlanDayAttractionResponseDto> getAttractionsByPlanDay(Long planDayId) {
        List<PlanDayAttraction> attractions = planDayAttractionMapper.findByPlanDayId(planDayId);
        return attractions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PlanDayAttractionResponseDto convertToDto(PlanDayAttraction planDayAttraction) {
        return PlanDayAttractionResponseDto.builder()
                .id(planDayAttraction.getId())
                .planDayId(planDayAttraction.getPlandayId())
                .attractionId(planDayAttraction.getAttractionId())
                .attractionTitle(planDayAttraction.getAttractionTitle())
                .attractionImage(null)
                .latitude(planDayAttraction.getLatitude())
                .longitude(planDayAttraction.getLongitude())
                .addr1(planDayAttraction.getAddr1())
                .time(planDayAttraction.getTime())
                .content(planDayAttraction.getContent())
                .createdAt(planDayAttraction.getCreatedAt())
                .updatedAt(planDayAttraction.getUpdatedAt())
                .build();
    }
}
