package com.ssafy.cotrip.api.user.repository;

import com.ssafy.cotrip.domain.User;

public interface UserMapper {
    User findByEmail(String email);

    User findById(Long id);

    void insertUser(User user);

    void deleteByUserId(Long userId);

    void updatePassword(String password, String email);

    void updateNickname(String nickname, String email);

    boolean existsNickname(String nickname);

    boolean existsEmail(String email);
}
