package com.ssafy.cotrip.api.attraction.repository;

import com.ssafy.cotrip.api.attraction.dto.AttractionCondition;
import com.ssafy.cotrip.api.attraction.dto.response.AttractionDto;
import com.ssafy.cotrip.domain.Attraction;
import com.ssafy.cotrip.domain.ContentType;
import com.ssafy.cotrip.domain.Gugun;
import com.ssafy.cotrip.domain.Sido;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AttractionMapper {

    List<AttractionDto> searchAttractions(AttractionCondition condition);

    List<ContentType> findAllContentTypes();

    List<Sido> findAllSidos();

    List<Gugun> findAllGugunsBySidoId(Long sidoId);

    // 주소 기반 코드 조회
    Integer findSidoCodeByName(String name);

    Integer findGugunCodeByName(@Param("sidoCode") Integer sidoCode, @Param("name") String name);

    List<AttractionDto> findAllByName(@Param("name") String name,
            @Param("cursorId") Long cursorId,
            @Param("size") int size);

    // Kakao 장소 관련
    Attraction findByKakaoId(String kakaoId);

    Attraction findByRoadAddress(String roadAddress);

    void insertKakaoAttraction(Attraction attraction);

    // AI 카테고리 업데이트
    void updateContentType(@Param("attractionId") Long attractionId, @Param("contentTypeId") Long contentTypeId);

}
