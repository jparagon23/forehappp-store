package com.forehapp.store.general.dto;

import com.forehapp.store.general.exceptions.ErrorCode;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String code;
    private final String error;

    public ErrorResponse(ErrorCode code, String error) {
        this.code = code.name();
        this.error = error;
    }

    public ErrorResponse(String error) {
        this.code = ErrorCode.INTERNAL_ERROR.name();
        this.error = error;
    }
}
