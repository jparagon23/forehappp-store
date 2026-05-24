package com.forehapp.store.general.exceptions;

public class BadRequestException extends RuntimeException {
    private final ErrorCode code;

    public BadRequestException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() { return code; }
}
