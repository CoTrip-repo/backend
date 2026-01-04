package com.ssafy.cotrip.api.user.repository;

import com.ssafy.cotrip.domain.OAuth;

public interface OAuthMapper {
    OAuth findByProviderAndProviderId(String provider, String providerId);

    void insertOAuth(OAuth oauth);

    boolean existsProviderId(String sub);

    void deleteByUserId(Long userId);
}
