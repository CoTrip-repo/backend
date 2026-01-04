package com.ssafy.cotrip.domain;

import com.ssafy.cotrip.domain.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String role;
    private String loginType; // SYSTEM or GOOGLE
}
