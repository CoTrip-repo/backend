package com.ssafy.cotrip.apiPayload.exception.handler;

import com.ssafy.cotrip.apiPayload.code.BaseErrorCode;
import com.ssafy.cotrip.apiPayload.exception.GeneralException;

public class ExpenseHandler extends GeneralException {
    public ExpenseHandler(BaseErrorCode code) {
        super(code);
    }

    public ExpenseHandler(BaseErrorCode code, String customMessage) {
        super(code, customMessage);
    }
}
