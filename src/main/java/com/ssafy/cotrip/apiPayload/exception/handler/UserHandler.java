package com.ssafy.cotrip.apiPayload.exception.handler;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.exception.GeneralException;

public class UserHandler extends GeneralException {
    public UserHandler(BaseErrorCode code) {
        super(code);
    }
}
