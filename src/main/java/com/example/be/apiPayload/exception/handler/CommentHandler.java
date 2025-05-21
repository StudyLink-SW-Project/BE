package com.example.be.apiPayload.exception.handler;

import com.example.be.apiPayload.code.BaseErrorCode;
import com.example.be.apiPayload.exception.GeneralException;

public class CommentHandler extends GeneralException {
    public CommentHandler(BaseErrorCode code) {
        super(code);
    }

}
