package com.ssafy.cotrip.api.user.service;

import com.ssafy.cotrip.api.user.dto.response.*;
import com.ssafy.cotrip.api.user.repository.OAuthMapper;
import com.ssafy.cotrip.api.user.repository.UserMapper;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.UserHandler;
import com.ssafy.cotrip.domain.User;
import com.ssafy.cotrip.security.jwt.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.ssafy.cotrip.apiPayload.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserMapper userMapper;
    private final OAuthMapper oAuthMapper;
    private final JwtTokenUtil jwtTokenUtil;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Override
    @Transactional
    public CreateUserResponseDto insertUser(String email, String password, String nickname) {

        User newUser = User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .nickname(nickname)
                .build();

        try {
            userMapper.insertUser(newUser);

        } catch (DuplicateKeyException e) {

            String message = e.getMostSpecificCause().getMessage();

            if (message.contains("uk_users_email")) {
                throw new UserHandler(EXISTED_EMAIL);

            } else {
                throw new UserHandler(EXISTED_NAME);
            }
        }

        Long userId = newUser.getId();
        CreateUserResponseDto responseDto = CreateUserResponseDto.builder()
                .userId(userId)
                .build();

        return responseDto;
    }

    @Override
    public TokenResponseDto login(String email, String password, HttpServletResponse response) {
        User findUser = userMapper.findByEmail(email);
        if (findUser == null) {
            throw new UserHandler(MEMBER_NO_EXIST);
        }

        // 탈퇴한 회원 체크
        if (findUser.getDeletedAt() != null) {
            throw new UserHandler(WITHDRAWAL_MEMBER);
        }

        if (!bCryptPasswordEncoder.matches(password, findUser.getPassword())) {
            throw new UserHandler(PASSWORD_NOT_MATCH);
        }

        Long userId = findUser.getId();

        String accessToken = jwtTokenUtil.generateToken(userId, email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(userId, email);

        addCookie(response, refreshToken);

        TokenResponseDto responseDto = TokenResponseDto.builder()
                .accessToken(accessToken)
                .build();

        return responseDto;
    }

    @Override
    public TokenResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenUtil.extractRefreshTokenFromCookie(request);

        if (refreshToken == null || isBlacklisted(refreshToken)) {
            throw new UserHandler(ErrorStatus.INVALID_TOKEN);
        }

        if (jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new UserHandler(ErrorStatus.EXPIRED_REFRESH_TOKEN);
        }

        Long userId = jwtTokenUtil.extractUserId(refreshToken);
        String email = jwtTokenUtil.extractUsername(refreshToken);

        setBlacklist(refreshToken);

        String newAccess = jwtTokenUtil.generateToken(userId, email);
        String newRefresh = jwtTokenUtil.generateRefreshToken(userId, email);

        addCookie(response, newRefresh);

        TokenResponseDto responseDto = TokenResponseDto.builder()
                .accessToken(newAccess).build();

        return responseDto;
    }

    private void addCookie(HttpServletResponse response, String newRefresh) {
        Cookie newCookie = new Cookie("refreshToken", newRefresh);
        newCookie.setHttpOnly(true);
        newCookie.setPath("/api/v1/auth");
        newCookie.setMaxAge((int) jwtTokenUtil.getRemainingTime(newRefresh));
        response.addCookie(newCookie);
    }

    public void setBlacklist(String token) {
        long ttl = jwtTokenUtil.getRemainingTime(token);

        if (ttl <= 0)
            return;

        redisTemplate.opsForValue()
                .set(BLACKLIST_PREFIX + token, "blacklisted",
                        ttl, TimeUnit.MILLISECONDS);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenUtil.extractRefreshTokenFromCookie(request);

        long ttl = jwtTokenUtil.getRemainingTime(refreshToken);

        if (ttl <= 0)
            return;

        redisTemplate.opsForValue()
                .set(BLACKLIST_PREFIX + refreshToken, "blacklisted",
                        ttl, TimeUnit.MILLISECONDS);

        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setPath("/api/v1/auth");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new UserHandler(MEMBER_NO_EXIST);
        }

        userMapper.deleteByUserId(user.getId());
        oAuthMapper.deleteByUserId(user.getId());
    }

    @Override
    @Transactional
    public void updatePassword(String password, String email) {
        userMapper.updatePassword(bCryptPasswordEncoder.encode(password), email);
    }

    @Override
    @Transactional
    public void updateNickname(String nickname, String email) {
        try {
            userMapper.updateNickname(nickname, email);
        } catch (DuplicateKeyException e) {
            throw new UserHandler(EXISTED_NAME);
        }
    }

    @Override
    public GetUserResponseDto getUser(String email) {
        User user = userMapper.findByEmail(email);
        GetUserResponseDto responseDto = GetUserResponseDto.builder()
                .email(email)
                .nickname(user.getNickname())
                .userId(user.getId())
                .role(user.getRole())
                .loginType(user.getLoginType() != null ? user.getLoginType() : "SYSTEM")
                .build();

        return responseDto;
    }

    @Override
    public CheckNicknameDuplicatedResponseDto isNicknameDuplicated(String nickname) {
        boolean isDuplicated = userMapper.existsNickname(nickname);
        CheckNicknameDuplicatedResponseDto responseDto = CheckNicknameDuplicatedResponseDto.builder()
                .isDuplicated(isDuplicated).build();

        return responseDto;
    }

    @Override
    public CheckEmailDuplicatedResponseDto isEmailDuplicated(String email) {
        boolean isDuplicated = userMapper.existsEmail(email);
        CheckEmailDuplicatedResponseDto responseDto = CheckEmailDuplicatedResponseDto.builder()
                .isDuplicated(isDuplicated).build();

        return responseDto;
    }

    @Override
    public CheckPasswordResponseDto isPasswordRight(String writtenPassword, String savedPassword) {
        boolean isRight = bCryptPasswordEncoder.matches(writtenPassword, savedPassword);
        CheckPasswordResponseDto responseDto = CheckPasswordResponseDto.builder()
                .isRight(isRight).build();

        return responseDto;
    }
}
