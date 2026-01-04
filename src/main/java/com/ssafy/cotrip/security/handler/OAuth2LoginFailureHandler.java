package com.ssafy.cotrip.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 로그인 실패", exception);

        ErrorStatus errorStatus = ErrorStatus.OAUTH_LOGIN_FAIL;

        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            String errorCode = oauthEx.getError().getErrorCode();

            if ("EMAIL_DUPLICATION".equals(errorCode)) {
                errorStatus = ErrorStatus.EXISTED_EMAIL;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> apiResponse =
                ApiResponse.onFailure(
                        errorStatus.getCode(),
                        errorStatus.getMessage(),
                        null
                );

        response.getWriter().write(
                objectMapper.writeValueAsString(apiResponse)
        );
    }
}
