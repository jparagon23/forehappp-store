package com.forehapp.store.general.exceptions;

public class ForbiddenException extends RuntimeException {
    private final ErrorCode code;

    public ForbiddenException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() { return code; }
}
