package com.example.be.apiPayload.exception.tokenException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenException extends RuntimeException {
    private final TokenErrorResult tokenErrorResult;

    @Override
    public String getMessage() {
        return tokenErrorResult.getMessage();
    }
}