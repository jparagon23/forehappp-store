package com.forehapp.store.general.exceptions;

public class ConflictException extends RuntimeException {
    private final ErrorCode code;

    public ConflictException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() { return code; }
}
