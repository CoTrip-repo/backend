# =========================
# 1) Build stage (Maven)
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# 캐시 최적화: 의존성 정의 먼저 복사
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

# 소스 복사 후 패키징
COPY src ./src
RUN mvn -B -DskipTests package


# =========================
# 2) Runtime stage
# =========================
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul
WORKDIR /app

# 빌드 산출물 복사
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# JVM 옵션 설정
ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx448m"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]