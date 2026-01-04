package com.ssafy.cotrip.security.handler;

import com.ssafy.cotrip.api.user.repository.OAuthMapper;
import com.ssafy.cotrip.api.user.repository.UserMapper;
import com.ssafy.cotrip.domain.OAuth;
import com.ssafy.cotrip.domain.User;
import com.ssafy.cotrip.security.jwt.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserMapper userMapper;
    private final OAuthMapper oAuthMapper;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        log.info("OAuth2LoginSuccessHandler 진입");

        String provider = authToken.getAuthorizedClientRegistrationId();
        String providerId = oAuth2User.getAttributes().get("sub").toString();
        String email = oAuth2User.getAttributes().get("email").toString();

        OAuth oAuth = oAuthMapper.findByProviderAndProviderId(provider, providerId);

        Long userId;

        if (oAuth == null) {
            log.info("=====회원정보 저장 시작=====");
            String nickname = "user_" + UUID.randomUUID().toString().substring(0, 8);

            User newUser = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .loginType("GOOGLE")
                    .build();

            userMapper.insertUser(newUser);
            userId = newUser.getId();

            OAuth newOAuth = OAuth.builder()
                    .providerId(providerId)
                    .provider(provider)
                    .userId(userId)
                    .build();

            oAuthMapper.insertOAuth(newOAuth);
        } else {
            log.info("=======회원정보 저장되어있음========");
            userId = oAuth.getUserId();
        }

        String accessToken = jwtTokenUtil.generateToken(userId, email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(userId, email);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/api/v1/auth");
        refreshCookie.setMaxAge((int) jwtTokenUtil.getRemainingTime(refreshToken));
        response.addCookie(refreshCookie);

        String redirectUrl = String.format(
                "%s/auth/callback#accessToken=%s",
                frontendBaseUrl,
                accessToken
        );

        response.sendRedirect(redirectUrl);
    }
}
