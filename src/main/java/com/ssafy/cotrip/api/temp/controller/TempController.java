package com.ssafy.cotrip.api.temp.controller;

import com.ssafy.cotrip.api.temp.dto.response.TempTestResponse;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import com.ssafy.cotrip.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TempController {
    @GetMapping("/test")
    public ApiResponse<TempTestResponse> testAPI() {
        log.info("tempAPI");
        TempTestResponse response = new TempTestResponse();
        response.setResponse("hello");
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/session")
    public ApiResponse<Long> sessionTest(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.onSuccess(userDetails.getId());
    }

}
