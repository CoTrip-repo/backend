package com.ssafy.cotrip.apiPayload.exception.handler;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.exception.GeneralException;

public class HotplaceHandler extends GeneralException {

    public HotplaceHandler(BaseErrorCode code) {
        super(code);
    }

    public HotplaceHandler(BaseErrorCode code, String customMessage) {
        super(code, customMessage);
    }

}
