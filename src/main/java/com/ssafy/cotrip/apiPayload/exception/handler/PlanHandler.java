package com.ssafy.cotrip.apiPayload.exception.handler;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.exception.GeneralException;

public class PlanHandler extends GeneralException {
    public PlanHandler(BaseErrorCode code) {
        super(code);
    }

    public PlanHandler(BaseErrorCode code, String customMessage) {
        super(code, customMessage);
    }
}
