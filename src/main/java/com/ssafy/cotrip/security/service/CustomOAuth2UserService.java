package com.ssafy.cotrip.security.service;

import com.ssafy.cotrip.api.user.repository.OAuthMapper;
import com.ssafy.cotrip.api.user.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{
    private final UserMapper userMapper;
    private final OAuthMapper  oAuthMapper;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Object sub = oAuth2User.getAttributes().get("sub");
        Object email = oAuth2User.getAttributes().get("email");

        if (!oAuthMapper.existsProviderId(sub.toString()) && userMapper.existsEmail(email.toString())) {

            throw new OAuth2AuthenticationException(
                    new OAuth2Error("EMAIL_DUPLICATION"),
                    "이미 자체 로그인으로 가입된 이메일입니다."
            );
        }

        log.info("registrationId={}, email={}, sub={}", registrationId, email, sub);
        return oAuth2User;
    }
}
