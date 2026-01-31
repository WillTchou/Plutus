package com.project.plutus.exceptions;

public class PlutusStateException extends RuntimeException{
    protected final String code;
    protected final String message;

    public PlutusStateException(final String code, final String message) {
        this.code = code;
        this.message = message;
    }
}
