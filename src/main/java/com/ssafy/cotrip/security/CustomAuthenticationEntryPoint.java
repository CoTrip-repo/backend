package com.ssafy.cotrip.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {


        log.info("JwtAuthenticationEntryPoint 진입");

        String exception = (String) request.getAttribute("jwt_exception");

        ErrorStatus status;
        if ("EXPIRED".equals(exception)) {
            status = ErrorStatus.EXPIRED_ACCESS_TOKEN;
        } else {
            status = ErrorStatus.INVALID_TOKEN;
        }

        ApiResponse<Void> apiResponse =
                ApiResponse.onFailure(status.getCode(), status.getMessage(), null);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }
}
