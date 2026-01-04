package com.ssafy.cotrip.api.user.controller;

import com.ssafy.cotrip.api.user.dto.request.*;
import com.ssafy.cotrip.api.user.dto.response.*;
import com.ssafy.cotrip.api.user.service.UserService;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    @PostMapping("/v1/auth/users")
    public ApiResponse<CreateUserResponseDto> insertUser(@RequestBody CreateUserRequestDto requestDto) {
        CreateUserResponseDto responseDto = userService.insertUser(requestDto.email(), requestDto.password(), requestDto.nickname());

        return ApiResponse.onSuccess(responseDto);
    }

    @PostMapping("/v1/auth/login")
    public ApiResponse<TokenResponseDto> login(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        TokenResponseDto responseDto = userService.login(requestDto.email(), requestDto.password(),response);

        return ApiResponse.onSuccess(responseDto);
    }

    @PostMapping("/v1/auth/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response);

        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/v1/auth/refresh")
    public ApiResponse<TokenResponseDto> refresh(HttpServletRequest request, HttpServletResponse response) {
        TokenResponseDto responseDto = userService.refresh(request, response);

        return ApiResponse.onSuccess(responseDto);
    }

    @DeleteMapping("/v1/users")
    public ApiResponse<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());

        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/v1/users/password")
    public ApiResponse<Void> updatePassword(@RequestBody UpdatePasswordRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        userService.updatePassword(requestDto.password(), userDetails.getUsername());

        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/v1/users/nickname")
    public ApiResponse<Void> nicknamePassword(@RequestBody UpdateNicknameRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        userService.updateNickname(requestDto.nickname(), userDetails.getUsername());

        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/v1/users/my")
    public ApiResponse<GetUserResponseDto> getUser(@AuthenticationPrincipal UserDetails userDetails) {

        GetUserResponseDto responseDto = userService.getUser(userDetails.getUsername());

        return ApiResponse.onSuccess(responseDto);
    }

    @GetMapping("/v1/auth/users/nickname")
    public ApiResponse<CheckNicknameDuplicatedResponseDto> checkNicknameDuplicated(@RequestParam String nickname) {
        CheckNicknameDuplicatedResponseDto responseDto = userService.isNicknameDuplicated(nickname);

        return ApiResponse.onSuccess(responseDto);
    }

    @GetMapping("/v1/auth/users/email")
    public ApiResponse<CheckEmailDuplicatedResponseDto> checkEmailDuplicated(@RequestParam String email) {
        CheckEmailDuplicatedResponseDto responseDto = userService.isEmailDuplicated(email);

        return ApiResponse.onSuccess(responseDto);
    }

    @PostMapping("/v1/users/password")
    public ApiResponse<CheckPasswordResponseDto> checkPassword(@RequestBody CheckPasswordRequestDto requestDto,  @AuthenticationPrincipal UserDetails userDetails) {
        CheckPasswordResponseDto responseDto = userService.isPasswordRight(requestDto.password(), userDetails.getPassword());

        return ApiResponse.onSuccess(responseDto);
    }
}
