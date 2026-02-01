package com.ssafy.cotrip.api.test.controller;

import com.ssafy.cotrip.security.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestTokenController {

    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/token")
    public Map<String, String> generateTestToken(@RequestParam(defaultValue = "1") Long userId) {
        String token = jwtTokenUtil.generateToken(userId, "test@test.com");
        return Map.of("accessToken", token);
    }
}
