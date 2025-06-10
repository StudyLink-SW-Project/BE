package com.example.be.apiPayload.exception.handler;

import com.example.be.apiPayload.code.BaseErrorCode;
import com.example.be.apiPayload.exception.GeneralException;

public class RoomHandler extends GeneralException {
    public RoomHandler(BaseErrorCode code) {
        super(code);
    }

}
