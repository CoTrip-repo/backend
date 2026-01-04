package com.ssafy.cotrip.apiPayload.exception;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.code.ErrorReasonDTO;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final BaseErrorCode code;
    private final String customMessage;

    // 1) 기본 메시지 사용할 때
    public GeneralException(BaseErrorCode code) {
        this.code = code;
        this.customMessage = null;
    }

    // 2) 커스텀 메시지 사용할 때
    public GeneralException(BaseErrorCode code, String customMessage) {
        this.code = code;
        this.customMessage = customMessage;
    }

    public ErrorReasonDTO getErrorReason() {
        ErrorReasonDTO base = this.code.getReason();
        if (customMessage == null) {
            return base;
        }

        return ErrorReasonDTO.builder()
                .code(base.getCode())
                .message(customMessage)
                .isSuccess(base.getIsSuccess())
                .build();
    }

    public ErrorReasonDTO getErrorReasonHttpStatus() {
        ErrorReasonDTO base = this.code.getReasonHttpStatus();
        if (customMessage == null) {
            return base;
        }

        return ErrorReasonDTO.builder()
                .code(base.getCode())
                .message(customMessage)
                .isSuccess(base.getIsSuccess())
                .httpStatus(base.getHttpStatus())
                .build();
    }
}
