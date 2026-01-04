package com.ssafy.cotrip.apiPayload.exception.handler;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.exception.GeneralException;

public class S3Handler extends GeneralException {
    public S3Handler(BaseErrorCode code) {
        super(code);
    }

    public S3Handler(BaseErrorCode code, String customMessage) {
        super(code, customMessage);
    }
}
