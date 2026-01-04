-- 마이그레이션 스크립트 시작
-- 기존 스키마: ssafy_trip
-- 신규 스키마: cotrip

USE cotrip;

-- 현재 설정 값 백업
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;

-- FK 체크 끄기
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 attractions만 먼저 비우기 (안전용)
TRUNCATE TABLE attractions;
   
SET SESSION net_read_timeout = 600;
SET SESSION net_write_timeout = 600;
SET SESSION innodb_lock_wait_timeout = 600;

------------------------------------------------
-- 0. 신규 테이블 초기화
--    다른 데이터가 전혀 없다고 했으니 통째로 비워도 됨
------------------------------------------------
TRUNCATE TABLE attractions;
TRUNCATE TABLE guguns;
TRUNCATE TABLE sidos;
TRUNCATE TABLE content_types;

------------------------------------------------
-- 1. Sidos 마이그레이션
--    ssafy_trip.sidos.sido_code / sido_name -> cotrip.sidos
--    id는 AUTO_INCREMENT 로 새로 생성
------------------------------------------------
INSERT INTO cotrip.sidos (sido_code, sido_name)
SELECT DISTINCT
       s.sido_code,
       s.sido_name
FROM ssafy_trip.sidos s
ORDER BY s.sido_code;

------------------------------------------------
-- 2. Content Type 마이그레이션
--    ssafy_trip.contenttypes.content_type_name -> cotrip.content_type.name
--    id는 AUTO_INCREMENT 로 새로 생성
------------------------------------------------
INSERT INTO cotrip.content_types (content_type_name)
SELECT DISTINCT
       ct.content_type_name
FROM ssafy_trip.contenttypes ct;

------------------------------------------------
-- 3. Guguns 마이그레이션
--    ssafy_trip.guguns:
--      - gugun_code, gugun_name 그대로 사용
--      - sido_code 로 ssafy_trip.sidos 를 찾고
--      - 그 sido_code 를 가진 cotrip.sidos.id 를 FK 로 사용
------------------------------------------------
INSERT INTO cotrip.guguns (gugun_code, gugun_name, sido_id)
SELECT DISTINCT
       g.gugun_code,
       g.gugun_name,
       s_new.id AS sido_id
FROM ssafy_trip.guguns g
JOIN ssafy_trip.sidos s_old
  ON g.sido_code = s_old.sido_code
JOIN cotrip.sidos s_new
  ON s_new.sido_code = s_old.sido_code
ORDER BY g.gugun_code;

------------------------------------------------
-- 4. Attractions 마이그레이션 (프로시저 + 배치 처리)
------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS migrate_attractions//
CREATE PROCEDURE migrate_attractions()
BEGIN
    DECLARE v_min INT;
    DECLARE v_max INT;
    DECLARE v_from INT;
    DECLARE v_to INT;
    DECLARE v_chunk INT DEFAULT 1000;

    SELECT MIN(no), MAX(no) INTO v_min, v_max
    FROM ssafy_trip.attractions;

    IF v_min IS NOT NULL AND v_max IS NOT NULL THEN
        SET v_from = v_min;

        WHILE v_from <= v_max DO
            SET v_to = v_from + v_chunk - 1;

            INSERT INTO cotrip.attractions (
                content_id,
                title,
                image1,
                image2,
                map_level,
                latitude,
                longitude,
                tel,
                addr1,
                addr2,
                homepage,
                overview,
                content_type_id,
                gugun_id,
                sido_id
            )
            SELECT 
                a.content_id,
                a.title,
                a.first_image1,
                a.first_image2,
                a.map_level,
                a.latitude,
                a.longitude,
                a.tel,
                a.addr1,
                a.addr2,
                a.homepage,
                a.overview,
                ct_new.id,
                COALESCE(g_new.id, g_fallback.id) AS gugun_id,
                s_new.id AS sido_id
            FROM ssafy_trip.attractions a
            JOIN ssafy_trip.contenttypes ct_old
                ON a.content_type_id = ct_old.content_type_id
            JOIN cotrip.content_types ct_new
                ON ct_new.content_type_name = ct_old.content_type_name
            JOIN ssafy_trip.sidos s_old
                ON a.area_code = s_old.sido_code
            JOIN cotrip.sidos s_new
                ON s_new.sido_code = s_old.sido_code

            -- 구군 매핑
            LEFT JOIN ssafy_trip.guguns g_old
                ON a.si_gun_gu_code = g_old.gugun_code
                AND g_old.sido_code = s_old.sido_code

            LEFT JOIN cotrip.guguns g_new
                ON g_new.gugun_code = g_old.gugun_code
                AND g_new.sido_id = s_new.id

            LEFT JOIN cotrip.guguns g_fallback
                ON g_fallback.sido_id = s_new.id
                AND g_fallback.gugun_code = -1

            WHERE a.no BETWEEN v_from AND v_to
              AND a.si_gun_gu_code IS NOT NULL      -- NULL 제외
              AND a.si_gun_gu_code <> 0             -- 0 제외
            ;

            SET v_from = v_to + 1;
        END WHILE;
    END IF;
END//
DELIMITER ;

-- 프로시저 실행
CALL migrate_attractions();

-- (선택) 프로시저 정리
DROP PROCEDURE IF EXISTS migrate_attractions;

------------------------------------------------
-- FK 체크 원복
------------------------------------------------
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
