package com.ssafy.cotrip.api.user.service;

import com.ssafy.cotrip.api.user.dto.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    CreateUserResponseDto insertUser(String email, String password, String nickname);
    TokenResponseDto login(String email, String password, HttpServletResponse response);
    TokenResponseDto refresh(HttpServletRequest request, HttpServletResponse response);
    void logout(HttpServletRequest request, HttpServletResponse response);
    void deleteUser(String email);
    void updatePassword(String password, String email);
    void updateNickname(String nickname, String email);
    GetUserResponseDto getUser(String email);
    CheckNicknameDuplicatedResponseDto isNicknameDuplicated(String nickname);
    CheckEmailDuplicatedResponseDto isEmailDuplicated(String email);
    CheckPasswordResponseDto isPasswordRight(String writtenPassword, String savedPassword);
}
