-- Safe Update Mode 비활성화
SET SQL_SAFE_UPDATES = 0;
-- ================================
-- 공간 인덱스 추가 마이그레이션
-- attractions 테이블에 POINT 컬럼 및 SPATIAL INDEX 추가
-- ================================

USE cotrip;

-- 1. POINT 컬럼 추가 (SRID 4326 = WGS84)
ALTER TABLE attractions
ADD COLUMN location POINT SRID 4326 NULL;

-- 2. 기존 latitude/longitude로 POINT 생성
UPDATE attractions
SET location = ST_GeomFromText(
        CONCAT('POINT(', latitude, ' ', longitude, ')'),
        4326
               )
WHERE latitude IS NOT NULL
  AND longitude IS NOT NULL
  AND deleted_at IS NULL;
-- 3. NULL인 행에 기본값 설정
UPDATE attractions
SET location = ST_GeomFromText('POINT(90.0 0.0)', 4326)
WHERE location IS NULL;
-- Safe Update Mode 다시 활성화
SET SQL_SAFE_UPDATES = 1;
-- 4. 컬럼을 NOT NULL로 변경
ALTER TABLE attractions
    MODIFY COLUMN location POINT NOT NULL SRID 4326;
-- 5. SPATIAL INDEX 생성
CREATE SPATIAL INDEX idx_attractions_location ON attractions(location);

-- (추가) expense의 group_id
ALTER TABLE expense
ADD COLUMN group_id VARCHAR(36) NOT NULL COMMENT '지출 그룹 식별자(동일 지출 1건 묶기)'
AFTER planday_id;