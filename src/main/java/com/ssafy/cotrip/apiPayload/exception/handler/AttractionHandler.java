package com.ssafy.cotrip.apiPayload.exception.handler;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.exception.GeneralException;

public class AttractionHandler extends GeneralException {

    public AttractionHandler(BaseErrorCode code) {
        super(code);
    }

    public AttractionHandler(BaseErrorCode code, String customMessage) {
        super(code, customMessage);
    }

}
