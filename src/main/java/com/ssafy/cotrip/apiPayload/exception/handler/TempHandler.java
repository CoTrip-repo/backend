package com.ssafy.cotrip.apiPayload.exception.handler;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.exception.GeneralException;

public class TempHandler extends GeneralException {

    public TempHandler(BaseErrorCode code) {
        super(code);
    }
}
