-- ================================
-- DB 초기화
-- ================================
DROP DATABASE IF EXISTS cotrip;
CREATE DATABASE cotrip
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE cotrip;

SET FOREIGN_KEY_CHECKS = 0;

-- ================================
-- USERS
-- ================================
CREATE TABLE users (
    id        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email     VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(1000) NULL,
    nickname  VARCHAR(100) NOT NULL,
    role      VARCHAR(45) NOT NULL DEFAULT 'USER',
    loginType VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- OAUTH
-- ================================
CREATE TABLE oauth (
    user_id      BIGINT UNSIGNED PRIMARY KEY,
    provider     VARCHAR(45) NOT NULL,
    provider_id  VARCHAR(300) NOT NULL,
    UNIQUE (provider, provider_id),
    FOREIGN KEY (user_id) REFERENCES users(id),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- CONTENT TYPES
-- ================================
CREATE TABLE content_types (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    content_type_name VARCHAR(100) NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- SIDO
-- ================================
CREATE TABLE sidos (
    id        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    sido_code INT NOT NULL,
    sido_name VARCHAR(25) NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- GUGUN
-- ================================
CREATE TABLE guguns (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    sido_id    BIGINT UNSIGNED NOT NULL,
    gugun_code INT NOT NULL,
    gugun_name VARCHAR(25) NULL,
    FOREIGN KEY (sido_id) REFERENCES sidos(id),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- ATTRACTIONS
-- ================================
CREATE TABLE attractions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    content_id    INT NULL,
    title         VARCHAR(300) NULL,
    image1        VARCHAR(1000) NULL,
    image2        VARCHAR(1000) NULL,
    map_level     INT NULL,
    latitude      DECIMAL(20,17) NULL,
    longitude     DECIMAL(20,17) NULL,
    tel           VARCHAR(20) NULL,
    addr1         VARCHAR(100) NULL,
    addr2         VARCHAR(100) NULL,
    homepage      VARCHAR(1000) NULL,
    overview      VARCHAR(10000) NULL,

    content_type_id BIGINT UNSIGNED NOT NULL,
    gugun_id        BIGINT UNSIGNED NOT NULL,
    sido_id         BIGINT UNSIGNED NOT NULL,

    FOREIGN KEY (content_type_id) REFERENCES content_types(id),
    FOREIGN KEY (gugun_id) REFERENCES guguns(id),
    FOREIGN KEY (sido_id) REFERENCES sidos(id),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL,
    
    kakao_id      VARCHAR(50) DEFAULT NULL COMMENT 'Kakao Place ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- POSTS
-- ================================
CREATE TABLE posts (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(100) NOT NULL,
    content       VARCHAR(1000) NULL,
    user_id       BIGINT UNSIGNED NOT NULL,
    attraction_id BIGINT UNSIGNED NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (attraction_id) REFERENCES attractions(id),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- POST IMAGES
-- ================================
CREATE TABLE post_images (
    post_id  BIGINT UNSIGNED NOT NULL,
    idx      INT UNSIGNED NOT NULL,
    url      VARCHAR(2000) NOT NULL,

    PRIMARY KEY (post_id, idx),
    FOREIGN KEY (post_id) REFERENCES posts(id),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- PLANS
-- ================================
CREATE TABLE plans (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(15) NOT NULL UNIQUE,
    title      VARCHAR(300) NOT NULL,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    budget INT UNSIGNED NULL,
    leader_id  BIGINT UNSIGNED NOT NULL,

    FOREIGN KEY (leader_id) REFERENCES users(id),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- PLAN PARTICIPANTS
-- ================================
CREATE TABLE plan_participants (
    id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,

    UNIQUE (plan_id, user_id),
    FOREIGN KEY (plan_id) REFERENCES plans(id),
    FOREIGN KEY (user_id) REFERENCES users(id),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- PLANDAYS
-- ================================
CREATE TABLE plandays (
    id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    day     INT NOT NULL,
    date    VARCHAR(45) NOT NULL,
    plan_id BIGINT UNSIGNED NOT NULL,

    FOREIGN KEY (plan_id) REFERENCES plans(id),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- PLANDAY_ATTRACTIONS
-- ================================
CREATE TABLE planday_attractions (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    planday_id    BIGINT UNSIGNED NOT NULL,
    attraction_id BIGINT UNSIGNED NOT NULL,
    time          VARCHAR(50) NOT NULL,
    content       VARCHAR(1000) NULL,

    FOREIGN KEY (planday_id) REFERENCES plandays(id),
    FOREIGN KEY (attraction_id) REFERENCES attractions(id),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ================================
-- EXPENSE
-- ================================
CREATE TABLE expense (
     id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
     plan_id    BIGINT UNSIGNED NOT NULL,
     user_id    BIGINT UNSIGNED NOT NULL,
     planday_id BIGINT UNSIGNED NOT NULL,

     amount      INT NOT NULL,           -- 금액
     category    VARCHAR(20) NOT NULL,   -- 카테고리
     description VARCHAR(200) NULL,      -- 설명

     FOREIGN KEY (plan_id) REFERENCES plans(id),
     FOREIGN KEY (user_id) REFERENCES users(id),
     FOREIGN KEY (planday_id) REFERENCES plandays(id),

     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
     deleted_at DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;