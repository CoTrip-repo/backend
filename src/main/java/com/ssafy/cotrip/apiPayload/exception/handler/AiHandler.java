package com.ssafy.cotrip.apiPayload.exception.handler;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.exception.GeneralException;

public class AiHandler extends GeneralException {
    public AiHandler(BaseErrorCode code) {
        super(code);
    }

    public AiHandler(BaseErrorCode code, String customMessage) {
        super(code, customMessage);
    }
}
