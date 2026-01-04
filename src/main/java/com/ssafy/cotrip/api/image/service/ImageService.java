package com.ssafy.cotrip.api.image.service;

import com.ssafy.cotrip.api.image.dto.response.LocalImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/files}")
    private String urlPrefix;

    public List<LocalImageUploadResponse> uploadImages(List<MultipartFile> files) throws IOException {

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        List<LocalImageUploadResponse> result = new ArrayList<>();

        // 업로드 루트 경로를 절대 경로로 변경 후 normalize
        Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // uploadDir이 존재하지 않으면 생성
        if (!Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            // 이미지 확장자 체크
            String contentType = file.getContentType();
            if (contentType == null ||
                    !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
                throw new IllegalArgumentException("jpeg, jpg, png 이미지만 업로드 가능합니다.");
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }

            String datePath = LocalDate.now().toString();
            String savedFileName = UUID.randomUUID() + ext;

            // 날짜별 폴더 생성 (uploadDir/YYYY-MM-DD/)
            Path dirPath = rootPath.resolve(datePath);
            Files.createDirectories(dirPath);

            // 최종 파일 절대 경로 생성
            Path filePath = dirPath.resolve(savedFileName);

            // 파일 저장
            file.transferTo(filePath.toFile());

            String urlPath = String.format("%s/%s/%s", urlPrefix, datePath, savedFileName);

            result.add(new LocalImageUploadResponse(originalName, urlPath));
        }

        return result;
    }
}
