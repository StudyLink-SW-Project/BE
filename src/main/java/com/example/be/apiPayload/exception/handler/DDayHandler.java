package com.example.be.apiPayload.exception.handler;

import com.example.be.apiPayload.code.BaseErrorCode;
import com.example.be.apiPayload.exception.GeneralException;

public class DDayHandler extends GeneralException {
    public DDayHandler(BaseErrorCode code) {
        super(code);
    }
}
