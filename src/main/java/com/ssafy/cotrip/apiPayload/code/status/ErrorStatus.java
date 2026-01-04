package com.ssafy.cotrip.apiPayload.code.status;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 일반적인 에러
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON402", "금지된 요청입니다."),

    // jwt
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT4001", "유효하지 않은 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "JWT4002", "만료된 access token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "JWT4003", "만료된 refresh token입니다."),

    // OAuth
    OAUTH_LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "AUTH4001", "OAuth2 로그인에 실패했습니다."),

    // Member
    EXISTED_NAME(HttpStatus.BAD_REQUEST, "MEMBER4001", "이미 존재하는 닉네임입니다."),
    MEMBER_NO_EXIST(HttpStatus.NOT_FOUND, "MEMBER4002", "존재하지 않는 회원입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "MEMBER4003", "비밀번호가 일치하지 않습니다."),
    WITHDRAWAL_MEMBER(HttpStatus.FORBIDDEN, "MEMBER4004", "탈퇴한 회원입니다."),
    EXISTED_EMAIL(HttpStatus.BAD_REQUEST, "MEMBER4005", "이미 존재하는 이메일입니다."),

    // Hotplace
    POST_AUTHORIZATION(HttpStatus.FORBIDDEN, "POST4001", "게시글에 대한 권한이 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4002", "존재하지 않는 게시글입니다."),

    // S3
    IMAGE_INVALID_EXTENSION(HttpStatus.BAD_REQUEST, "IMAGE4001", "허용되지 않은 이미지 확장자입니다."),
    FAIL_IMAGE_DELETE(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE4002", "이미지 삭제에 실패했습니다."),

    // Expense
    TARGET_USER_REQUIRED(HttpStatus.BAD_REQUEST, "EXPENSE4001", "대상자가 최소 1명 필요합니다."),
    EXPENSE_NOT_FOUND(HttpStatus.BAD_REQUEST, "EXPENSE4002", "존재하지 않는 지출 내역입니다."),
    INVALID_TARGET_USER(HttpStatus.BAD_REQUEST, "EXPENSE4003", "대상자가 플랜 참가자가 아닙니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "EXPENSE4004", "유효하지 않은 커서입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}